package com.sunbox.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunbox.annotation.framework.NoBase;
import com.sunbox.annotation.framework.NoParam;
import com.sunbox.annotation.framework.RuleBase;
import com.sunbox.annotation.framework.RuleParam;
import com.sunbox.attribute.Nrule;
import com.sunbox.attribute.Rule;
import com.sunbox.element.Bud;
import com.sunbox.element.Element;
import com.sunbox.element.Leaf;
import com.sunbox.element.Node;
import com.sunbox.element.Root;
import com.sunbox.exception.ExpressionFormatException;
import com.sunbox.producer.RuleProducer;



/**
 * 1.元素：一组[]表示是元素,元素分为Node和Leaf Node是包含元素的单元，可以包含Node,也可以包含Leaf.Leaf 下面只能包含函数
 * 2.规则：元素与元素之间的关系是"与"或者"或"，做以下限定 如果一个元素完全是由Node组成，那么这些Node之间的关系是"或"，
 *         如果本元素既有Node又有Leaf，或者完全是Leaf 那么这些元素之间的关系是"与",Leaf下函数之间的关系是"与".
 * 3.特殊元素：Bud 是一个只会返回True的Leaf 表示成[] 这个是为了处理元素内的完全是由Node组成， 但是Node之间又必须用"与"相连的情况，
 *         root 元素是根元素，所有元素的起点，root 元素有 一个 unid 表示唯一root
 * 4.函数：$加函数名称表示一个函数，函数的实现是由第三方语言实现
 * 5. 函数参数：分为参数值与基准值，参数值在前，基准值在后，参数值由##把key包裹，调用之时用方法名.参数的方式调用.值与值之间用,隔开 如果
 * 			没有参数或使用默认参数，参数值请置空。当没有参数的时候，需要在对应的Rule上用@NoParam注解 默认参数是param 如果有新增维护的
 * 			字段请用@RuleParam注解value是本字段对应表达式的参数，默认是字段名称，请实现get,set方法 。
 *            基准值由{}包裹，{key:value}形式，参数与基准值有两个默认参数可以不加，如果参数 不加的话，默认使用基准值，有且仅有一个,如果只有key 冒号不能省略。
 * 6.参数使用：正常传值，列表或者是数组，用逗号隔开，字典值用| 每个值之间用逗号隔开
 * 7.@format格式化完成不允许出现的参数有 [ ] $ # ( ) { } 
 * 8. 对于冒号 : 冒号只对时间格式做了兼容，如果没有自定义参数，格式化时间里面带有冒号，必须前面加上： 例：{:2019-08-17 00:00:00}
 * **/


