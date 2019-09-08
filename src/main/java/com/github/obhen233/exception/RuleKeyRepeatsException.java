package com.github.obhen233.exception;

public class RuleKeyRepeatsException   extends RuntimeException{
	
	RuleKeyRepeatsException(){
		super();
	}
	
	public RuleKeyRepeatsException(String errorInfo) {
		super(errorInfo);
	}
}
