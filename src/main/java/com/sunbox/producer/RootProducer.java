package com.sunbox.producer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sunbox.annotation.application.IfNull;
import com.sunbox.annotation.framework.Description;
import com.sunbox.annotation.framework.Function;
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
import com.sunbox.exception.ClassRepeatsException;
import com.sunbox.exception.ClassUnregisteredException;
import com.sunbox.exception.NoFindRootException;
import com.sunbox.exception.UnidNullException;
import com.sunbox.out.FeildInfo;
import com.sunbox.out.FieldType;
import com.sunbox.out.RootParam;
import com.sunbox.util.CodingUtil;
import com.sunbox.util.PackageScanner;
import com.sunbox.util.ReflectionUtil;
import com.sunbox.util.StringUtil;

public class RootProducer {
	
	private static Logger logger = LoggerFactory.getLogger(RootProducer.class);
	
	private static Map<String,Root> engineMap = new HashMap<String,Root>();
	private static final String defaultRuleXmlPath = "ruleengine-commom.xml";
	private static RootProducer instance;
	private RootProducer(){}
	public static synchronized RootProducer getInstance() {
		if (instance == null) {
			instance = new RootProducer();
		}
		return instance;
	}
	
	public static boolean inntProducer(String xmlPath) throws Exception{
		ClassLoader classLoader = RootProducer.class.getClassLoader();
		InputStream in = classLoader.getResourceAsStream(xmlPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		NodeList nodeList = doc.getElementsByTagName("element-scan");
		if(nodeList != null){
			org.w3c.dom.Element scanElement = (org.w3c.dom.Element)nodeList.item(0);
			String annotationPath = scanElement.getAttribute("base-package");
			inntAnnotation(annotationPath);
		}
		
		return true;
	}
	
	private static void inntAnnotation(String annotationPath) throws Exception{
		if(StringUtil.isNotBlank(annotationPath)){
			 PackageScanner scanner = new PackageScanner(annotationPath);
			 List<String> scannerClassList = scanner.getFullyQualifiedClassNameList();
			 for(String elementClass :scannerClassList){
				Class anonClass = Class.forName(elementClass);
				com.sunbox.annotation.application.Root rootAnno = (com.sunbox.annotation.application.Root) anonClass.getDeclaredAnnotation(com.sunbox.annotation.application.Root.class);
				if(rootAnno != null){
					String unid = rootAnno.unid();
					if(StringUtil.isBlank(unid))
						throw new UnidNullException("the "+elementClass+" root element unid is null.");
					Root root = new Root();
					root.setUnid(unid);
					com.sunbox.annotation.application.Node nodeAnno = (com.sunbox.annotation.application.Node)anonClass.getAnnotation(com.sunbox.annotation.application.Node.class);
					if(nodeAnno == null){
						logger.info(elementClass +"has not Nodes.");
						continue;
					}
					Class<? extends Element>[] elements = nodeAnno.elements();
					List<Element> es = formatAnnotation(elements);
					root.setElements(es);
					getKeyFromRoot(root);
				}
			}
		 }
	}
	
	private static List<Element> formatAnnotation(Class<? extends Element>[] es) throws Exception{
		List<Element> elements = new ArrayList<Element>();
		if(es != null && es.length > 0){
			for(Class<? extends Element> ec : es){
				Node node = null;
				Leaf leaf = null;
				Bud bud = null;
				if(ec.equals(Node.class)){
					com.sunbox.annotation.application.Node n = ec.getAnnotation(com.sunbox.annotation.application.Node.class);
					List<Element> list = formatAnnotation(n.elements());
					if(list != null && list.size() > 0){
						node = new Node();
						node.setElements(list);
					}		
				}
				if(ec.equals(Bud.class)){
					bud = new Bud();
				}else if(ec.equals(Leaf.class)){
					leaf = new Leaf();
					List<Rule> rules = new ArrayList<Rule>();
					leaf.setRules(rules);
					com.sunbox.annotation.application.Leaf l = ec.getAnnotation(com.sunbox.annotation.application.Leaf.class);
					com.sunbox.annotation.application.Rule[] ruleAnnos = l.value();
					if(ruleAnnos != null && ruleAnnos.length > 0){
						for(com.sunbox.annotation.application.Rule anno:ruleAnnos){
							Class<? extends Rule> ruleClass = anno.rule();
							boolean not = anno.not();
							Rule rule = ruleClass.newInstance();
							Field[] fields = StringUtil.getAllFields(ruleClass);
							for(Field f: fields){
								f.setAccessible(true);
								String name = f.getName();
								//param不用初始化
								//RuleParam ruleParam = f.getAnnotation(RuleParam.class);
								RuleBase ruleBase = f.getAnnotation(RuleBase.class);
								if(ruleBase != null){
									String base = ruleBase.base();
									Object o = StringUtil.StringToField(base, f);
									ReflectionUtil.invokeSet(rule, name, o);
								}
							}
							if(not){
								Nrule nRule = new Nrule(rule);
								rules.add(nRule);
							}else{
								rules.add(rule);
							}
						}
					}else{
						IfNull ifNullAnno = ec.getAnnotation(IfNull.class);
						boolean ifNull = ifNullAnno == null ? false:ifNullAnno.value();
						leaf = new Leaf();
						leaf.setRules(null, ifNull);
					}
				}
				
				if(node == null && leaf != null && bud == null){
					elements.add(leaf);
				}
				if(node != null && leaf == null && bud == null){
					elements.add(node);
				}
				if(node == null && leaf == null && bud != null){
					elements.add(bud);
				}
			}
			return elements;
		}else{
			return null;
		}
	}
	
	public static boolean inntProducer(Properties properties) throws Exception{
		String annotationPath = properties.getProperty("element-scan");
		inntAnnotation(annotationPath);
		return true;
	}
	
	
	private static void getKeyFromRoot(Root root) throws Exception{
		StringBuilder sb = new StringBuilder();
		String unid = root.getUnid();
		if(StringUtil.isBlank(unid))
			throw new UnidNullException("the  root element unid is null.");
		sb.append(unid);
		List<Element> elements = root.getElements();
		SortedSet<String> sortSet = new TreeSet<String>();
		getElementsStr(elements,sortSet);
		for(String key:sortSet){
			sb.append("&").append(key);
		}
		engineMap.put(CodingUtil.MD5(sb.toString()), root);
	}
	
	
	private static void getElementsStr(List<Element> elements,SortedSet<String> sortSet ){
		if(elements != null && elements.size() > 0)
		for(Element element : elements){
			if("Node".equals(element.getClass().getSimpleName())){
				Node n = (Node)element;
				getElementsStr(n.getElements(),sortSet);
			}else if("Leaf".equals(element.getClass().getSimpleName())){
				Leaf l = (Leaf)element;
				List<Rule> rules = l.getRules();
				if(rules != null && rules.size() > 0){
					for(Rule r:rules){
						if(Nrule.class.isInstance(r)){
							Rule nr = ((Nrule)r).getRule();
							getParamFields(nr,sortSet);
						}else{
							getParamFields(r, sortSet);
						}
					}
				}
			}
		}
	}
	private static void getParamFields(Rule rule,SortedSet<String> sortSet){
		Class<? extends Rule> clazz = rule.getClass();
		Field[] fields = StringUtil.getAllFields(clazz);
		StringBuilder s = null;
		String preKey = RuleProducer.getKeyByClassName(clazz.getName());
		if(StringUtil.isBlank(preKey))
			throw new ClassUnregisteredException("Rule Class \""+clazz.getName()+"\" unregistered");
		NoParam noParam = clazz.getAnnotation(NoParam.class);
		NoBase noBase = clazz.getAnnotation(NoBase.class);
		boolean isNoParam = noParam != null;
		boolean isNoBase = noBase != null;
		boolean hasCustomizeParam = false;
		boolean hasCustomizeBase = false;
		for(Field f : fields){
			f.setAccessible(true);
			String name = f.getName();
			Object o = ReflectionUtil.invokeGet(rule,name);
			if(o == null){
				RuleParam ruleParam = f.getDeclaredAnnotation(RuleParam.class);
				RuleBase ruleBase = f.getDeclaredAnnotation(RuleBase.class);
				if(ruleParam != null && !isNoParam){
					hasCustomizeParam = true;
					String value = ruleParam.value();
					String key = StringUtil.isNotBlank(value)?value:name;
					s = new StringBuilder();
					sortSet.add(s.append(preKey).append(".").append(key).toString());
				}else if(ruleBase != null && !isNoBase){
					hasCustomizeBase = true;
					String value = ruleBase.value();
					String key = StringUtil.isNotBlank(value)?value:name;
					s = new StringBuilder();
					sortSet.add(s.append(preKey).append(".").append(key).toString());
				}
			}
		}
		if(!hasCustomizeParam  && !isNoParam){
			Object o = ReflectionUtil.invokeGet(rule,"param");
			if(o == null){
				sortSet.add(s.append(preKey).append(".").append("param").toString());
			}
		}
		
		if(!hasCustomizeBase  && !isNoBase){
			Object o = ReflectionUtil.invokeGet(rule,"base");
			if(o == null){
				sortSet.add(s.append(preKey).append(".").append("base").toString());
			}
		}
	}
	
	public Root getRootByKey(String key) throws NoFindRootException{
		Root root = engineMap.get(key);
		if(root == null){
			throw new NoFindRootException();
		}
		return deepClone(root) ;
	}
	
	public static String getKeyStrFromRoot(Root root) throws Exception{
		StringBuilder sb = new StringBuilder();
		String unid = root.getUnid();
		if(StringUtil.isBlank(unid))
			throw new UnidNullException("the root element unid is null.");
		sb.append(unid);
		List<Element> elements = root.getElements();
		SortedSet<String> sortSet = new TreeSet<String>();
		getElementsStr(elements,sortSet);
		for(String key:sortSet){
			sb.append("&").append(key);
		}
		return CodingUtil.MD5(sb.toString());
	}
	public static boolean insertRoot(Root root,boolean updateFlag) throws Exception{
		String unid = root.getUnid();
		if(StringUtil.isBlank(unid)){
			throw new UnidNullException("insert Root error");
		}
		String key = getKeyStrFromRoot(root);
		if(engineMap.containsKey(key)){
			if(!updateFlag)
				throw new ClassRepeatsException("root already have.");
		}
		engineMap.put(key, root);
		return true;
	}
	public static boolean insertRoot(Root root) throws Exception{
		return insertRoot(root,false);
	}
	
	public boolean deleteRoot(String key){
		if(StringUtil.isBlank(key))
			return true;
		Root root = engineMap.remove(key.toUpperCase());
		return root != null;
	}
	public boolean deleteRoot(Root root) throws Exception{
		String unid = root.getUnid();
		if(StringUtil.isBlank(unid)){
			throw new UnidNullException("insert Root error");
		}
		String key = getKeyStrFromRoot(root);
		return deleteRoot(key);
	}
	
	public List<RootParam> getRootsParam(String lang){
		List<RootParam> rootParams = new ArrayList<RootParam>();	
		for(String key:engineMap.keySet()){
			Root r = engineMap.get(key);
			RootParam rootParam = new RootParam();
			TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
			rootParam.setFeilds(feildInfos);
			rootParam.setUnid(r.getUnid());
			getFeildInfo(r.getElements(),feildInfos, lang);
			rootParams.add(rootParam);
		}
		return rootParams;
	}
	public List<RootParam> getRootsParam(){
		return getRootsParam("zh_cn");
	}
	
	private void getFeildInfo(List<Element> elements,TreeSet<FeildInfo> feildInfos,String lang){
		if(elements == null || elements.size() <=0)
			return;
		for(Element e:elements){
			if(Bud.class.equals(e.getClass()))
				continue;
			else if(Node.class.equals(e.getClass()))
				getFeildInfo(((Node)e).getElements(),feildInfos,lang);
			else if(Leaf.class.equals(e.getClass())){
				Leaf leaf = (Leaf)e;
				List<Rule> rules = leaf.getRules();
				if(rules == null || rules.size() <= 0)
					return;
				
				for(Rule rule:rules){
					TreeSet<FeildInfo> infos = getFeildInfo(rule,lang);
					if(feildInfos != null && feildInfos.size() > 0)
						feildInfos.addAll(infos);
				}
			}
		}
	}
	
	private static TreeSet<FeildInfo> getFeildInfo(Rule rule,String lang){
		
		Rule ru = null;
		if(Nrule.class.equals(rule.getClass()))
			ru = ((Nrule)rule).getRule();
		ru = rule;
		if(ru != null){
			Function function = ru.getClass().getDeclaredAnnotation(Function.class);
			String ruleFunctionName = null;
			if(function != null)
				ruleFunctionName = function.name();
			NoParam noParam = ru.getClass().getDeclaredAnnotation(NoParam.class);
			NoBase noBase = ru.getClass().getDeclaredAnnotation(NoBase.class);
			String ruleClassName = ru.getClass().getSimpleName();
			StringBuilder sb = new StringBuilder();
    		sb.append(ruleClassName.substring(0, 1).toLowerCase());  
		    sb.append(ruleClassName.substring(1));
		    String className = (StringUtil.isNotBlank(ruleFunctionName))?ruleFunctionName:sb.toString();
		    TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
		    for(Field field:StringUtil.getAllFields(ru.getClass())){
		    	Object objValue = ReflectionUtil.invokeGet(ru, field.getName());
		    	if(objValue != null)
		    		continue;
			    String fieldName = null;
			    if(field != null){
			    	FeildInfo feildInfo = new FeildInfo();
			    	field.setAccessible(true);
				    String fieldValue = null;
				    RuleBase ruleBase = field.getDeclaredAnnotation(RuleBase.class);
				    RuleParam ruleParam = field.getDeclaredAnnotation(RuleParam.class);
				    if(ruleBase != null){
				    	if(noBase != null)
				    		continue;
				    	if(StringUtil.isNotBlank(ruleBase.base()))
				    		continue;
				    	feildInfo.setFieldType(FieldType.base);
				    	fieldValue = ruleBase.value();
				    }else if(ruleParam != null){
				    	if(noParam != null)
				    		continue;
				    	feildInfo.setFieldType(FieldType.param);
				    	fieldValue = ruleParam.value();
				    }
				    fieldName = StringUtil.isNotBlank(fieldValue)?fieldValue:field.getName();
				    StringBuilder fieldSb = new StringBuilder(); 
				    feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
				    feildInfo.setSimpleName(fieldName);
				    feildInfo.setLang(lang);
				    feildInfo.setDesc(StringUtil.getDescription(ru.getClass(),field,lang));
				    feildInfos.add(feildInfo);
			    }
		    }
		    return feildInfos;
		}
		return null;
	}
	public List<RootParam> getRootByUnid(String unid,String lang){
		if(StringUtil.isBlank(unid))
			return null;
		List<RootParam> rootParams = new ArrayList<RootParam>();
		for(Entry<String,Root> entry:engineMap.entrySet()){
			Root root = entry.getValue();
			if(root != null){
				String rootUnid = root.getUnid();
				if(!unid.equals(rootUnid))
					continue;
				RootParam rootParam = new RootParam();
				TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
				rootParam.setFeilds(feildInfos);
				rootParam.setUnid(root.getUnid());
				getFeildInfo(root.getElements(),feildInfos,lang);
				rootParams.add(rootParam); 
			}
		}
		return rootParams;
	}
	
	public List<RootParam> getRootByUnid(String unid){
		return getRootByUnid(unid,"zh_cn");
	}
	
	
	private  <T extends Serializable> T deepClone(T object) {
	    T temp = null;
	    try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(object);
	        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
	        ObjectInputStream ois = new ObjectInputStream(bis);
	        temp = (T) ois.readObject();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return temp;
	} 


	
}
