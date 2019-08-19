package com.sunbox.exception;

public class NoFindRootException  extends RuntimeException{
	
	public NoFindRootException(){
		super();
	}
	
	public NoFindRootException(String errorInfo) {
		super(errorInfo);
	}
}
