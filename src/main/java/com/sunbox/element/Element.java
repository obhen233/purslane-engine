package com.sunbox.element;

import com.sunbox.result.ExcuteResult;

/*
 * 每一个规则都是一个元素
 * Every Leaf or Node Is An Element.
 * */
public interface Element {
	public ExcuteResult excute();
}
