package com.sunbox.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class I18nUtil {
	
	public static Properties properties = null;
	private static Logger logger = LoggerFactory.getLogger(I18nUtil.class);
	static{
		try{
			if(properties == null){
				ClassLoader classLoader = I18nUtil.class.getClassLoader();
				InputStream in = classLoader.getResourceAsStream("i18n.properties");
				if(in != null){
					properties = new Properties();
					properties.load(in);
				}
			}
		}catch(Exception e){
			logger.info("I18nUtil innt properties error",e);
		}
	}
	
	public static String getValue(String key){
		if(properties == null)
			return "";
		return properties.getProperty(key,"");
	}
	
	public static String getValue(String key,String lang){
		if(properties == null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(".");
		sb.append(lang);
		String tempKey = sb.toString();
		String tempValue = properties.getProperty(tempKey,"");
		if(StringUtil.isNotBlank(tempValue))
			return tempValue;
		return getValue(key);
		
	}
}
