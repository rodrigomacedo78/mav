package com.stefanini.mav.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public final class ServiceLocator implements Serializable {

	private static final long serialVersionUID = 8815838232418882949L;

	private static ServiceLocator INSTANCE = new ServiceLocator();
	
	public enum Service {
		
		GERENTE_MENSAGEM("gerenteMensagem");
		
		private String name;
		
		private Service(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}

	@Autowired
	private ApplicationContext applicationContext;

	private ServiceLocator() {
	}

	public static ServiceLocator getInstance() {
		return INSTANCE;
	}
	
	public <T> T getService(final Service service, final Class<T> type) {
		
		return applicationContext.getBean(Service.GERENTE_MENSAGEM.getName(), type);
	}
}
