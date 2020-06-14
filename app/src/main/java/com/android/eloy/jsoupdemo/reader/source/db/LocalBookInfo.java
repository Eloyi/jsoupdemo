package com.android.eloy.jsoupdemo.reader.source.db;

import android.os.SystemClock;
import android.util.Log;

import java.io.Serializable;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * all pojo want to save to db need extends RealmObject
 * check saveToDB(),deleteFromDB()
 */
public class LocalBookInfo extends RealmObject implements Serializable {

    private static final long serialVersionUID = 2846959175386797400L;


    //use this identify book from where
    public String sourceKey;
    @PrimaryKey
    public String bookNum;

    public String bookName;

    public String author;

    //we need use categorylink get categorylist
    public String categorylink;

    public int currentChapter;

    public int currentPage;

    public long lastWatchTime;

    public String extra1;

    //Realm need this but do not use this for coding
    public LocalBookInfo(){}

    public LocalBookInfo(String bookNum){
        this.bookNum = bookNum;
    }

    @Override
    public String toString() {
        return "LocalBookInfo{" +
                "sourceKey='" + sourceKey + '\'' +
                ", bookNum='" + bookNum + '\'' +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", categorylink='" + categorylink + '\'' +
                ", currentChapter=" + currentChapter +
                ", currentPage=" + currentPage +
                ", lastWatchTime=" + lastWatchTime +
                ", extra1='" + extra1 + '\'' +
                '}';
    }

    public void saveToDB(Realm realm) {
        realm.beginTransaction();
        this.lastWatchTime = System.currentTimeMillis();
        realm.copyToRealmOrUpdate(this);
        realm.commitTransaction();
    }

    public void deleteFromDB(Realm realm) {
        final RealmResults<LocalBookInfo> resultList = realm.where(LocalBookInfo.class).equalTo("bookNum", bookNum).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // remove single match
                resultList.deleteAllFromRealm();
            }
        });
    }

    public static void getLocalBookInfoList(Realm realm) {
        final RealmResults<LocalBookInfo> resultList = realm.where(LocalBookInfo.class).findAll();

        for (LocalBookInfo localBookInfo : resultList) {
            Log.e("test", "localBookInfo" + localBookInfo);
        }
    }

    public static void deleteAll(Realm realm){
        final RealmResults<LocalBookInfo> resultList = realm.where(LocalBookInfo.class).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // remove single match
                resultList.deleteAllFromRealm();
            }
        });
    }
}
