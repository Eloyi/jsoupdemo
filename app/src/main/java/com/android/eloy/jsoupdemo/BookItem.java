package com.android.eloy.jsoupdemo;

public class BookItem {
    public String title;
    public String baseUrl;
    public String link;

    public BookItem(String title, String baseUrl, String link) {
        this.title = title;
        this.baseUrl = baseUrl;
        this.link = link;
    }

    @Override
    public String toString() {
        return "BookItem{" +
                "title='" + title + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
