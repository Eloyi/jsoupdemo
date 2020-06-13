package com.android.eloy.jsoupdemo.reader.source.db

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

class MyMigration : RealmMigration{
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Log.e("test", "migrate old $oldVersion new $newVersion")
        val schema = realm.schema

        var migrationVersion = oldVersion.toInt()

//        if (migrationVersion == 0) {
//            schema.create("LocalBookInfo")
//                .addField("lastWatchTime", String::class.java)
//            migrationVersion++
//        }

        if (migrationVersion == 1){
            schema.get("LocalBookInfo")?.addField("extra1", String::class.java)
        }
    }
}