package com.sunbox.common.rule;

import com.sunbox.attribute.Rule;

public class Greater extends Rule{

	@Override
	public boolean excute() {
		
		return Integer.parseInt(this.getParam()) > Integer.parseInt(this.getBase());
	}
	
	
}