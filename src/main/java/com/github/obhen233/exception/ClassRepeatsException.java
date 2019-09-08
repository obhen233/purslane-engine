package com.github.obhen233.exception;

public class ClassRepeatsException   extends RuntimeException{


	public ClassRepeatsException(){
		super();
	}
	
	public ClassRepeatsException(String errorInfo) {
		super(errorInfo);
	}
}
