package com.sunbox.exception;

public class RuleKeyRepeatsException   extends RuntimeException{
	
	RuleKeyRepeatsException(){
		super();
	}
	
	public RuleKeyRepeatsException(String errorInfo) {
		super(errorInfo);
	}
}
