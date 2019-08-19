package com.sunbox.exception;

public class BudErrorUseException  extends RuntimeException{
	
	public BudErrorUseException(){
		super();
	}
	
	public BudErrorUseException(String errorInfo) {
		super(errorInfo);
	}
}
