package com.sunbox.producer;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.dom4j.Document;
//import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sunbox.annotation.framework.Function;
import com.sunbox.annotation.framework.NoBase;
import com.sunbox.annotation.framework.NoParam;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.attribute.Rule;
import com.sunbox.exception.RuleKeyRepeatsException;
import com.sunbox.out.FeildInfo;
import com.sunbox.out.FieldType;
import com.sunbox.out.RuleInfo;
import com.sunbox.util.PackageScanner;
import com.sunbox.util.StringUtil;

public class RuleProducer {
	private static Logger logger = LoggerFactory.getLogger(RuleProducer.class);
	private static final String commom_package = "com.sunbox.common.rule";
	private static final String defaultRuleXmlPath = "ruleengine-commom.xml";
	private static Map<String,String> ruleMap = new HashMap<String,String>();
	private static RuleProducer instance;
	private RuleProducer(){}
	public static synchronized RuleProducer getInstance() {
		if (instance == null) {
			instance = new RuleProducer();
		}
		return instance;
	}

	public static boolean inntProducer(String xmlPath) throws Exception{
		inntCommomRule();
		ClassLoader classLoader = RuleProducer.class.getClassLoader();
		InputStream in = classLoader.getResourceAsStream(xmlPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		NodeList nodeList = doc.getElementsByTagName("rule-scan");
		if(nodeList != null){
			org.w3c.dom.Element scanElement = (org.w3c.dom.Element)nodeList.item(0);
			String annotationPath = scanElement.getAttribute("base-package");
			 if(StringUtil.isNotBlank(annotationPath)){
				 PackageScanner scanner = new PackageScanner(annotationPath);
				 List<String> ruleClassList = scanner.getFullyQualifiedClassNameList();
				 for(String rule :ruleClassList){
						putRuleMap(rule);
				}
			 }
		 }
		 return true;
	}
	
	
	public static boolean inntProducer() throws Exception{
		return inntProducer(defaultRuleXmlPath);
	}
	
	public static boolean inntProducer(Properties properties) throws Exception{
		String annotationPath = properties.getProperty("rule-scan");
		if(StringUtil.isNotBlank(annotationPath)){
			 PackageScanner scanner = new PackageScanner(annotationPath);
			 List<String> ruleClassList = scanner.getFullyQualifiedClassNameList();
			 for(String rule :ruleClassList){
					putRuleMap(rule);
			}
		 }
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		RuleProducer.inntProducer();
	}
	

	private static boolean notExistRule(String key){
		return !ruleMap.containsKey(key);
	}
	
	private static void inntCommomRule()throws Exception{
		PackageScanner scanner = new PackageScanner(commom_package);
		List<String> ruleClass = scanner.getFullyQualifiedClassNameList();
		for(String rule :ruleClass){
			putRuleMap(rule);
		}
	}
	
	private static void putRuleMap(String classStr)throws Exception{
		Class ruleClass = Class.forName(classStr);
		if(ruleClass.getSuperclass().equals(Rule.class)){
			String className = ((Class<Rule>)ruleClass).getSimpleName();
			String key = null;
			String funName = null;
			Function function = ((Class<Rule>)ruleClass).getAnnotation(Function.class);
			if(function != null)
				funName = function.name();
			if(StringUtil.isBlank(funName)){
				StringBuffer sb = new StringBuffer();  
				sb.append(className.substring(0, 1).toLowerCase());  
			    sb.append(className.substring(1)); 
			    key = sb.toString();
			}else{
				key = funName;
			}
			if(notExistRule(key))
				ruleMap.put(key,classStr);
			else
				throw new RuleKeyRepeatsException("rule function \""+key+"\" already exit!");
		}
	}
	
	private static void putRuleMap(String key,String classStr)throws Exception{
		Class ruleClass = Class.forName(classStr);
		if(ruleClass.getSuperclass().equals(Rule.class)){
			if(notExistRule(key))
				ruleMap.put(key,classStr);
			else
				throw new RuleKeyRepeatsException("rule function \""+key+"\" already exit!");
		}
	}
	
	private static boolean repeatClass(String className){
		return ruleMap.containsValue(className);
	}
	public static Map<String,String> getRuleMap(){
		Map<String,String> returnMap = new HashMap<String,String>();
		for(String key : ruleMap.keySet())
			returnMap.put(key, ruleMap.get(key));
		return returnMap;
	}
	
	
	public static Rule getRuleInstanceByKey(String key)throws Exception{
		String classKey = ruleMap.get(key);
		Class<Rule> ruleClass = (Class<Rule>) Class.forName(classKey);
		return ruleClass.newInstance();
	}
	
	public static String getKeyByClassName(String classStr){
		for(Entry<String, String> entry:ruleMap.entrySet()){
			if((entry.getValue()).equals(classStr)){
				return entry.getKey();
			}
		}
		return null;
	}	
	
	private static RuleInfo getFeildInfo(String ruleClassStr,String lang) throws Exception{
		Class<? extends Rule> ruleClass = (Class<? extends Rule>) Class.forName(ruleClassStr);
		Function function = ruleClass.getDeclaredAnnotation(Function.class);
		String ruleFunctionName = null;
		if(function != null)
			ruleFunctionName = function.name();
		NoParam noParam = ruleClass.getDeclaredAnnotation(NoParam.class);
		NoBase noBase = ruleClass.getDeclaredAnnotation(NoBase.class);
		String ruleClassName = ruleClass.getSimpleName();
		StringBuilder sb = new StringBuilder();
		sb.append(ruleClassName.substring(0, 1).toLowerCase());  
	    sb.append(ruleClassName.substring(1));
	    String className = (StringUtil.isNotBlank(ruleFunctionName))?ruleFunctionName:sb.toString();
	    RuleInfo ruleInfo = new RuleInfo();
	    ruleInfo.setFunction(className);
	    ruleInfo.setDesc(StringUtil.getDescription(ruleClass, lang));
	    ruleInfo.setLang(lang);
	    List<FeildInfo> feildInfoList = new ArrayList<FeildInfo>();
	    ruleInfo.setFeilds(feildInfoList);
	    for(Field field :StringUtil.getAllFields(ruleClass)){
	    	FeildInfo feildInfo = new FeildInfo();
	    	RuleParam ruleParam = field.getDeclaredAnnotation(RuleParam.class);
	    	RuleBase ruleBase = field.getDeclaredAnnotation(RuleBase.class);
	    	String fieldName = field.getName();
	    	if(ruleParam != null){
	    		if(noParam != null)
	    			continue;
	    		String fieldValue = ruleParam.value();
	    		feildInfo.setSimpleName(StringUtil.isNotBlank(fieldValue)?fieldValue:fieldName);
				StringBuilder fieldSb = new StringBuilder();
				feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
	    		feildInfo.setDesc(StringUtil.getDescription(ruleClass, field, lang));
	    		feildInfo.setLang(lang);
	    		feildInfo.setFieldType(FieldType.param);
	    		feildInfoList.add(feildInfo);
	    	}else if(ruleBase != null){
	    		if(noBase != null)
	    			continue;
	    		String fieldValue = ruleBase.value();
	    		String fieldBase = ruleBase.base();
	    		feildInfo.setSimpleName(StringUtil.isNotBlank(fieldValue)?fieldValue:fieldName);
				StringBuilder fieldSb = new StringBuilder();
				feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
	    		feildInfo.setDesc(StringUtil.getDescription(ruleClass, field, lang));
	    		feildInfo.setLang(lang);
	    		feildInfo.setFieldType(FieldType.base);
	    		feildInfo.setValue(fieldBase);
	    		feildInfoList.add(feildInfo);
	    	}else{

	    		if("base".equals(fieldName)){
	    			if(noBase == null){
	    				feildInfo.setSimpleName(fieldName);
						StringBuilder fieldSb = new StringBuilder();
						feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
	    	    		feildInfo.setDesc(StringUtil.getDescription(ruleClass, field, lang));
	    	    		feildInfo.setLang(lang);
	    	    		feildInfo.setFieldType(FieldType.base);
	    	    		feildInfoList.add(feildInfo);
	    			}
	    		}else if("param".equals(fieldName)){
					if(noParam == null) {
						feildInfo.setSimpleName(fieldName);
						StringBuilder fieldSb = new StringBuilder();
						feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
						feildInfo.setDesc(StringUtil.getDescription(ruleClass, field, lang));
						feildInfo.setLang(lang);
						feildInfo.setFieldType(FieldType.param);
						feildInfoList.add(feildInfo);
					}
	    		}
	    	}
	    }
	    return ruleInfo;
	}
	
	public static List<RuleInfo> getFeildInfo(String lang) throws Exception{
		List<RuleInfo> rules = new ArrayList<RuleInfo>();
		for(Entry<String,String> entry:ruleMap.entrySet()){
			String ruleClassStr = entry.getValue();
			if(StringUtil.isNotBlank(ruleClassStr))
				rules.add(getFeildInfo(ruleClassStr,lang));
		}
		return rules;
	}
	
	public static List<RuleInfo> getFeildInfo() throws Exception{
		return getFeildInfo("zh_cn");
	}
	
	
}
