package com.android.eloy.jsoupdemo.reader.source;

public class Content{
	private String xpath;

	public void setXpath(String xpath){
		this.xpath = xpath;
	}

	public String getXpath(){
		return xpath;
	}

	@Override
 	public String toString(){
		return 
			"Content{" + 
			"xpath = '" + xpath + '\'' + 
			"}";
		}
}
