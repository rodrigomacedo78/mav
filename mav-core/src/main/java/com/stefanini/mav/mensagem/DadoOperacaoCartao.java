package com.stefanini.mav.mensagem;

import java.io.Serializable;

public class DadoOperacaoCartao implements Serializable {
	
	private static final long serialVersionUID = 7678829367254641220L;

	public String getCodigoOrg() {
		return codigoOrg;
	}

	protected void setCodigoOrg(String codigoOrg) {
		this.codigoOrg = codigoOrg;
	}

	public String getCodigoLogo() {
		return codigoLogo;
	}

	protected void setCodigoLogo(String codigoLogo) {
		this.codigoLogo = codigoLogo;
	}

	public String getCodigoCampanha() {
		return codigoCampanha;
	}

	protected void setCodigoCampanha(String codigoCampanha) {
		this.codigoCampanha = codigoCampanha;
	}

	public String getCodigoModalidade() {
		return codigoModalidade;
	}

	protected void setCodigoModalidade(String codigoModalidade) {
		this.codigoModalidade = codigoModalidade;
	}

	public String getFiller() {
		return filler;
	}

	protected void setFiller(String filler) {
		this.filler = filler;
	}

	private String codigoOrg;
	
	private String codigoLogo;
	
	private String codigoCampanha;
	
	private String codigoModalidade;
	
	private String filler;


}