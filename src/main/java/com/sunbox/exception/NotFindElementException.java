package com.sunbox.exception;

public class NotFindElementException extends RuntimeException{
	public NotFindElementException(){
		super();
	}
	
	public NotFindElementException(String errorInfo) {
		super(errorInfo);
	}
}
