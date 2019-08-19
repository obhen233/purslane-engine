package com.sunbox.exception;

public class NotFindBaseException  extends RuntimeException{
	
	public NotFindBaseException(){
		super();
	}
	
	public NotFindBaseException(String errorInfo) {
		super(errorInfo);
	}
}
