package com.android.eloy.jsoupdemo.reader.source;

public class SourceRegx{
	private Search search;
	private int access;
	private boolean enable;
	private Catalog catalog;
	private String searchUrl;
	private String id;
	private String key;
	private Content content;

	public void setSearch(Search search){
		this.search = search;
	}

	public Search getSearch(){
		return search;
	}

	public void setAccess(int access){
		this.access = access;
	}

	public int getAccess(){
		return access;
	}

	public void setEnable(boolean enable){
		this.enable = enable;
	}

	public boolean isEnable(){
		return enable;
	}

	public void setCatalog(Catalog catalog){
		this.catalog = catalog;
	}

	public Catalog getCatalog(){
		return catalog;
	}

	public void setSearchUrl(String searchUrl){
		this.searchUrl = searchUrl;
	}

	public String getSearchUrl(){
		return searchUrl;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setKey(String key){
		this.key = key;
	}

	public String getKey(){
		return key;
	}

	public void setContent(Content content){
		this.content = content;
	}

	public Content getContent(){
		return content;
	}

	@Override
 	public String toString(){
		return 
			"SourceRegx{" + 
			"search = '" + search + '\'' + 
			",access = '" + access + '\'' + 
			",enable = '" + enable + '\'' + 
			",catalog = '" + catalog + '\'' + 
			",searchUrl = '" + searchUrl + '\'' + 
			",id = '" + id + '\'' + 
			",key = '" + key + '\'' + 
			",content = '" + content + '\'' + 
			"}";
		}
}
