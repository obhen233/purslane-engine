package com.sunbox.common.rule;

import com.sunbox.attribute.Rule;

public class Equals extends Rule{

	@Override
	public boolean excute() {
		// TODO Auto-generated method stub
		return this.getBase().equals(this.getParam());
	}
	
	
}
