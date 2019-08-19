package com.sunbox.exception;

public class ClassRepeatsException   extends RuntimeException{


	public ClassRepeatsException(){
		super();
	}
	
	public ClassRepeatsException(String errorInfo) {
		super(errorInfo);
	}
}
