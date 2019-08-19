package com.sunbox.element;

import java.util.List;

import com.sunbox.result.ExcuteResult;
//the root element 
public class Root extends Node{
	private String unid;//unique identification//为区分不同的规则表达式 
	
	@Override
	public ExcuteResult excute() {
		// TODO Auto-generated method stub
		return super.excute();
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	
	@Override
	public String toString(){
		return super.toString();
	}
	
	
	
	
}
