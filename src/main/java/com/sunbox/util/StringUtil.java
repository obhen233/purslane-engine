package com.sunbox.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunbox.annotation.framework.DateFormat;
import com.sunbox.annotation.framework.Description;
import com.sunbox.annotation.framework.Function;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.exception.ExpressionFormatException;
import com.sunbox.formator.Formator;

public class StringUtil {
	
	private static Logger logger = LoggerFactory.getLogger(StringUtil.class);
	
	private static final String[] baseDataTypes = {"java.lang.String","int","java.lang.Integer","long","java.lang.Long",
			"float","java.lang.Float","boolean","java.lang.Boolean","double","java.lang.Double","short","java.lang.Short"
			,"java.math.BigDecimal"};
		
	public static Object StringToField(String value,Field field){
		field.setAccessible(true);
		String clazz = field.getGenericType().getTypeName();
		if(value == null || "".equals(value))
			return null;
		if("java.lang.String".equals(clazz) || "java.lang.Object".equals(clazz))
			return value;
		if("int".equals(clazz) || "java.lang.Integer".equals(clazz))
			return (Integer.parseInt(value));
		if("long".equals(clazz) || "java.lang.Long".equals(clazz))
			return Long.parseLong(value);
		if("float".equals(clazz) || "java.lang.Float".equals(clazz))
			return Float.parseFloat(value);
		if("boolean".equals(clazz) || "java.lang.Boolean".equals(clazz))
			return Boolean.parseBoolean(value);
		if("double".equals(clazz) || "java.lang.Double".equals(clazz))
			return Double.parseDouble(value);
		if("short".equals(clazz) || "java.lang.Short".equals(clazz))
			return Short.parseShort(value);
		if("java.math.BigDecimal".equals(clazz))
			return new BigDecimal(value);
		if("java.util.Date".equals(clazz)){
			DateFormat dateFormat = field.getAnnotation(DateFormat.class);
			String format = "yyyy-MM-dd HH:mm:ss";
			if(dateFormat != null) {
				if (StringUtil.isNotBlank(dateFormat.value())) {
					format = dateFormat.value();
				}
			}
			try {
				return new SimpleDateFormat(format).parse(value);
			} catch (ParseException e) {
				logger.error("StringToField",e);
				return null;
			}
		}
		if(isGenericTypeUtilMap(field)){
			return parseMap(value,field);
		}
		if(isGenericTypeArray(field)){
			return parseArray(value,field);
		}
		if(isGenericTypeCollection(field)){
			return parseList(value,field);
		}
		com.sunbox.annotation.framework.Formator formatorAnno = field.getAnnotation(com.sunbox.annotation.framework.Formator.class);
		if(formatorAnno != null){
			try {
				Formator formator = (formatorAnno.value()).newInstance();
				if(containKeyWord(value))
					throw new ExpressionFormatException("the format value contains [ ] $ # ( ) { } :"+value);
				return formator.parse(value);
			} catch (Exception e) {
				logger.error("StringToField",e);
			}
		}
		return value;
	}
	
	
	public static String formatFieldValue(Object value,Field field){
		field.setAccessible(true);
		String clazz = field.getGenericType().getTypeName();
		if(value == null || "".equals(value))
			return null;
		if("java.util.Date".equals(clazz)){
			DateFormat dateFormat = field.getAnnotation(DateFormat.class);
			String format = "yyyy-MM-dd HH:mm:ss";
			if(dateFormat != null) {
				if (StringUtil.isNotBlank(dateFormat.value())) {
					format = dateFormat.value();
				}
			}
			return new SimpleDateFormat(format).format(value);
		}
		if(isGenericTypeUtilMap(field)){
			return formatMap((Map)value);
		}
		if(isGenericTypeArray(field)){
			return formatList((Object[])value);
		}
		if(isGenericTypeCollection(field)){
			return formatList((Collection)value);
		}
		com.sunbox.annotation.framework.Formator formatorAnno = field.getAnnotation(com.sunbox.annotation.framework.Formator.class);
		if(formatorAnno != null){
			try {
				Formator formator = (formatorAnno.value()).newInstance();
				String text =  formator.format(value);
				if(containKeyWord(text))
					throw new ExpressionFormatException("the format value contains [ ] $ # ( ) { } :"+value);
				return text;
			} catch (Exception e) {
				logger.error("StringToField",e);
			}
		}
		return value.toString();
	}
	
