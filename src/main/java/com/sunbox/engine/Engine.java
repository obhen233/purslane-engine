package com.sunbox.engine;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunbox.element.Root;
import com.sunbox.exception.NoFindRootException;
import com.sunbox.exception.NotFindParamException;
import com.sunbox.producer.RootProducer;
import com.sunbox.util.ExpressionUtil;
import com.sunbox.util.RootKeyUtil;

public class Engine {
	
	private static Logger logger = LoggerFactory.getLogger(Engine.class);
	
	public static Object doExcute(Map<String,Object> param,String unid) throws Exception{
		String key = RootKeyUtil.getkeyFromMap(param, unid);
		Root root = RootProducer.getInstance().getRootByKey(key);
		RootKeyUtil.setRootFields(root,param);
		return root.excute().getFormatMsg();
		
	}
	
	public static Object doExcute(Object obj,Map<String,Object> param) throws Exception{
		return doExcute(obj.getClass(),param);
	}
	
	public static Object doExcute(Class clazz,Map<String,Object> param) throws Exception{
		Root root = ExpressionUtil.expression2Root(clazz);
		RootKeyUtil.setRootFields(root,param);
		return root.excute().getFormatMsg();
	}
	
	public static Object  doExcute(String expression,Map<String,Object> param) throws Exception{
		Root root = ExpressionUtil.expression2Root(expression);
		RootKeyUtil.setRootFields(root,param);
		return root.excute().getFormatMsg();
	}
	
}
