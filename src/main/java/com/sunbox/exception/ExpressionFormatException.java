package com.sunbox.exception;

public class ExpressionFormatException extends RuntimeException{
	
	ExpressionFormatException(){
		super();
	}
	
	public ExpressionFormatException(String errorInfo) {
		super(errorInfo);
	}
}