	public static String formatMap(Map map){
		StringBuilder sb = new StringBuilder();
		for(Object key:map.entrySet()){
			if(isBaseDataType(key)){
				sb.append(sb.length() > 0 ? ",":"");
				sb.append(key.toString());
				sb.append("|");
				sb.append(map.get(key) == null ? "":map.get(key).toString());
			}
		}
		return sb.toString();
	}
	//text: key1:value1,key2:value2
	public static Map parseMap(String text,Field field){
		field.setAccessible(true);
		String typeName = field.getGenericType().getTypeName();
		try {
			String keyClassStr = "java.lang.String";
			String valClassStr = "java.lang.Object";
			try{
				if(ParameterizedType.class.isInstance(field.getGenericType())){
					ParameterizedType type = (ParameterizedType)field.getGenericType();
					Type[] arguments =  type.getActualTypeArguments();
					if(arguments.length >1){
						keyClassStr = arguments[0].getTypeName();
						valClassStr = arguments[1].getTypeName();
					}
				}	
			}catch(ClassCastException e1){
				logger.info("parseMap#1",e1);
			}
			Class keyClass = Class.forName(keyClassStr);
			Class valClass = Class.forName(valClassStr);
			Map instance = null;
			try{
				instance = (Map)Class.forName(typeName).newInstance();
			}catch(Exception e){
				logger.info("parseMap#2",e);
				instance = new HashMap<String,Object>();
			}
			if(isBlank(text))
				return instance;
			List<String> baseList = Arrays.asList(baseDataTypes);
			if(baseList.contains(keyClassStr) && baseList.contains(valClassStr)){
				String[] ts = text.split(",");
				for(String t :ts){
					if(isNotBlank(t)){
						Object k = null;
						Object v = null;
						String[] kv = t.split("//|");
						if(kv != null && kv.length > 0){
							if(kv.length > 1){
								 k = formatBaseDataType(keyClassStr,kv[0]);
								 v = formatBaseDataType(keyClassStr,kv[1]);
							}else{
								if(t.endsWith("//|") && kv.length <= 1){
									 k = formatBaseDataType(keyClassStr,kv[0]);
									 v = null;
								}
							}
								
						}
						if(k != null){
							instance.put(k, v);
						}
					}
				}
				return instance;
			}else{
				throw new RuntimeException("Unsupport data type:"+ keyClassStr + " or " + valClassStr);
			}
		
			
		} catch (ClassNotFoundException e) {
			logger.error("parseMap#0",e);
			return null;
		}
		
		
	}
	
	private static Object formatBaseDataType(String clazz,String value){
		if(value == null || "".equals(value))
			return null;
		if("java.lang.String".equals(clazz) || "java.lang.Object".equals(clazz))
			return value;
		if("int".equals(clazz) || "java.lang.Integer".equals(clazz))
			return (Integer.parseInt(value));
		if("long".equals(clazz) || "java.lang.Long".equals(clazz))
			return Long.parseLong(value);
		if("float".equals(clazz) || "java.lang.Float".equals(clazz))
			return Float.parseFloat(value);
		if("boolean".equals(clazz) || "java.lang.Boolean".equals(clazz))
			return Boolean.parseBoolean(value);
		if("double".equals(clazz) || "java.lang.Double".equals(clazz))
			return Double.parseDouble(value);
		if("short".equals(clazz) || "java.lang.Short".equals(clazz))
			return Short.parseShort(value);
		if("java.math.BigDecimal".equals(clazz))
			return new BigDecimal(value);
		return value;
	}
	
