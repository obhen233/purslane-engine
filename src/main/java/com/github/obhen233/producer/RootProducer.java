package com.github.obhen233.producer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.github.obhen233.annotation.application.Leaf;
import com.github.obhen233.annotation.application.Node;
import com.github.obhen233.annotation.application.Root;
import com.github.obhen233.annotation.application.Rule;
import com.github.obhen233.annotation.framework.Function;
import com.github.obhen233.annotation.framework.NoBase;
import com.github.obhen233.element.Bud;
import com.github.obhen233.element.Element;
import com.github.obhen233.out.FeildInfo;
import com.github.obhen233.out.FieldType;
import com.github.obhen233.out.RootParam;
import com.github.obhen233.plugin.AsyncStorePlugin;
import com.github.obhen233.plugin.StorePlugin;
import com.github.obhen233.plugin.SynchStorePlugin;
import com.github.obhen233.util.CodingUtil;
import com.github.obhen233.util.PackageScanner;
import com.github.obhen233.util.ReflectionUtil;
import com.github.obhen233.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.github.obhen233.annotation.application.IfNull;
import com.github.obhen233.annotation.framework.NoParam;
import com.github.obhen233.annotation.framework.RuleBase;
import com.github.obhen233.annotation.framework.RuleParam;
import com.github.obhen233.attribute.Nrule;
import com.github.obhen233.exception.ClassRepeatsException;
import com.github.obhen233.exception.ClassUnregisteredException;
import com.github.obhen233.exception.NoFindRootException;
import com.github.obhen233.exception.UnidNullException;

public class RootProducer {

	private static Logger logger = LoggerFactory.getLogger(RootProducer.class);

	private static Map<String, com.github.obhen233.element.Root> engineMap = new HashMap<String, com.github.obhen233.element.Root>();
	private static final String defaultRuleXmlPath = "ruleengine-commom.xml";
	private static final int defaultSaveRootSec = 5000;
	private static RootProducer instance;
	private static StorePlugin storePlugin;

	private RootProducer() {
	}

	public static synchronized RootProducer getInstance() {
		if (instance == null) {
			instance = new RootProducer();
		}
		return instance;
	}

