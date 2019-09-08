package com.github.obhen233.result;

import java.io.Serializable;

public class ExcuteResult implements Serializable {
	
	private boolean result = false;
	
	private Object formatMsg;

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Object getFormatMsg() {
		return formatMsg;
	}

	public void setFormatMsg(Object formatMsg) {
		this.formatMsg = formatMsg;
	}
	
	@Override
	public String toString() {
		return this.formatMsg == null ? "":this.formatMsg.toString();
	}
	
	
}