	private static boolean isGenericTypeUtilMap(Field field){
		String typeName = field.getGenericType().getTypeName();
		String[] types = typeName.split("<");
		if(types.length > 1) typeName = types[0];
		return typeName.startsWith("java.util") && typeName.endsWith("Map");
	}
	private static boolean isGenericTypeArray(Field field){
		String typeName = field.getGenericType().getTypeName();
		return typeName.endsWith("[]");
	}
	private static boolean isGenericTypeCollection(Field field){
		field.setAccessible(true);
		String typeName = field.getGenericType().getTypeName();
		String[] types = typeName.split("<");
		if(types.length > 1) typeName = types[0];
		//Class typeClass = Class.forName(typeName).getSuperclass();
		return typeName.startsWith("java.util.") && (typeName.endsWith("List") || typeName.endsWith("Set"));
	}
	
	public static String formatList(Collection list){
		StringBuilder sb = new StringBuilder();
		for(Object key:list){
			if(isBaseDataType(key)){
				sb.append(sb.length() > 0 ? ",":"");
				sb.append(key == null ? "":key.toString());
			}
		}
		return sb.toString();
	}
	public static String formatList(Object[] o){
		return formatList(Arrays.asList(o));
	}
	private static Collection parseList(String text,Field field){
		String classStr = "java.lang.String";
		try{
			if(ParameterizedType.class.isInstance(field.getGenericType())){
				ParameterizedType type = (ParameterizedType)field.getGenericType();
				Type[] arguments =  type.getActualTypeArguments();
				if(arguments!= null && arguments.length >1){
					classStr = arguments[0].getTypeName();
				}
			}
		}catch(ClassCastException e1){
			e1.getStackTrace();
			logger.info("parseMap#1",e1);
		}
		Type type = field.getGenericType();
		int parameterIndex = type.getTypeName().indexOf("<");
		String typeName = type.getTypeName();
		if(parameterIndex != -1)
			typeName = typeName.substring(0,parameterIndex);
		if(typeName.endsWith("Set")){
			Set instance = null;
			try {
				instance = (Set)Class.forName(type.getTypeName()).newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.info("parseMap#2",e);
				instance  = new HashSet();
			}
			if(isBlank(text))
				return instance;
			String[] es = text.split(",");
			for(String e:es){
				Object o = formatBaseDataType(classStr,e);
				instance.add(o);
			}
			return instance;
		}
		
		if(typeName.endsWith("List")){
			List instance = null;
			try {
				instance = (List)Class.forName(type.getTypeName()).newInstance();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.getStackTrace();
				logger.info("parseMap#2",e);
				instance  = new ArrayList();
			}
			if(isBlank(text))
				return instance;
			String[] es = text.split(",");
			for(String e:es){
				Object o = formatBaseDataType(classStr,e);
				instance.add(o);
			}
			return instance;
		}
		throw new RuntimeException(field.getGenericType().getTypeName() + "sames not list or set.");
	}
	
	private static Object[] parseArray(String text,Field field){
		String typeName = field.getGenericType().getTypeName();
		String[] types = typeName.split("<");
		if(types.length > 1) typeName = types[0];
		typeName = typeName.replace("[]", "");
		if(isBlank(text))
			return new ArrayList().toArray();
		String[] as = text.split(",");
		ArrayList list = new ArrayList();
		for(String a :as){
			list.add(formatBaseDataType(typeName,a));
		}
		return list.toArray();
	}
	
	private static boolean isBaseDataType(Object o){	
		if(o == null)
			throw new RuntimeException("Unsupport data type:null");
		String name = o.getClass().getName();
		List<String> baseList = Arrays.asList(baseDataTypes);
		if(baseList.contains(name)){
			return true;
		}else{
			throw new RuntimeException("Unsupport data type:"+name);
		}
	}
	
	
	public static Object StringToField(Class clazz,String value,String name){
		try{
			Field field = clazz.getDeclaredField(name);
			return StringToField( value, field);
		}catch(Exception e){
			logger.error("StringToField",e);
			return null;
		}
	}
	
	
	public static boolean isNotBlank(String value){
		 return !(value == null || value.trim().length() == 0);
	}
	
