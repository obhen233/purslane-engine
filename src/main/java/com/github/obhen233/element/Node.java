package com.github.obhen233.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.github.obhen233.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.obhen233.result.ExcuteResult;

// A Leaf mast belong to a Node,Node is composed of  Nodes or Leafes, and those Nodes or Leafes are join with "AND" if there are same Leafes in the Node,
// if there is nerver a Leaf in the Node and those Nodes belong to the Node are joined with "or"
//这个是节点，每个节点是由叶子和节点组成的，同一个节点下的叶子和叶子之间，叶子和节点之间是通过 "与"相连的，如果一个节点完全是由节点构成的，那么这些节点是通过"或"相连的，
public class Node implements Element,Serializable {
	
	private Logger logger = LoggerFactory.getLogger(Node.class);
	
	private List<Element> elements;

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}
	
	public ExcuteResult excute(){
		if(this.getElements() == null){
			ExcuteResult er = new ExcuteResult();
			er.setResult(false);
			er.setFormatMsg("ERROR:elements is null");
			return er;
		}
		boolean allNode = true;
		for(Element e:elements){
			if(Leaf.class.isInstance(e)){
				allNode = false;
				break;
			}
		}
		if(!allNode){
			for(Element e:elements){
				ExcuteResult er = e.excute();
				if(!er.isResult()){
					return er;
				}
					
			}
			ExcuteResult excuteResult = new ExcuteResult();
			excuteResult.setResult(true);
			excuteResult.setFormatMsg("SUCCESS!");
			return excuteResult;
		}else{
			StringBuilder sb = new StringBuilder();
			List<Object> excuteResultList = new ArrayList<Object>();
			for(Element e:elements){
				ExcuteResult er = e.excute();
				if(er.getFormatMsg() != null || StringUtil.isNotBlank(er.getFormatMsg().toString())){
					excuteResultList.add(er.getFormatMsg());
				}
				sb.append(sb.length() > 0 ? " AND " :"");
				sb.append(er.getFormatMsg());
				if(er.isResult()){
					ExcuteResult excuteResult = new ExcuteResult();
					excuteResult.setResult(true);
					excuteResult.setFormatMsg("SUCCESS!");
					return excuteResult;
				}
			}
			for(Object obj : excuteResultList){
				if(!String.class.equals(obj.getClass())){
					ExcuteResult excuteResult = new ExcuteResult();
					excuteResult.setResult(false);
					excuteResult.setFormatMsg(excuteResultList);
					return excuteResult;
				}
			}
			ExcuteResult excuteResult = new ExcuteResult();
			excuteResult.setResult(false);
			excuteResult.setFormatMsg(sb.toString());
			return excuteResult;
		}
		
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		elementsToStr(sb,elements);
		sb.append("]");
		return "[]".equals(sb.toString())?"":sb.toString();
	}
	
	private void elementsToStr(StringBuilder sb,List<Element> elements){
		if(elements == null || elements.size() <= 0)
			return;
		for(int i = 0;i<elements.size();i++){
			sb.append((elements.get(i)).toString());
		}

	}
	
	@Override
	public boolean equals(Object obj) {
		 if (!(obj instanceof Node)) { 
	         return false; 
	     }
		 return this.toString().equals(obj.toString());
	}
	
}
