package com.stefanini.mav.es;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapBean {
	
	int tamanho() default 1;
	
	boolean obrigatorio() default false;
	
	boolean propagar() default false;
	
}
