package com.github.obhen233.engine;

import java.util.Map;

import com.github.obhen233.util.ExpressionUtil;
import com.github.obhen233.util.RootKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.obhen233.element.Root;
import com.github.obhen233.producer.RootProducer;

public class Engine {
	
	private static Logger logger = LoggerFactory.getLogger(Engine.class);
	
	public static Object doExcute(Map<String,Object> param,String unid) throws Exception{
		String key = RootKeyUtil.getkeyFromMap(param, unid);
		Root root = RootProducer.getRootByKey(key);
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
