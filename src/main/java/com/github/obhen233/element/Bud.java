package com.github.obhen233.element;

import com.github.obhen233.result.ExcuteResult;

//when there are all Nodes,but want join with "AND",can use it.
public class Bud extends Leaf{
	
	private final boolean flag = true;

	@Override
	public ExcuteResult excute() {
		ExcuteResult result = new ExcuteResult();
		result.setResult(flag);
		result.setFormatMsg("SUCCESS!");
		return result;
	}
	
	@Override
	public String toString() {
		return "[]";
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (!(obj instanceof Bud)) { 
	         return false; 
	     }
		return super.equals(obj);
	}
	
	
}
