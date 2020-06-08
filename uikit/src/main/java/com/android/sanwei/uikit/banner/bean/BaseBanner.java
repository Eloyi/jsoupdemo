package com.android.sanwei.uikit.banner.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseBanner implements Serializable {

    private static final long serialVersionUID = 4777273692096731964L;

    public Integer imageRes;
    public String imageUrl;
    public String title;
    public int viewType;

    public BaseBanner(Integer imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public BaseBanner(String imageUrl, String title, int viewType) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.viewType = viewType;
    }

    public static List<BaseBanner> getTestData3() {
        List<BaseBanner> list = new ArrayList<>();
        list.add(new BaseBanner("https://img.zcool.cn/community/011ad05e27a173a801216518a5c505.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/0148fc5e27a173a8012165184aad81.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/013c7d5e27a174a80121651816e521.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/01b8ac5e27a173a80120a895be4d85.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/01a85d5e27a174a80120a895111b2c.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/01085d5e27a174a80120a8958791c4.jpg", null, 1));
        list.add(new BaseBanner("https://img.zcool.cn/community/01f8735e27a174a8012165188aa959.jpg", null, 1));
        return list;
    }


    @Override
    public String toString() {
        return "BaseBanner{" +
                "imageRes=" + imageRes +
                ", imageUrl='" + imageUrl + '\'' +
                ", title='" + title + '\'' +
                ", viewType=" + viewType +
                '}';
    }
}
