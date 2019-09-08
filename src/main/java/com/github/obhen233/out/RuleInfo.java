package com.github.obhen233.out;

import java.io.Serializable;
import java.util.List;

public class RuleInfo implements Serializable {
	
	private String function;
	
	private String desc;
	
	private String lang;
	
	private List<FeildInfo> feilds;

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public List<FeildInfo> getFeilds() {
		return feilds;
	}

	public void setFeilds(List<FeildInfo> feilds) {
		this.feilds = feilds;
	}
	
	
	
}
