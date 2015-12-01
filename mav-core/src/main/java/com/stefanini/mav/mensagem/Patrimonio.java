package com.stefanini.mav.mensagem;

import java.io.Serializable;

public class Patrimonio implements Serializable {

	private static final long serialVersionUID = 6764766881318842021L;
	
	private String tipo;
	
	private String nome;
	
	private Double valor;
	
	private String origem;
	
	@Override
	public boolean equals(Object obj) {
		
		if(!Patrimonio.class.isInstance(obj)) {
			
			return false;
		}
		
		Patrimonio outro = Patrimonio.class.cast(obj);
		return tipo.equals(outro.tipo)
				&& nome.equals(outro.nome)
				&& valor.equals(outro.nome)
				&& origem.equals(outro.origem);
	}

	public String getTipo() {
		return tipo;
	}

	protected void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNome() {
		return nome;
	}

	protected void setNome(String nome) {
		this.nome = nome;
	}

	public Double getValor() {
		return valor;
	}

	protected void setValor(Double valor) {
		this.valor = valor;
	}

	public String getOrigem() {
		return origem;
	}

	protected void setOrigem(String origem) {
		this.origem = origem;
	}



}