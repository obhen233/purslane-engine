package com.github.obhen233.exception;

public class UnidNullException extends RuntimeException {
	
	public UnidNullException(){
		super();
	}
	
	public UnidNullException(String errorInfo) {
		super(errorInfo);
	}
}
