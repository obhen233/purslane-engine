package com.github.obhen233.attribute;

//NOT the result of T
//对T的执行结果取非
public class Nrule extends Rule{
	
	private Rule T;
	
	public Nrule(Rule T){
		this.T = T;
	}

	@Override
	public boolean excute() {
		boolean f =  T.excute();
		return !f;
	}
	
	public Rule getRule(){
		return T;
	}
}
