package com.stefanini.mav.mensagem;

import com.stefanini.mav.es.ConfiguracaoMensagem;
import com.stefanini.mav.mensagem.Cabecalho.Fluxo;

@ConfiguracaoMensagem(inicio = 83, sentido = Fluxo.SAIDA)
public class RespostaPropostaFinanciamento extends RespostaPropostaPadrao {

	private static final long serialVersionUID = 8742611196995104074L;

	public RespostaPropostaFinanciamento(String id, Cabecalho cabecalho) {
		super(id, cabecalho);
	}
	
}
