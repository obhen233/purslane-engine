package com.sunbox.attribute;

import java.io.Serializable;

// A single rule
public abstract class Rule implements Serializable,Attribute {
	
	private String param;
	
	private String base;
	
	
	
	public String getParam() {
		return param;
	}



	public void setParam(String param) {
		this.param = param;
	}



	public String getBase() {
		return base;
	}



	public void setBase(String base) {
		this.base = base;
	}


	public abstract boolean excute(); 
	
	public Object excuteMsg(){
		return "";
	}
	
}
