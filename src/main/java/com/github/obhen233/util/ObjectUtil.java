package com.github.obhen233.util;

import java.io.*;
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


	public static byte[] toByteArray (Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray ();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			logger.error("toByteArray",ex);
		}
		return bytes;
	}


	public static Object toObject (byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			logger.error("toObject",ex);
		} catch (ClassNotFoundException ex) {
			logger.error("toObject",ex);
		}
		return obj;
	}
}
