package com.github.obhen233.exception;

public class ClassUnregisteredException  extends RuntimeException{
	
	ClassUnregisteredException(){
		super();
	}
	
	public ClassUnregisteredException(String errorInfo) {
		super(errorInfo);
	}
}
