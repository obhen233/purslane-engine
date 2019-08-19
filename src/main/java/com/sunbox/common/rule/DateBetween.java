package com.sunbox.common.rule;

import java.util.Date;

import com.sunbox.annotation.framework.DateFormat;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.attribute.Rule;

public class DateBetween  extends Rule{

	@DateFormat
	@RuleBase
	private Date startDate;
	
	@DateFormat
	@RuleBase
	private Date endDate;
	
	@DateFormat
	@RuleParam
	private Date target;
	
	@Override
	public boolean excute() {
		// TODO Auto-generated method stub
		 return endDate.compareTo(target) >= 0 && target.compareTo(startDate) >= 0;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getTarget() {
		return target;
	}

	public void setTarget(Date target) {
		this.target = target;
	}
	
	
	
	
	

}