	public boolean inntProducer(String xmlPath) throws Exception {
		ClassLoader classLoader = RootProducer.class.getClassLoader();
		InputStream in = classLoader.getResourceAsStream(xmlPath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		NodeList nodeList = doc.getElementsByTagName("element-scan");
		if (nodeList != null) {
			org.w3c.dom.Element scanElement = (org.w3c.dom.Element) nodeList.item(0);
			String annotationPath = scanElement.getAttribute("base-package");
			inntAnnotation(annotationPath);
		}

		return true;
	}

	public static boolean inntProducer(StorePlugin storePlugin,int SaveRootSec) throws Exception {
		RootProducer.storePlugin = storePlugin;
		if(storePlugin != null && AsyncStorePlugin.class.isInstance(storePlugin)) {
			List<com.github.obhen233.element.Root> roots = storePlugin.getRoots();
			for (com.github.obhen233.element.Root root : roots) {
				getKeyFromRoot(deepClone(root));
			}
			saveRootsTimers(SaveRootSec);
		}
		return true;
	}

	public static boolean inntProducer(StorePlugin storePlugin) throws Exception {
		return inntProducer(storePlugin,defaultSaveRootSec);
	}

	private static void inntAnnotation(String annotationPath) throws Exception {
		if (StringUtil.isNotBlank(annotationPath)) {
			PackageScanner scanner = new PackageScanner(annotationPath);
			List<String> scannerClassList = scanner.getFullyQualifiedClassNameList();
			for (String elementClass : scannerClassList) {
				Class anonClass = Class.forName(elementClass);
				Root rootAnno = (Root) anonClass.getDeclaredAnnotation(Root.class);
				if (rootAnno != null) {
					String unid = rootAnno.unid();
					if (StringUtil.isBlank(unid))
						throw new UnidNullException("the " + elementClass + " root element unid is null.");
					com.github.obhen233.element.Root root = new com.github.obhen233.element.Root();
					root.setUnid(unid);
					Node nodeAnno = (Node) anonClass.getAnnotation(Node.class);
					if (nodeAnno == null) {
						logger.info(elementClass + "has not Nodes.");
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

	private static List<Element> formatAnnotation(Class<? extends Element>[] es) throws Exception {
		List<Element> elements = new ArrayList<Element>();
		if (es != null && es.length > 0) {
			for (Class<? extends Element> ec : es) {
				com.github.obhen233.element.Node node = null;
				com.github.obhen233.element.Leaf leaf = null;
				Bud bud = null;
				Node n = ec.getAnnotation(Node.class);
				Leaf l = ec.getAnnotation(Leaf.class);
				if (n!= null) {
					List<Element> list = formatAnnotation(n.elements());
					if (list != null && list.size() > 0) {
						node = new com.github.obhen233.element.Node();
						node.setElements(list);
					}
				}
				if (ec.equals(Bud.class)) {
					bud = new Bud();
				}
				if (l!= null) {
					leaf = new com.github.obhen233.element.Leaf();
					List<com.github.obhen233.attribute.Rule> rules = new ArrayList<com.github.obhen233.attribute.Rule>();
					leaf.setRules(rules);

					Rule[] ruleAnnos = l.value();
					if (ruleAnnos != null && ruleAnnos.length > 0) {
						for (Rule anno : ruleAnnos) {
							Class<? extends com.github.obhen233.attribute.Rule> ruleClass = anno.rule();
							boolean not = anno.not();
							com.github.obhen233.attribute.Rule rule = ruleClass.newInstance();
							Field[] fields = StringUtil.getAllFields(ruleClass);
							for (Field f : fields) {
								f.setAccessible(true);
								String name = f.getName();
								//param不用初始化
								//RuleParam ruleParam = f.getAnnotation(RuleParam.class);
								RuleBase ruleBase = f.getAnnotation(RuleBase.class);
								if (ruleBase != null) {
									String base = ruleBase.base();
									Object o = StringUtil.StringToField(base, f);
									ReflectionUtil.invokeSet(rule, name, o);
								}
							}
							if (not) {
								Nrule nRule = new Nrule(rule);
								rules.add(nRule);
							} else {
								rules.add(rule);
							}
						}
					} else {
						IfNull ifNullAnno = ec.getAnnotation(IfNull.class);
						boolean ifNull = ifNullAnno == null ? false : ifNullAnno.value();
						leaf = new com.github.obhen233.element.Leaf();
						leaf.setRules(null, ifNull);
					}
				}

				if (node == null && leaf != null && bud == null) {
					elements.add(leaf);
				}
				if (node != null && leaf == null && bud == null) {
					elements.add(node);
				}
				if (node == null && leaf == null && bud != null) {
					elements.add(bud);
				}
			}
			return elements;
		} else {
			return null;
		}
	}

	public static boolean inntProducer(Properties properties) throws Exception {
		String annotationPath = properties.getProperty("element-scan");
		inntAnnotation(annotationPath);
		return true;
	}


	private static void getKeyFromRoot(com.github.obhen233.element.Root root) throws Exception {
		StringBuilder sb = new StringBuilder();
		String unid = root.getUnid();
		if (StringUtil.isBlank(unid))
			throw new UnidNullException("the  root element unid is null.");
		sb.append(unid);
		List<Element> elements = root.getElements();
		SortedSet<String> sortSet = new TreeSet<String>();
		getElementsStr(elements, sortSet);
		for (String key : sortSet) {
			sb.append("&").append(key);
		}
		engineMap.put(CodingUtil.MD5(sb.toString()), root);
	}


	private static void getElementsStr(List<Element> elements, SortedSet<String> sortSet) {
		if (elements != null && elements.size() > 0)
			for (Element element : elements) {
				if ("Node".equals(element.getClass().getSimpleName())) {
					com.github.obhen233.element.Node n = (com.github.obhen233.element.Node) element;
					getElementsStr(n.getElements(), sortSet);
				} else if ("Leaf".equals(element.getClass().getSimpleName())) {
					com.github.obhen233.element.Leaf l = (com.github.obhen233.element.Leaf) element;
					List<com.github.obhen233.attribute.Rule> rules = l.getRules();
					if (rules != null && rules.size() > 0) {
						for (com.github.obhen233.attribute.Rule r : rules) {
							if (Nrule.class.isInstance(r)) {
								com.github.obhen233.attribute.Rule nr = ((Nrule) r).getRule();
								getParamFields(nr, sortSet);
							} else {
								getParamFields(r, sortSet);
							}
						}
					}
				}
			}
	}

	private static void getParamFields(com.github.obhen233.attribute.Rule rule, SortedSet<String> sortSet) {
		Class<? extends com.github.obhen233.attribute.Rule> clazz = rule.getClass();
		Field[] fields = StringUtil.getAllFields(clazz);
		StringBuilder s = null;
		String preKey = RuleProducer.getKeyByClassName(clazz.getName());
		if (StringUtil.isBlank(preKey))
			throw new ClassUnregisteredException("Rule Class \"" + clazz.getName() + "\" unregistered");
		NoParam noParam = clazz.getAnnotation(NoParam.class);
		NoBase noBase = clazz.getAnnotation(NoBase.class);
		boolean isNoParam = noParam != null;
		boolean isNoBase = noBase != null;
		boolean hasCustomizeParam = false;
		boolean hasCustomizeBase = false;
		for (Field f : fields) {
			f.setAccessible(true);
			String name = f.getName();
			Object o = ReflectionUtil.invokeGet(rule, name);
			if (o == null) {
				RuleParam ruleParam = f.getDeclaredAnnotation(RuleParam.class);
				RuleBase ruleBase = f.getDeclaredAnnotation(RuleBase.class);
				if (ruleParam != null && !isNoParam) {
					hasCustomizeParam = true;
					String value = ruleParam.value();
					String key = StringUtil.isNotBlank(value) ? value : name;
					s = new StringBuilder();
					sortSet.add(s.append(preKey).append(".").append(key).toString());
				} else if (ruleBase != null && !isNoBase) {
					hasCustomizeBase = true;
					String value = ruleBase.value();
					String key = StringUtil.isNotBlank(value) ? value : name;
					s = new StringBuilder();
					sortSet.add(s.append(preKey).append(".").append(key).toString());
				}
			}else{
				hasCustomizeBase = true;
			}
		}
		if (!hasCustomizeParam && !isNoParam) {
			Object o = ReflectionUtil.invokeGet(rule, "param");
			if (o == null) {
				s = new StringBuilder();
				sortSet.add(s.append(preKey).append(".").append("param").toString());
			}
		}

		if (!hasCustomizeBase && !isNoBase) {
			Object o = ReflectionUtil.invokeGet(rule, "base");
			if (o == null) {
				s = new StringBuilder();
				sortSet.add(s.append(preKey).append(".").append("base").toString());
			}
		}
	}

	public static com.github.obhen233.element.Root getRootByKey(String key) throws Exception {
		if(storePlugin != null && SynchStorePlugin.class.isInstance(storePlugin)){
			for(com.github.obhen233.element.Root root :((SynchStorePlugin)storePlugin).getRoots()){
				String tempKey = getKeyStrFromRoot(root);
				if(tempKey.equals(key))
					return root;
			}
			throw new NoFindRootException();
		}else {
			com.github.obhen233.element.Root root = engineMap.get(key);
			if (root == null) {
				throw new NoFindRootException();
			}
			return deepClone(root);
		}
	}

	public static String getKeyStrFromRoot(com.github.obhen233.element.Root root) throws Exception {
		StringBuilder sb = new StringBuilder();
		String unid = root.getUnid();
		if (StringUtil.isBlank(unid))
			throw new UnidNullException("the root element unid is null.");
		sb.append(unid);
		List<Element> elements = root.getElements();
		SortedSet<String> sortSet = new TreeSet<String>();
		getElementsStr(elements, sortSet);
		for (String key : sortSet) {
			sb.append("&").append(key);
		}
		return CodingUtil.MD5(sb.toString());
	}

	public static boolean insertRoot(com.github.obhen233.element.Root root, boolean updateFlag) throws Exception {
		if (storePlugin != null && SynchStorePlugin.class.isInstance(storePlugin))
			return ((SynchStorePlugin) storePlugin).saveRoot(root,updateFlag);
		String unid = root.getUnid();
		if (StringUtil.isBlank(unid)) {
			throw new UnidNullException("insert Root error");
		}
		String key = getKeyStrFromRoot(root);
		if (engineMap.containsKey(key)) {
			if (!updateFlag)
				throw new ClassRepeatsException("root already have.");
		}

		engineMap.put(key, root);
		return true;
	}

	public static boolean insertRoot(com.github.obhen233.element.Root root) throws Exception {
		return insertRoot(root, false);
	}

	public static boolean deleteRoot(String key) throws Exception {

		if (storePlugin != null && SynchStorePlugin.class.isInstance(storePlugin)) {
			return ((SynchStorePlugin) storePlugin).deleteRoot(getRootByKey(key));
		}else {
			if (StringUtil.isBlank(key))
				return true;
			com.github.obhen233.element.Root root = engineMap.remove(key.toUpperCase());
			return root != null;
		}
	}

	public static boolean deleteRoot(com.github.obhen233.element.Root root) throws Exception {
		String unid = root.getUnid();
		if (StringUtil.isBlank(unid)) {
			throw new UnidNullException("insert Root error");
		}
		String key = getKeyStrFromRoot(root);
		return deleteRoot(key);
	}

	public static List<RootParam> getRootsParam(String lang) {
		List<RootParam> rootParams = new ArrayList<RootParam>();
		if (storePlugin != null && SynchStorePlugin.class.isInstance(storePlugin)) {
			for (com.github.obhen233.element.Root r : ((SynchStorePlugin)storePlugin).getRoots()) {
				RootParam rootParam = new RootParam();
				TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
				rootParam.setFeilds(feildInfos);
				rootParam.setUnid(r.getUnid());
				getFeildInfo(r.getElements(), feildInfos, lang);
				rootParams.add(rootParam);
			}
		}else {
			for (String key : engineMap.keySet()) {
				com.github.obhen233.element.Root r = engineMap.get(key);
				RootParam rootParam = new RootParam();
				TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
				rootParam.setFeilds(feildInfos);
				rootParam.setUnid(r.getUnid());
				getFeildInfo(r.getElements(), feildInfos, lang);
				rootParams.add(rootParam);
			}
		}
		return rootParams;
	}

	public static List<RootParam> getRootsParam() {
		return getRootsParam("zh_cn");
	}

	private static void getFeildInfo(List<Element> elements, TreeSet<FeildInfo> feildInfos, String lang) {
		if (elements == null || elements.size() <= 0)
			return;
		for (Element e : elements) {
			if (Bud.class.equals(e.getClass()))
				continue;
			else if (com.github.obhen233.element.Node.class.equals(e.getClass()))
				getFeildInfo(((com.github.obhen233.element.Node) e).getElements(), feildInfos, lang);
			else if (com.github.obhen233.element.Leaf.class.equals(e.getClass())) {
				com.github.obhen233.element.Leaf leaf = (com.github.obhen233.element.Leaf) e;
				List<com.github.obhen233.attribute.Rule> rules = leaf.getRules();
				if (rules == null || rules.size() <= 0)
					return;

				for (com.github.obhen233.attribute.Rule rule : rules) {
					TreeSet<FeildInfo> infos = getFeildInfo(rule, lang);
					if (feildInfos == null)
						feildInfos = new TreeSet<FeildInfo>();
						feildInfos.addAll(infos);
				}
			}
		}
	}

	private static TreeSet<FeildInfo> getFeildInfo(com.github.obhen233.attribute.Rule rule, String lang) {

		com.github.obhen233.attribute.Rule ru = rule;
		if (Nrule.class.equals(rule.getClass()))
			ru = ((Nrule) rule).getRule();
		if (ru != null) {
			Function function = ru.getClass().getDeclaredAnnotation(Function.class);
			String ruleFunctionName = null;
			if (function != null)
				ruleFunctionName = function.name();
			NoParam noParam = ru.getClass().getDeclaredAnnotation(NoParam.class);
			NoBase noBase = ru.getClass().getDeclaredAnnotation(NoBase.class);
			String ruleClassName = ru.getClass().getSimpleName();
			StringBuilder sb = new StringBuilder();
			sb.append(ruleClassName.substring(0, 1).toLowerCase());
			sb.append(ruleClassName.substring(1));
			String className = (StringUtil.isNotBlank(ruleFunctionName)) ? ruleFunctionName : sb.toString();
			TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
			Field[] fields = StringUtil.getAllFields(ru.getClass());
			for (Field field :fields) {
				Object objValue = ReflectionUtil.invokeGet(ru, field.getName());
				if (objValue != null)
					continue;
				String fieldName = null;
				if (field != null) {
					FeildInfo feildInfo = new FeildInfo();
					field.setAccessible(true);
					if("base".equals(field.getName()) && (!needBase(fields) || noBase != null))
						continue;
					if("param".equals(field.getName()) && (!needParam(fields) || noParam != null))
						continue;

					String fieldValue = null;
					RuleBase ruleBase = field.getDeclaredAnnotation(RuleBase.class);
					RuleParam ruleParam = field.getDeclaredAnnotation(RuleParam.class);
					if (ruleBase != null) {
						if (noBase != null)
							continue;
						if (StringUtil.isNotBlank(ruleBase.base()))
							continue;
						feildInfo.setFieldType(FieldType.base);
						fieldValue = ruleBase.value();
					} else if (ruleParam != null) {
						if (noParam != null)
							continue;
						feildInfo.setFieldType(FieldType.param);
						fieldValue = ruleParam.value();
					}
					fieldName = StringUtil.isNotBlank(fieldValue) ? fieldValue : field.getName();
					StringBuilder fieldSb = new StringBuilder();
					feildInfo.setName(fieldSb.append(className).append(".").append(fieldName).toString());
					feildInfo.setSimpleName(fieldName);
					feildInfo.setLang(lang);
					feildInfo.setDesc(StringUtil.getDescription(ru.getClass(), field, lang));
					feildInfos.add(feildInfo);
				}
			}
			return feildInfos;
		}
		return null;
	}

	public static List<RootParam> getRootParamByUnid(String unid, String lang) {
		if (StringUtil.isBlank(unid))
			return null;
		List<RootParam> rootParams = new ArrayList<RootParam>();
		if (storePlugin != null && SynchStorePlugin.class.isInstance(storePlugin)) {
			for (com.github.obhen233.element.Root root : ((SynchStorePlugin) storePlugin).getRoots()) {
				if (root != null) {
					String rootUnid = root.getUnid();
					if (!unid.equals(rootUnid))
						continue;
					RootParam rootParam = new RootParam();
					TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
					rootParam.setFeilds(feildInfos);
					rootParam.setUnid(root.getUnid());
					getFeildInfo(root.getElements(), feildInfos, lang);
					rootParams.add(rootParam);
				}
			}
		}else {

			for (Entry<String, com.github.obhen233.element.Root> entry : engineMap.entrySet()) {
				com.github.obhen233.element.Root root = entry.getValue();
				if (root != null) {
					String rootUnid = root.getUnid();
					if (!unid.equals(rootUnid))
						continue;
					RootParam rootParam = new RootParam();
					TreeSet<FeildInfo> feildInfos = new TreeSet<FeildInfo>();
					rootParam.setFeilds(feildInfos);
					rootParam.setUnid(root.getUnid());
					getFeildInfo(root.getElements(), feildInfos, lang);
					rootParams.add(rootParam);
				}
			}
		}
		return rootParams;
	}

	public static List<RootParam> getRootParamByUnid(String unid) {
		return getRootParamByUnid(unid, "zh_cn");
	}


	private static <T extends Serializable> T deepClone(T object) {
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

	public static boolean saveRoots() {
		if (storePlugin != null && AsyncStorePlugin.class.isInstance(storePlugin)) {
			List<com.github.obhen233.element.Root> list = new ArrayList<com.github.obhen233.element.Root>();
			for (String key : engineMap.keySet()) {
				com.github.obhen233.element.Root root = engineMap.get(key);
				if (root != null)
					list.add(deepClone(root));
			}
			return ((AsyncStorePlugin) storePlugin).saveRoots(list);
		}
		return false;
	}

	private static void saveRootsTimers(int saveSec){
		Timer saveTimer = new Timer();
		saveTimer.schedule(new TimerTask() {
			@Override
			public void run () {
				RootProducer.saveRoots();
			}
		},1000,saveSec);
	}

	public static List<com.github.obhen233.element.Root> getRoots(){
		if (storePlugin != null) {
			return storePlugin.getRoots();
		}
		List<com.github.obhen233.element.Root> roots = new ArrayList<com.github.obhen233.element.Root>(engineMap.size());
		for(Entry<String, com.github.obhen233.element.Root> entry:engineMap.entrySet()){
			roots.add(deepClone(entry.getValue()));
		}
		return roots;
	}


	private static boolean needBase(Field[] fields){
		for(Field field:fields) {
			RuleBase ruleBase = field.getDeclaredAnnotation(RuleBase.class);
			if (ruleBase != null)
				return false;
		}
		return true;
	}
	private static boolean needParam(Field[] fields) {
		for (Field field : fields) {
			RuleParam ruleParam = field.getDeclaredAnnotation(RuleParam.class);
			if (ruleParam != null)
				return false;
		}
		return true;
	}
}
