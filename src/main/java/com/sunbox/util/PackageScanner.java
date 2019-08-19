package com.sunbox.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageScanner{
	
	
    private Logger logger = LoggerFactory.getLogger(PackageScanner.class);
    private String basePackage;
    private ClassLoader cl;

    
    public PackageScanner(String basePackage) {
        this.basePackage = basePackage;
        this.cl = getClass().getClassLoader();
    }
    public PackageScanner(String basePackage, ClassLoader cl) {
        this.basePackage = basePackage;
        this.cl = cl;
    }
  
    
    public List<String> getFullyQualifiedClassNameList() throws IOException {
        return doScan(basePackage, new ArrayList<String>());
    }

 
    private List<String> doScan(String basePackage, List<String> nameList) throws IOException {
        String splashPath = StringUtil.dotToSplash(basePackage);
        Enumeration<URL> urls = cl.getResources(splashPath);
        while(urls.hasMoreElements()){
        	URL url = urls.nextElement();
	        String filePath = StringUtil.getRootPath(url);
	        List<String> names = null; // contains the name of the class file. e.g., Apple.class will be stored as "Apple"
	        if (isJarFile(filePath)) {
	            names = readFromJarFile(filePath, splashPath);
	        } else {
	            names = readFromDirectory(filePath);
	        }
	        for (String name : names) {
	            if (isClassFile(name)) {
	                nameList.add(toFullyQualifiedName(name, basePackage));
	            } else {
	                doScan(basePackage + "." + name, nameList);
	            }
	        }
        }
        return nameList;
    }

    private String toFullyQualifiedName(String shortName, String basePackage) {
    	 StringBuilder sb = null;
    	if(!shortName.contains("/")){
	        sb = new StringBuilder(basePackage);
	        sb.append('.');
	        sb.append(StringUtil.trimExtension(shortName));
    	}else{
    		sb = new StringBuilder(StringUtil.trimExtension(StringUtil.splashToDot(shortName)));
    	}
        return sb.toString();
    }

    private List<String> readFromJarFile(String jarPath, String splashedPackageName) throws IOException {
        JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
        JarEntry entry = jarIn.getNextJarEntry();
        List<String> nameList = new ArrayList<String>();
        while (null != entry) {
            String name = entry.getName();
            if (name.startsWith(splashedPackageName) && isClassFile(name) && !name.contains("$")) {
                nameList.add(name);
            }
            entry = jarIn.getNextJarEntry();
        }
        jarIn.close();
        return nameList;
    }

    private List<String> readFromDirectory(String path) {
        File file = new File(path);
        String[] names = file.list();

        if (null == names) {
            return null;
        }

        return Arrays.asList(names);
    }

    private boolean isClassFile(String name) {
        return name.endsWith(".class");
    }

    private boolean isJarFile(String name) {
        return name.endsWith(".jar");
    }

  
    public static void main(String[] args) throws Exception {
    	PackageScanner scan = new PackageScanner("org");
        List<String> list = scan.getFullyQualifiedClassNameList();
        for(String s:list){
        	System.out.println(s);
        }
    }
}
