package com.sunbox.formator;

public interface Formator<T> {
		
	public T parse(String text);
	
	public String format(T t);
	
}
