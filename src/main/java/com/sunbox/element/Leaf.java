package com.sunbox.element;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunbox.annotation.framework.Function;
import com.sunbox.annotation.framework.NoBase;
import com.sunbox.annotation.framework.NoParam;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.attribute.Nrule;
import com.sunbox.attribute.Rule;
import com.sunbox.exception.ClassUnregisteredException;
import com.sunbox.producer.RuleProducer;
import com.sunbox.result.ExcuteResult;
import com.sunbox.util.ReflectionUtil;
import com.sunbox.util.StringUtil;

//A Leaf mast belong to a Node,and it has none child Node or Leaf,but it have Rules,and all Rules join with "AND".
//叶子节点，没有任何的子节点，规则只在叶子节点上，所有的规则是"与"的关系。
public class Leaf implements Element,Serializable {
	
	private Logger logger = LoggerFactory.getLogger(Leaf.class);
	private List<Rule> rules;
	
	private boolean result = false;
	
	
	
	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
	
	
	
	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void setRules(List<Rule> rules,boolean result) {
		this.rules = rules;
		this.result = result;
	}
	
	public Leaf(){};
	
	public Leaf(List<Rule> rules){
		this.rules = rules;
	}

	@Override
	public ExcuteResult excute() {
		ExcuteResult excuteResult = new ExcuteResult();
		List<Rule> rs = this.rules;
		if(rs == null || rs.size() <= 0){
			excuteResult.setResult(result);
			excuteResult.setFormatMsg("[]");
			return excuteResult;
		}
			
		for(Rule r:rs){
			if(!r.excute()){
				excuteResult.setResult(false);
				excuteResult.setFormatMsg(excuteMsg(r));
				return excuteResult;
			}
		}
		excuteResult.setResult(true);
		excuteResult.setFormatMsg("SUCCESS!");
		return excuteResult;
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Rule rule:rules)
			formartRule(rule, sb);
		sb.append("]");
		return sb.toString();
	}
	
	private void formartRule(Rule rule,StringBuilder sb){
		if(sb.length()> 1) sb.append(",");
		if(Nrule.class.isInstance(rule)){
			sb.append("!");
			rule = ((Nrule)rule).getRule();
		}
		sb.append("$");
		String ruleKey = RuleProducer.getKeyByClassName(rule.getClass().getName());
		if(StringUtil.isBlank(ruleKey))
			throw new ClassUnregisteredException(rule.getClass().getName() + " Unregistered.");
		sb.append(ruleKey);
			/*
		Function function = rule.getClass().getAnnotation(Function.class);
		String name = rule.getClass().getSimpleName();
		if(function != null && StringUtil.isNotBlank(function.name())){
			sb.append(function.name());
		}else{
			sb.append(name.substring(0, 1).toLowerCase());  
		    sb.append(name.substring(1));
		}
		*/
		NoParam noParam = rule.getClass().getAnnotation(NoParam.class);
		NoBase noBase = rule.getClass().getAnnotation(NoBase.class);
		Field[] fields = StringUtil.getAllFields(rule.getClass());
		List<Map<String,String>> baseList = new ArrayList<Map<String,String>>();
		List<Map<String,String>> paramList = new ArrayList<Map<String,String>>();
		for(Field field:fields){
			field.setAccessible(true);
			if(noParam == null){
				RuleParam ruleParam = field.getAnnotation(RuleParam.class);
				if(ruleParam != null){
					String value = ruleParam.value();
					String fieldName = field.getName();
					Map<String,String> m = new HashMap<String,String>();
					m.put("value", value);
					m.put("key", fieldName);
					paramList.add(m);
				}
			}
			if(noBase == null){
				RuleBase ruleBase = field.getAnnotation(RuleBase.class);
				if(ruleBase != null){
					String value = ruleBase.value();
					String baseName = field.getName();
					Map<String,String> m = new HashMap<String,String>();
					m.put("key", StringUtil.isNotBlank(value)?value:baseName);
					m.put("value", StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule, baseName),field));
					baseList.add(m);
				}
			}
		}
		if(baseList.size() <= 0 && noBase == null){
			Map<String,String> m = new HashMap<String,String>();
			m.put("key", "base");
			m.put("value", (String)ReflectionUtil.invokeGet(rule, "base"));
		}
		if(baseList.size() <= 0 && noBase == null){
			Map<String,String> m = new HashMap<String,String>();
			m.put("key", "base");
			m.put("value", (String)ReflectionUtil.invokeGet(rule, "base"));
			baseList.add(m);
		}
		if(paramList.size() <= 0 && noParam == null){
			Map<String,String> m = new HashMap<String,String>();
			m.put("key", "param");
			m.put("value", (String)ReflectionUtil.invokeGet(rule, "param"));
			paramList.add(m);
		}
		//if(baseList.size() > 0 && paramList.size() > 0)
			sb.append("(");
		if(paramList.size() > 0 && !"param".equals(paramList.get(0).get("name"))){
			for(Map<String,String> m :paramList){
				String key = m.get("key");
				if(StringUtil.isNotBlank(key) && !"param".equals(key)){
					sb.append("#");
					sb.append(key);
					sb.append("#");
				}
			}
		}
		if(baseList.size() > 0){
			if(paramList.size() > 0  && !("param".equals(paramList.get(0).get("key")))) sb.append(",");
			for(int i = 0;i<baseList.size();i++){
				if(i>0)sb.append(",");
				sb.append("{").append("base".equals(baseList.get(i).get("key"))?"":baseList.get(i).get("key")).append("base".equals(baseList.get(i).get("key"))?"":":").append(StringUtil.isNotBlank(baseList.get(i).get("value")) ?baseList.get(i).get("value"):"").append("}");
			}
		}else{
			if(noBase == null){
				String baseVal = null;
				try {
					baseVal = StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule.getClass(), "base"),rule.getClass().getDeclaredField("base"));
				} catch (NoSuchFieldException e) {
					logger.error("NoSuchFieldException", e);
				} catch (SecurityException e) {
					logger.error("SecurityException", e);
				}
				if(StringUtil.isNotBlank(baseVal)){
					if(paramList.size() > 0 && !("param".equals(paramList.get(0).get("key")))) sb.append(",");
					sb.append("{").append(baseVal).append("}");
				}
			}
		}
		//if(baseList.size() > 0 && paramList.size() > 0)
			sb.append(")");
		
	}
	
	private Object excuteMsg(Rule rule) {
		
		
		if(Nrule.class.equals(rule.getClass())){
			Rule e = ((Nrule)rule).getRule();
			Object excuteMsg = e.excuteMsg();
			if(excuteMsg != null && StringUtil.isNotBlank(excuteMsg.toString()))
				return excuteMsg;
		}
		Object excuteMsg = rule.excuteMsg();
		if(excuteMsg != null && StringUtil.isNotBlank(excuteMsg.toString()))
			return excuteMsg;
		
		Class<Rule> ruleClass = (Class<Rule>) rule.getClass();
		StringBuffer sb = new StringBuffer();  
		String funcName = null;
		sb.append("function :");
		if(ruleClass.equals(Nrule.class)){
			sb.append("!");
			rule = ((Nrule)rule).getRule();
			ruleClass = (Class<Rule>) rule.getClass();
		}
		sb.append("$");
		Function function = ruleClass.getAnnotation(Function.class);
		String ruleName = ruleClass.getSimpleName();
		if(function != null){
			funcName = function.name();
			if(StringUtil.isNotBlank(funcName)){
				sb.append(funcName);
			}
		}
		if(StringUtil.isBlank(funcName)){
			sb.append(ruleName.substring(0, 1).toLowerCase());  
		    sb.append(ruleName.substring(1));
		}
		sb.append("(");
		NoParam noParam =  ruleClass.getDeclaredAnnotation(NoParam.class);
		NoBase noBase =  ruleClass.getDeclaredAnnotation(NoBase.class);
		Field[] fields = StringUtil.getAllFields(ruleClass);
		List<Map<String,String>> baseList = new ArrayList<Map<String,String>>();
		List<Map<String,String>> paramList = new ArrayList<Map<String,String>>();
		for(Field field:fields){
			field.setAccessible(true);
			if(noParam == null){
				RuleParam ruleParam = field.getAnnotation(RuleParam.class);
				if(ruleParam != null){
					String value = ruleParam.value();
					String fieldName = field.getName();
					Map<String,String> m = new HashMap<String,String>();
					m.put("value", value);
					m.put("key", fieldName);
					paramList.add(m);
				}
			}
			if(noBase == null){
				RuleBase ruleBase = field.getAnnotation(RuleBase.class);
				if(ruleBase != null){
					String value = ruleBase.value();
					String base = ruleBase.base();
					String baseName = field.getName();
					Map<String,String> m = new HashMap<String,String>();
					m.put("value", value);
					m.put("key", baseName);
					m.put("base", StringUtil.isNotBlank(base)?base:StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule, baseName),field));
					baseList.add(m);
				}
			}
		}
		if(paramList.size() > 0){
			for(int i = 0;i<paramList.size();i++){
				if(i > 0)sb.append(",");
				Map<String,String> m = paramList.get(i);
				sb.append("#");
				sb.append(StringUtil.isNotBlank(m.get("value"))?m.get("value"):m.get("key"));
				sb.append("#");
			}
		}
		if(baseList.size() > 0){
			if(paramList.size() > 0) sb.append(",");
			if(baseList.size() <= 0 &&  noBase == null){
				sb.append("{");
				sb.append((String)ReflectionUtil.invokeGet(rule, "base"));
				sb.append("}");
			}else{
				for(int i = 0;i<baseList.size();i++){
					if(i>0)sb.append(",");
					sb.append("{");
					Map<String,String> m = baseList.get(i);
					sb.append(StringUtil.isNotBlank(m.get("value"))?m.get("value"):m.get("key"));
					sb.append(":");
					sb.append(m.get("base"));
					sb.append("}");
				}
			}
		}else{
			String baseVal = null;
			try {
				baseVal = StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule, "base"),rule.getClass().getDeclaredField("base"));
			} catch (NoSuchFieldException e) {
				logger.error("NoSuchFieldException", e);
			} catch (SecurityException e) {
				logger.error("SecurityException", e);
			}
			if(StringUtil.isNotBlank(baseVal)){
				if(paramList.size() > 0) sb.append(",");
				sb.append("{").append(baseVal).append("}");
			}
		}
		if(baseList.size() > 0 && paramList.size() > 0)
		sb.append(")");
		sb.append(" excute false,");
		if(paramList.size() > 0){
			sb.append(" param is:"); 
			for(int i = 0;i<paramList.size();i++){
				if(i > 0)sb.append(",");
				Map<String,String> m = paramList.get(i);
				String paramVal = null;
				try {
					paramVal = StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule, m.get("key")),rule.getClass().getDeclaredField( m.get("key")));
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(StringUtil.isNotBlank(paramVal)?paramVal:"null");
			}
		}else{
			if(noBase == null){
				sb.append(" param is:"); 
				String paramVal = null;
				try {
					paramVal = StringUtil.formatFieldValue(ReflectionUtil.invokeGet(rule, "param"),rule.getClass().getDeclaredField("param"));
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(StringUtil.isNotBlank(paramVal)?paramVal:"null");
			}else{
				sb.append(" the function is none param");
			}
		}
		sb.append(".");
		return sb.toString();
		
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (!(obj instanceof Leaf)) { 
	         return false; 
	     }
		 return this.toString().equals(obj.toString());
	}
	

}

