package com.sunbox.common.rule;

import java.util.List;

import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.attribute.Rule;

public class IncludeObj  extends Rule{

	@RuleBase
	private List<String> list; 
	
	@Override
	public boolean excute() {
		return list.contains(this.getParam());
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}
	
	

}
