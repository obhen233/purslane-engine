package com.sunbox.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);
	
	public static <T> List<T> deepCopy(List<T> src) {  
    	try {
	    	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  
	        ObjectOutputStream out = new ObjectOutputStream(byteOut);  
	        out.writeObject(src);  
	      
	        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  
	        ObjectInputStream in = new ObjectInputStream(byteIn);
	        
	        return (List<T>) in.readObject();   
    	} catch (Exception e) {
	        logger.error("deepCopy",e);
	    }
	    return null;
    } 
    
    
    public static  <T extends Serializable> T deepClone(T object) {
	    T temp = null;
	    try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(object);
	        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
	        ObjectInputStream ois = new ObjectInputStream(bis);
	        temp = (T) ois.readObject();
	    } catch (Exception e) {
	        logger.error("deepClone",e);
	    }
	    return temp;
	} 
}
