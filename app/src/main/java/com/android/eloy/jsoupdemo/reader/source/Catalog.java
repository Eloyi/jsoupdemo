package com.android.eloy.jsoupdemo.reader.source;

public class Catalog{
	private String xpath;
	private String linkXpath;
	private String titleXpath;

	public void setXpath(String xpath){
		this.xpath = xpath;
	}

	public String getXpath(){
		return xpath;
	}

	public void setLinkXpath(String linkXpath){
		this.linkXpath = linkXpath;
	}

	public String getLinkXpath(){
		return linkXpath;
	}

	public void setTitleXpath(String titleXpath){
		this.titleXpath = titleXpath;
	}

	public String getTitleXpath(){
		return titleXpath;
	}

	@Override
 	public String toString(){
		return 
			"Catalog{" + 
			"xpath = '" + xpath + '\'' + 
			",linkXpath = '" + linkXpath + '\'' + 
			",titleXpath = '" + titleXpath + '\'' + 
			"}";
		}
}
