package com.sunbox.exception;

public class NotFindParamException extends RuntimeException{
	
	public NotFindParamException(){
		super();
	}
	
	public NotFindParamException(String errorInfo) {
		super(errorInfo);
	}
}
