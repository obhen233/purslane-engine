package com.github.obhen233.exception;

public class NotFindBaseException  extends RuntimeException{
	
	public NotFindBaseException(){
		super();
	}
	
	public NotFindBaseException(String errorInfo) {
		super(errorInfo);
	}
}
