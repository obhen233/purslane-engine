package com.github.obhen233.exception;

public class NotFindParamException extends RuntimeException{
	
	public NotFindParamException(){
		super();
	}
	
	public NotFindParamException(String errorInfo) {
		super(errorInfo);
	}
}
