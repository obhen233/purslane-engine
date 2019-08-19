package com.sunbox.out;

import java.io.Serializable;

public class FeildInfo  implements Serializable,Comparable {
	
	private String name;

	private String simpleName;
	
	private String desc;
	
	private String lang;
	
	private String value;
	
	private FieldType fieldType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(!(obj instanceof FeildInfo))
			return false;
		return this.name.equals(((FeildInfo)obj).getName());
	}
	@Override
	public int compareTo(Object obj){
		if (!(obj instanceof FeildInfo))
			throw new RuntimeException("noy a FeildInfo:"+obj.getClass());
		FeildInfo feildInfo = (FeildInfo)obj;
		return this.name.compareTo(feildInfo.name);
 
	}
	
	
}
