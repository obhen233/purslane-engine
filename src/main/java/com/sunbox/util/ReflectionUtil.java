package com.sunbox.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sunbox.exception.FeidNofindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionUtil {
	 
	private static Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);
	 
	 public static Method getGetMethod(Class objectClass, String fieldName) {  
	       StringBuffer sb = new StringBuffer();  
	       sb.append("get");  
	       sb.append(fieldName.substring(0, 1).toUpperCase());  
	       sb.append(fieldName.substring(1));  
	       try {  
	           return objectClass.getMethod(sb.toString());  
	       } catch (Exception e) { 
	    	   logger.error("getGetMethod",e);
	       }  
	       return null;  
	  }  
	 
	 public static Method getSetMethod(Class objectClass, String fieldName) {  
        try {  
            Class[] parameterTypes = new Class[1];  
            Field field = null;
            try{
            	field = objectClass.getDeclaredField(fieldName);
            }catch(Exception ex){
            	logger.error("getSetMethod",ex); 
            }
            while(field == null && objectClass != null){	
            	objectClass = objectClass.getSuperclass();
            	field = objectClass.getDeclaredField(fieldName);
            }
            if(field == null)
                throw new FeidNofindException(objectClass+"no find field "+fieldName);
            parameterTypes[0] = field.getType();  
            StringBuffer sb = new StringBuffer();  
            sb.append("set");  
            sb.append(fieldName.substring(0, 1).toUpperCase());  
            sb.append(fieldName.substring(1));  
            Method method = objectClass.getMethod(sb.toString(), parameterTypes);  
            return method;  
        } catch (Exception e) {  
        	logger.error("getSetMethod",e); 
        }  
        return null;  
	 }
	 
	 
	 public static void invokeSet(Object o, String fieldName, Object value) {  
        Method method = getSetMethod(o.getClass(), fieldName);  
        try {  
            method.invoke(o, new Object[] { value });  
        } catch (Exception e) {  
        	logger.error("invokeSet",e); 
        }  
	  } 
	 
	 public static Object invokeGet(Object o, String fieldName) {  
        Method method = getGetMethod(o.getClass(), fieldName);  
        try {  
            return method.invoke(o, new Object[0]);  
        } catch (Exception e) {  
        	logger.error("invokeGet",e);
        }  
        return null;  
	 }
	
}
