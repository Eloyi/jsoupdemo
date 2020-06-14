package com.android.eloy.jsoupdemo;

import android.app.Application;
import android.content.Context;

import com.android.eloy.jsoupdemo.reader.source.db.MyMigration;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {
    public static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        //init realm
        Realm.init(this);
    }

    /**
     * if use same configutation need use same realm
     */
    public static Realm getRealm(){
        if (realm == null) {
            RealmConfiguration myConfig = new RealmConfiguration.Builder()
                    .name("myrealm.realm")
                    .migration(new MyMigration())
                    .schemaVersion(2)
                    .build();

            realm = Realm.getInstance(myConfig);
        }

        return realm;
    }
}