public class ExpressionUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);
	static{
		try {
			RuleProducer.inntProducer();
		} catch (Exception e) {
			logger.error("innt rule error.",e);
		}
	}

	
	public static boolean testExpression(String expression) throws Exception{
		Node node = formatNode(expression);
		return node != null;
	}
	
	public static Root  expression2Root(String expression) throws Exception{
		
		Node node = formatNode(expression);
		Root root = new Root();
		if(node == null)
			throw new ExpressionFormatException("expression exchange error");
		List<Element> elements = node.getElements();
		if(elements != null && elements.size() > 0){
			root.setElements(elements);
		}else{
			throw new ExpressionFormatException("expression exchange error");
		}
		return root;	
	}
	
	private static String formatErrorExp(String expression){
		int firstIndex = indexOfAlphabet(expression);
		int lastIndex = lastIndexOfAlphabet(expression);
		if(firstIndex == -1 || lastIndex == -1)
			return "";
		return expression.substring(firstIndex,lastIndex+1);	
	}
	
	private static int indexOfAlphabet(String s){
		if(s == null || s.length() <= 0)
			return -1;
		for(int i = 0;i<s.length();i++){
			char c = s.charAt(i);
			if(('A'< c && 'Z' > c)||('a'< c && 'z' > c) ||('0'<c && '9'>c)){
				return i;
			}
		}
		return -1;
	}
	private static int lastIndexOfAlphabet(String s){
		if(s == null || s.length() <= 0)
			return -1;
		for(int i = 0;i<s.length();i++){
			int index = (s.length()-1)-i;
			char c = s.charAt(index);
			if(('A'< c && 'Z' > c)||('a'< c && 'z' > c) ||('0'<c && '9'>c)){
				return index;
			}
		}
		return -1;
	}
	//关键字 [ ] $ # ( ) { } :
	private static Leaf formatLeaf(String leafStr) throws Exception{
		Leaf leaf = new Leaf();
		
		if(StringUtil.isNotBlank(leafStr)){
			if(leafStr.startsWith("[") && leafStr.endsWith("]")){
				List<Rule> rules = new ArrayList<Rule>();
				String rulesStr =  leafStr.substring(1,leafStr.length()-1);
				while(true){
					if(StringUtil.isBlank(rulesStr)){
						break;
					}
					int index = rulesStr.indexOf(")");
					if( index != -1){
						String rulestr = rulesStr.substring(0,index+1);
						if(!StringUtil.isBlank(rulestr)){
							int funStartIndex = rulestr.indexOf("$");
							boolean isNot = false;
							if(funStartIndex != 0 && funStartIndex != -1){
								String exclStr = rulestr.substring(funStartIndex-1,funStartIndex);
								isNot = "!".equals(exclStr);
							}
							int funEndIndex = rulestr.indexOf("(");
							if(funStartIndex != -1 && funEndIndex != -1){
								String funcStr = rulestr.substring(funStartIndex+1,funEndIndex);
								Rule rule = RuleProducer.getRuleInstanceByKey(funcStr);
								if(rule == null)
									throw new ExpressionFormatException("innt Rule instance error "+funcStr);
								if(rulestr.contains("#")){
									NoParam noParam = rule.getClass().getAnnotation(NoParam.class);
									if(noParam != null)
										throw new ExpressionFormatException("this is a no param function "+funcStr);
									int lastPoundIndex = rulestr.lastIndexOf("#");
									int firstPoundIndex = rulestr.indexOf("#");
									if(lastPoundIndex != firstPoundIndex){
										String targets = rulestr.substring(firstPoundIndex,lastPoundIndex+1);
										if("##".endsWith(targets))
											throw new ExpressionFormatException("param is null:"+rulestr);
										checkTarget(targets,rule);		
									}else{
										throw new ExpressionFormatException("only have one # :"+rulestr);
									}
								}
								int leftIndexBrace = rulesStr.indexOf("{");
								int reghtIndexBrace = rulesStr.indexOf(")")-1;
								if(leftIndexBrace != -1 && reghtIndexBrace != -1){
									NoBase noBase = rule.getClass().getAnnotation(NoBase.class);
									if(noBase != null)
										throw new ExpressionFormatException("this is a no base function "+funcStr);
									 String baseStr = rulesStr.substring(leftIndexBrace,reghtIndexBrace+1);
									 setBase(baseStr,rule);
								}
								rules.add(ObjectUtil.deepClone(isNot?new Nrule(rule):rule));
							}else{
								throw new ExpressionFormatException("function name is illegal :"+rulestr);
							}
							rulesStr =  rulesStr.substring(index+1,rulesStr.length());
						}else{
							throw new ExpressionFormatException("format rule error "+rulestr);
						}
					}else{
						throw new ExpressionFormatException("The rule is not closed. rule string is :"+rulesStr);
					}
				}
				leaf.setRules(rules);
			}else{
				throw new ExpressionFormatException("The Leaf is illegal");
			}
		}else{
			throw new ExpressionFormatException("rule in Leaf is null");
		}
		return leaf;
	}	
	
	private static void checkTarget(String targetStr,Rule rule){
		List<String> managelist = new ArrayList<String>();
		List<String> paramList = getParamFromRule(rule.getClass());
		while(true){
			if(StringUtil.isBlank(targetStr)){
				break;
			}
			if(!targetStr.startsWith("#"))
				throw new ExpressionFormatException("The param has some error"+targetStr);
			String tmpTargetStr = targetStr.substring(1,targetStr.length());
			int nextPoundIndex = tmpTargetStr.indexOf("#");
			if(nextPoundIndex != -1){
				String param = tmpTargetStr.substring(0,nextPoundIndex);
				targetStr = targetStr.substring(nextPoundIndex+3 >targetStr.length() ?targetStr.length():nextPoundIndex+3 ,targetStr.length());
				if(StringUtil.isBlank(param))
					throw new ExpressionFormatException("The param is null");
				if(!paramList.contains(param))
					throw new ExpressionFormatException("param "+param+" not in "+rule.getClass().getSimpleName());
				managelist.add(param);
			}else{
				throw new ExpressionFormatException("The param has some error"+targetStr);
			}
			
		}
		if(managelist.size() != paramList.size()){
			StringBuilder sb = new StringBuilder();
			for(String s :managelist){
				sb.append(sb.length() > 0?",":"");
				sb.append(s);
			}
			throw new ExpressionFormatException("Please check "+ (sb.length() > 0 ? sb.toString():"null")+ ", the parameters are different from " + rule.getClass().getSimpleName()+" parameters");
		}
	}
	
	private static List<String> getParamFromRule(Class<? extends Rule> ruleClass){
		NoParam noParam = ruleClass.getAnnotation(NoParam.class);
		List<String> list = new ArrayList<String>();
		if(noParam != null)
			return list;
		
		Field[] fields = StringUtil.getAllFields(ruleClass);
		for(Field f:fields){
			f.setAccessible(true);
			String name = f.getName();
			RuleParam ruleParam = f.getAnnotation(RuleParam.class);
			if(ruleParam != null){
				String value = ruleParam.value();
				if(StringUtil.isBlank(value))
					list.add(name);
				else
					list.add(value);
			}
			
		}
		if(list.size() <= 0)
			list.add("param");
		return list;
	}
	private static void setBase(String baseStr,Rule rule){
		Map<String,String> manageMap =new HashMap<String,String>();
		List<String> baseList = getBaseFromRule(rule.getClass());
		while(true){
			if(StringUtil.isBlank(baseStr)){
				break;
			}
			if(!baseStr.startsWith("{") && !baseStr.endsWith("}") )
				throw new ExpressionFormatException("The base has some error"+baseStr);
			int leftIndexBrace = baseStr.indexOf("{");
			int reghtIndexBrace = baseStr.indexOf("}");
			String kAndv = baseStr.substring(leftIndexBrace+1,reghtIndexBrace);
			if(kAndv.indexOf("{") != -1 || kAndv.indexOf("}") != -1  )
				throw new ExpressionFormatException("base format error, around "+kAndv);
			
			if(StringUtil.isNotBlank(kAndv)){
				if(baseList.size() <= 0)
					throw new ExpressionFormatException("is a no base Rule "+rule.getClass().getSimpleName());
				if(!kAndv.contains(":")){
					manageMap.put("base",kAndv);
				}else{
					List<String> arr = new ArrayList();
					int indexOFColon = kAndv.indexOf(":");
					String pre = kAndv.substring(0,indexOFColon);
					String suf = kAndv.substring(indexOFColon+1);
					if(StringUtil.isNotBlank(pre) && StringUtil.isNotBlank(suf)){
						manageMap.put(pre,suf);
					}else if(StringUtil.isBlank(pre) && StringUtil.isNotBlank(suf)){
						if(baseList.size() != 1)
							throw new ExpressionFormatException("base size not match " + rule.getClass().getSimpleName() + " has " +baseList.size() + "but expression has more.");
						manageMap.put("base",suf);
					}else if(StringUtil.isNotBlank(pre) && StringUtil.isBlank(suf)){
						manageMap.put(pre,null);
					}else{
						if(baseList.size() >= 0)
							throw new ExpressionFormatException("base size not match " + rule.getClass().getSimpleName() + " has " +baseList.size() + "but expression has null.");
					}
				}
				baseStr = baseStr.substring((baseStr.length() >= reghtIndexBrace+2)?reghtIndexBrace+2:baseStr.length(),baseStr.length());
			}else{
				if(baseList.size() >= 0)
					throw new ExpressionFormatException("base size not match " + rule.getClass().getSimpleName() + " has " +baseList.size() + "but expression has null.");
			}
			
		}
		if(manageMap.keySet().size() != baseList.size()){
			StringBuilder sb = new StringBuilder();
			for(String key :manageMap.keySet()){
				sb.append(sb.length() > 0?",":"");
				sb.append(key);
			}
			throw new ExpressionFormatException("Please check "+ (sb.length() > 0 ? sb.toString():"null")+ ", the bases are different from " + rule.getClass().getSimpleName()+" bases");
		}
		if(manageMap != null ){
			for(Entry<String, String> baseEntry: manageMap.entrySet()){
				String key = baseEntry.getKey();
				String value = baseEntry.getValue();
				setRuleValue(rule,key,value);
			}
		}
	}
	private static void setRuleValue(Rule rule,String key,String value) {
		Class ruleClass = rule.getClass();
		if(ruleClass.getAnnotation(NoBase.class) != null ){
			return;
		}
		Field[] fields = StringUtil.getAllFields(ruleClass);
		boolean managed = false;
		for(Field f:fields){
			f.setAccessible(true);
			RuleBase ruleBase = f.getAnnotation(RuleBase.class);
			if(ruleBase == null)
				continue;
			managed = true;
			String paramValue = ruleBase.value();
			String name = f.getName();
			if(key.equals(StringUtil.isNotBlank(paramValue)?paramValue:name)){
				ReflectionUtil.invokeSet(rule, name, StringUtil.StringToField(value, f));
			}
		}
		if(!managed){
			if("base".equals(key))
				try {
					ReflectionUtil.invokeSet(rule, "base", StringUtil.StringToField(value, ruleClass.getSuperclass().getDeclaredField("base")));
				} catch (Exception e) {
					try {
						throw e;
					} catch (Exception e1) {
						logger.error("throw e error");
					}
				}
			else
				throw new ExpressionFormatException("please check,The base key "+key +"not find in "+ruleClass.getSimpleName());
		}
	}
	private static List<String> getBaseFromRule(Class<? extends Rule> ruleClass){
		List<String> list = new ArrayList<String>();
		NoBase noBase = ruleClass.getAnnotation(NoBase.class);
		if(noBase != null)
			return list;
		Field[] fields = StringUtil.getAllFields(ruleClass);
		for(Field f:fields){
			f.setAccessible(true);
			String name = f.getName();
			RuleBase ruleBase = f.getAnnotation(RuleBase.class);
			if(ruleBase != null){
				String value = ruleBase.value();
				if(StringUtil.isBlank(value))
					list.add(name);
				else
					list.add(value);
			}
			
		}
		if(list.size() <= 0)
			list.add("base");
		return list;
	}
	
	
	private static int indexOfRightSquareBracket(int indexOfLeftSquareBracket,String expression){
		if(indexOfLeftSquareBracket == -1)
			return -1;
		String bracketCharacter = expression.substring(indexOfLeftSquareBracket,indexOfLeftSquareBracket+1);
		if(!"[".equals(bracketCharacter))
			return -1;
		if((indexOfLeftSquareBracket+2) > expression.length())
			return -1;
		Stack<String> leftSquareBracketstack = new Stack<String>();
		leftSquareBracketstack.add("[");
		for(int i = indexOfLeftSquareBracket+1;i<expression.length();i++){
			Character c = expression.charAt(i);
			if(new Character('[').equals(c))
				leftSquareBracketstack.add("[");
			if(new Character(']').equals(c))
				leftSquareBracketstack.pop();
			if(leftSquareBracketstack.size() == 0){
				return i;
			}
		}
		return -1;
	}
	
	private static Node formatNode(String expression) throws Exception{
		
		if(StringUtil.isBlank(expression))
			return null;
		if(expression.startsWith("[") && expression.endsWith("]")){
			Node node = new Node();
			List<Element> elements = new ArrayList<Element>();
			expression = expression.substring(1,expression.length()-1);
			while(StringUtil.isNotBlank(expression)){
				int indexOfLeftSquareBracket = expression.indexOf("[");
				if(indexOfLeftSquareBracket == -1)
					throw new ExpressionFormatException("expression format error  around "+expression);
				int indexOfRightSquareBracket = indexOfRightSquareBracket(indexOfLeftSquareBracket,expression);
				if(indexOfRightSquareBracket == -1)
					throw new ExpressionFormatException("expression format error not find the right square bracket. around "+expression);
				String preExp = expression.substring(indexOfLeftSquareBracket,indexOfRightSquareBracket+1);
				String sufExp = expression.substring(indexOfRightSquareBracket+1);
				if(StringUtil.isNotBlank(preExp)){
					if("[]".equals(preExp)){
						elements.add(new Bud());
					}else{
						if(preExp.startsWith("[") && preExp.endsWith("]")){
							String tmpExp = preExp.substring(1,preExp.length()-1);
							if(tmpExp.contains("[") && tmpExp.contains("]")){
								elements.add(formatNode(preExp));
							}else if(!tmpExp.contains("[") && !tmpExp.contains("]") && tmpExp.contains("$")){
								elements.add(formatLeaf(preExp));
							}
						}else{
							throw new ExpressionFormatException("expression format error  around "+preExp);
						}
					}
				}else{
					throw new ExpressionFormatException("expression format error not find the element.around "+expression);
				}
				expression = sufExp;
			}
			node.setElements(elements);
			return node;
		}else{
			throw new ExpressionFormatException("expression not start with [ or not end with ]");
		}
	}
	
	public static Root  expression2Root(Class clazz) throws Exception{
		Root root = new Root();
		com.sunbox.annotation.application.Root rootAnno = (com.sunbox.annotation.application.Root)
				clazz.getDeclaredAnnotation(com.sunbox.annotation.application.Root.class);
		if(rootAnno == null)
			throw new ExpressionFormatException("not have Root Annotation.");
		String unid = rootAnno.unid();
		if(StringUtil.isBlank(unid))
			throw new ExpressionFormatException("unid is null");
		root.setUnid(unid);
		com.sunbox.annotation.application.Node nodeAnno = (com.sunbox.annotation.application.Node)
				clazz.getDeclaredAnnotation(com.sunbox.annotation.application.Node.class);
		if(nodeAnno == null)
			throw new ExpressionFormatException("not have Node Annotation.");
		Class<? extends Element>[] elementClasses = nodeAnno.elements();
		root.setElements(formatAnnoElement(elementClasses));
		return root;
	}
	
	private static List<Element> formatAnnoElement(Class<? extends Element>[] elementClasses) throws Exception{
		if(elementClasses == null || elementClasses.length <= 0)
			throw new ExpressionFormatException("elementClasses is null");
		List<Element> elements = new ArrayList<Element>();
		for(int i = 0;i < elementClasses.length ; i++){
			Class<? extends Element> elementClass = elementClasses[i];
			if(Bud.class.equals(elementClass)){
				elements.add(new Bud());
			}else {
				com.sunbox.annotation.application.Node nodeAnno = (com.sunbox.annotation.application.Node)
						elementClass.getDeclaredAnnotation(com.sunbox.annotation.application.Node.class);
				com.sunbox.annotation.application.Leaf leafAnno = (com.sunbox.annotation.application.Leaf)
						elementClass.getDeclaredAnnotation(com.sunbox.annotation.application.Leaf.class);
				if(nodeAnno == null && leafAnno == null){
					throw new ExpressionFormatException(elementClass.getSimpleName()+" not have Node Annotation OR Leaf Annotation");
				}
				if(nodeAnno != null){
					Class<? extends Element>[] eleClasses = nodeAnno.elements();
					List<Element> es = formatAnnoElement(eleClasses);
					if(es != null && es.size() > 0){
						Node node = new Node();
						node.setElements(es);
						elements.add(node);
					}	
				}
				if(leafAnno != null){
					com.sunbox.annotation.application.Rule[] rules = leafAnno.value();
					if(rules != null && rules.length > 0){
						Leaf leaf = new Leaf();
						List<Rule> rs = new ArrayList<Rule>();
						for(com.sunbox.annotation.application.Rule rule:rules){
							boolean not = rule.not();
							Class ruleClass = rule.rule();
							if(not){
								rs.add(new Nrule((Rule)ruleClass.newInstance()));
							}else{
								rs.add((Rule)ruleClass.newInstance());
							}
						}
						com.sunbox.annotation.application.IfNull ifNullAnno = (com.sunbox.annotation.application.IfNull)
								elementClass.getDeclaredAnnotation(com.sunbox.annotation.application.IfNull.class);
						boolean ifnull = ifNullAnno== null?false:ifNullAnno.value();
						leaf.setRules(rs,ifnull);
						elements.add(leaf);
					}else{
						throw new ExpressionFormatException(elementClass.getSimpleName()+" not have Rules");
					}
				}
				
			}
		}
		return elements;
	}
	public static void main(String[] args) throws Exception {
		
	}
}