	public static boolean isBlank(String value){
		return value == null || value.trim().length() == 0;
	}
	
	public static boolean isEmpty(String string) {
		return string==null || "".equals(string.trim());
	}
	
	
	
    
    public static String getRootPath(URL url) {
        String fileUrl = url.getFile();
        int pos = fileUrl.indexOf('!');

        if (-1 == pos) {
            return fileUrl;
        }

        return fileUrl.substring(5, pos);
    }

    
    public static String dotToSplash(String name) {
        return name.replaceAll("\\.", "/");
    }
    
 
    public static String splashToDot(String name) {
        return name.replaceAll("/","\\.");
    }

   
    public static String trimExtension(String name) {
        int pos = name.lastIndexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }

        return name;
    }

   
    public static String trimURI(String uri) {
        String trimmed = uri.substring(1);
        int splashIndex = trimmed.indexOf('/');

        return trimmed.substring(splashIndex);
    }

    
    //[ ] $ # ( ) { } 
    public static boolean containKeyWord(String content){
    	String regEx = "[\\[\\]\\$\\#\\(\\)\\{\\}]+";
    	Pattern p = Pattern.compile(regEx);
    	Matcher m = p.matcher(content);
    	return m.find();
    }
    
    public static String getDescription(Field field){
    	return getDescription(field,"zh_cn");
    }
    
    public static String getDescription(Field field,String lang){
    	Description desc = field.getDeclaredAnnotation(Description.class);
    	if(lang.equals(desc.lang()))
    		return desc.desc();
    	else
    		return "";
    }
    
    public static String getDescription(Class clazz){
    	return getDescription(clazz,null,"zh_cn");
    }
    
    public static String getDescription(Class clazz,String lang){
    	return getDescription(clazz,null,lang);
    }
    
    public static String getDescription(Class clazz,Field field,String lang){
    	Description desc = null;
    	if(field == null){
    		desc = (Description) clazz.getDeclaredAnnotation(Description.class);
    	}else{
    		desc = (Description) field.getDeclaredAnnotation(Description.class);
    	}
    	if(desc != null && lang.equals(desc.lang()))
    		return desc.desc();
    	else{
    		Function classFunction = (Function) clazz.getDeclaredAnnotation(Function.class);
    		String classFunctionName = null;
    		if(classFunction != null){
    			classFunctionName = classFunction.name();
    		}
    		String classSimpleName = clazz.getSimpleName();
    		StringBuilder sb = new StringBuilder();
    		sb.append(classSimpleName.substring(0, 1).toLowerCase());  
		    sb.append(classSimpleName.substring(1));
		    String className = (StringUtil.isNotBlank(classFunctionName))?classFunctionName:sb.toString();
		    String fieldName = null;
		    if(field != null){
			    String fieldValue = null;
			    RuleBase ruleBase = field.getAnnotation(RuleBase.class);
			    RuleParam ruleParam = field.getAnnotation(RuleParam.class);
			    if(ruleBase != null){
			    	fieldValue = ruleBase.value();
			    }else if(ruleParam != null){
			    	fieldValue = ruleParam.value();
			    }
			    fieldName = StringUtil.isNotBlank(fieldValue)?fieldValue:field.getName();
		    }
		    StringBuilder keySb = new StringBuilder();
		    keySb.append(className).append(field == null ?"":".").append(field == null?"":fieldName);
		    String key = keySb.toString();
		    return I18nUtil.getValue(key, lang);
    	}
    }
    
    public static String getDescription(Class clazz,Field field){
    	return getDescription( clazz, field,"zh_cn");
    }
    
    public static Field[] getAllFields(Class tempClass){
    	List<Field> fieldList = new ArrayList<Field>() ;
    	while (tempClass != null) {
    	      fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
    	      tempClass = tempClass.getSuperclass(); 
    	}
    	Field[] fields = new Field[fieldList.size()];
    	return fieldList.toArray(fields);
    }
    
   
}
