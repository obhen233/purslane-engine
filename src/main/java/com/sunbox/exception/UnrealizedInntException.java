package com.sunbox.exception;

public class UnrealizedInntException extends RuntimeException{
	
	public UnrealizedInntException(){
		super();
	}
	
	public UnrealizedInntException(String errorInfo) {
		super(errorInfo);
	}
}
