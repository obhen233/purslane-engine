package com.github.obhen233.exception;

public class NotFindElementException extends RuntimeException{
	public NotFindElementException(){
		super();
	}
	
	public NotFindElementException(String errorInfo) {
		super(errorInfo);
	}
}
