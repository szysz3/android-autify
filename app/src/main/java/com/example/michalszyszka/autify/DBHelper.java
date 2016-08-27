package com.example.michalszyszka.autify;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michalszyszka on 14.03.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "wimp.db", null, 22);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<Pair<String, String>> getAllOfflinePlaylists(){
        List<Pair<String, String>> resultList = new ArrayList<>();

        String query = "SELECT title, uuid FROM playlists WHERE isOffline = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do{
                resultList.add(new Pair<String, String>(cursor.getString(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        return resultList;
    }
}
