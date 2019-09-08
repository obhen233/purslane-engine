package com.github.obhen233.out;

import java.io.Serializable;
import java.util.TreeSet;

public class RootParam implements Serializable {
	
	private String unid;
	
	private TreeSet<FeildInfo> feilds;

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	public TreeSet<FeildInfo> getFeilds() {
		return feilds;
	}

	public void setFeilds(TreeSet<FeildInfo> feilds) {
		this.feilds = feilds;
	}

	
	
	
	
	
	
}
