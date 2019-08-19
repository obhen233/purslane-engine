package com.sunbox.exception;

public class UnidNullException extends RuntimeException {
	
	public UnidNullException(){
		super();
	}
	
	public UnidNullException(String errorInfo) {
		super(errorInfo);
	}
}
