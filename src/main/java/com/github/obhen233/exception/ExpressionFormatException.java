package com.github.obhen233.exception;

public class ExpressionFormatException extends RuntimeException{
	
	ExpressionFormatException(){
		super();
	}
	
	public ExpressionFormatException(String errorInfo) {
		super(errorInfo);
	}
}
