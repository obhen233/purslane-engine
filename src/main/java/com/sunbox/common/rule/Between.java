package com.sunbox.common.rule;

import com.sunbox.annotation.framework.Description;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.attribute.Rule;

public class Between  extends Rule{

	@RuleBase
	private Integer start;
	
	@RuleBase
	private Integer end;
	
	@RuleParam
	private Integer target;
	
	@Override
	public boolean excute() {
		System.out.println(end+":"+target+":"+start);
		System.out.println(end.compareTo(target));
		System.out.println(target.compareTo(start));
		return end.compareTo(target) >= 0 && target.compareTo(start) >= 0;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public Integer getTarget() {
		return target;
	}

	public void setTarget(Integer target) {
		this.target = target;
	}
	
}
