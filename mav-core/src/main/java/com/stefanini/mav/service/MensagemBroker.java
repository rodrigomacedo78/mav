package com.stefanini.mav.service;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stefanini.mav.core.Mensagem;
import com.stefanini.mav.es.MapeamentoNaoEncontrado;
import com.stefanini.mav.mensagem.CodigoMensagem;
import com.stefanini.mav.mensagem.MensagemBasica;
import com.stefanini.mav.mensagem.MensagemFactory;
import com.stefanini.mav.mensagem.MensagemNaoEncontradaException;
import com.stefanini.mav.mensagem.RespostaErro;
import com.stefanini.mav.mensagem.StatusProposta;
import com.stefanini.mav.service.ServiceLocator.Service;

public class MensagemBroker {
	
	private static Logger _LOGGER = LoggerFactory.getLogger(MensagemBroker.class);
	
	public enum MensagemErroBroker {
		
		MSG_ERRO_RETORNO("Erro ao processar retorno. servidor: %s, porta: %s"),
		MSG_ERRO_CONEXAO("Erro de conexão com a parceira. servidor: %s, porta: %s"),
		MSG_ERRO_AUSENCIA_PARCEIRA("Não há conexões disponíveis com as parceiras."),
		MSG_AUSENCIA_CODIGO_RETORNO("Não há código de retorno para essa mensagem.");
		
		private String texto;
		
		private MensagemErroBroker(String texto) {
			
			this.texto = texto;
		}
		
		public String getTexto() {
			return texto;
		}
		
		public MensagemBasica wrap(MensagemBasica m) throws MensagemNaoEncontradaException, MapeamentoNaoEncontrado {
			
			return wrap(m, null, new Object[0]);
		}
		
		public MensagemBasica wrap(MensagemBasica m, Throwable t, Parceira p) throws MensagemNaoEncontradaException, MapeamentoNaoEncontrado {
			
			return wrap(m, t, p.getServidor(), p.getPorta());
		}
		
		protected MensagemBasica wrap(MensagemBasica m, Throwable t, Object... args) throws MensagemNaoEncontradaException, MapeamentoNaoEncontrado {
			
			String mensagemErro = String.format(texto, args);
			if(t != null) {
				_LOGGER.error(mensagemErro, t);	
			}
			return MensagemFactory.gerarErro(m, mensagemErro);
		}
	}


	private static final MensagemBroker broker = new MensagemBroker();
	
	private LinkedHashSet<Parceira> parceiras = new LinkedHashSet<>();
	
	private IGerenciaMensagem gerente = ServiceLocator.getInstance().getService(Service.GERENTE_MENSAGEM, IGerenciaMensagem.class);
	
	private MensagemBroker() {
	}
	
	public static MensagemBroker getInstance() {
		return broker;
	}
	
	public List<Parceira> getParceiras() {
		return new LinkedList<>(parceiras);
	}
	
	public void setParceira(Parceira parceira) {
		parceiras.add(parceira);
	}
	
	public void clear() {
		parceiras.clear();
	}
	
	public static Parceira localizarParceira(String id, List<Parceira> parceiras) {
		
		if(parceiras.indexOf(new Parceira(id, "", null)) == -1) {
			return null;
		}
		
		return parceiras.get(parceiras.indexOf(new Parceira(id, "", null)));
	}
	
	
	public MensagemBasica enviarParceira(MensagemBasica mensagemBasica) throws MensagemNaoEncontradaException, BrokerException, MapeamentoNaoEncontrado {

		//Não há parceiras registradas
		if(parceiras.isEmpty()) {
			
			return MensagemErroBroker.MSG_ERRO_AUSENCIA_PARCEIRA.wrap(mensagemBasica);
		}
		
		//verificar se a mensagem não é 450 e assim localizar o fluxo anterior da proposta
		if(CodigoMensagem.C0450.equals(mensagemBasica.getCabecalho().getCodigo())) {
			MensagemBasica rs = null;
			
			for (Parceira parceira : parceiras) {
			
				rs = enviarMensagemParceira(mensagemBasica, parceira);
				if(StatusProposta.ELEGIVEL.getCodigo().equals(rs.getCabecalho().getCodigoRetorno())) {
					
					break;
				}
			}
			
			return rs;
		}
		else {
			String parceiraId = gerente.recuperarParceira(mensagemBasica);
			Parceira p = localizarParceira(parceiraId, new LinkedList<>(parceiras));
			return enviarMensagemParceira(mensagemBasica, p);
		}
		
	}

	private MensagemBasica enviarMensagemParceira(MensagemBasica mensagemBasica, Parceira parceira) throws MensagemNaoEncontradaException, MapeamentoNaoEncontrado, BrokerException {
		
		
		Mensagem mensagemDb = gerente.salvar(mensagemBasica);
		MensagemBasica retorno = null;
		
		try {
			_LOGGER.info("Processando conexão com " + parceira.getNome());
			gerente.gravarMensagemParceira(mensagemDb, parceira);
			retorno = parceira.processar(mensagemBasica);
			
		} catch (RuntimeException t) {
			
			RespostaErro erro = (RespostaErro) MensagemErroBroker.MSG_ERRO_CONEXAO.wrap(mensagemBasica, t, parceira);
			gerente.gravarMensagemParceira(gerente.salvar(erro), parceira);
			return erro;
		}
		
		try {
			Mensagem retornoMensagemDb = gerente.salvar(retorno);
			gerente.gravarMensagemParceira(retornoMensagemDb, parceira);
			
			//verificar se mensagem de erro
			if(!retorno.isOk()) {
				
				return retorno;
			}
			
			if(retorno.getCabecalho().getCodigoRetorno().isEmpty()) {
				
				RespostaErro erro = (RespostaErro) MensagemErroBroker.MSG_AUSENCIA_CODIGO_RETORNO.wrap(mensagemBasica, null, parceira);
				gerente.gravarMensagemParceira(gerente.salvar(erro), parceira);
				return erro;
			}
			
			return retorno;
			
		} catch (RuntimeException t) {
			
			RespostaErro erro = (RespostaErro) MensagemErroBroker.MSG_ERRO_RETORNO.wrap(mensagemBasica, t, parceira);
			gerente.gravarMensagemParceira(gerente.salvar(erro), parceira);
			return erro;
		}
	}	
}