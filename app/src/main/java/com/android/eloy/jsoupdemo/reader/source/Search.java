package com.android.eloy.jsoupdemo.reader.source;

public class Search{
	private String charset;
	private String coverXpath;
	private String xpath;
	private String descXpath;
	private String linkXpath;
	private String titleXpath;
	private String authorXpath;

	public void setCharset(String charset){
		this.charset = charset;
	}

	public String getCharset(){
		return charset;
	}

	public void setCoverXpath(String coverXpath){
		this.coverXpath = coverXpath;
	}

	public String getCoverXpath(){
		return coverXpath;
	}

	public void setXpath(String xpath){
		this.xpath = xpath;
	}

	public String getXpath(){
		return xpath;
	}

	public void setDescXpath(String descXpath){
		this.descXpath = descXpath;
	}

	public String getDescXpath(){
		return descXpath;
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

	public void setAuthorXpath(String authorXpath){
		this.authorXpath = authorXpath;
	}

	public String getAuthorXpath(){
		return authorXpath;
	}

	@Override
 	public String toString(){
		return 
			"Search{" + 
			"charset = '" + charset + '\'' + 
			",coverXpath = '" + coverXpath + '\'' + 
			",xpath = '" + xpath + '\'' + 
			",descXpath = '" + descXpath + '\'' + 
			",linkXpath = '" + linkXpath + '\'' + 
			",titleXpath = '" + titleXpath + '\'' + 
			",authorXpath = '" + authorXpath + '\'' + 
			"}";
		}
}
