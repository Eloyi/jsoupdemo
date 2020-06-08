package com.android.eloy.jsoupdemo.reader.response;

import java.io.Serializable;

public class Chapter implements Serializable {

    public String sourceKey;

    public String title;

    public String link;

    public String baseUrl;

    public Chapter(String sourceKey, String title, String link, String baseUrl) {
        this.sourceKey = sourceKey;
        this.title = title;
        this.link = link;
        this.baseUrl = baseUrl;
    }
}