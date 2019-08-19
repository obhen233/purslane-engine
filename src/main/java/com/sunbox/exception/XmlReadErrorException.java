package com.sunbox.exception;

public class XmlReadErrorException extends Exception{
	public XmlReadErrorException(){
		super();
	}
	
	public XmlReadErrorException(String errorInfo) {
		super(errorInfo);
	}
}
