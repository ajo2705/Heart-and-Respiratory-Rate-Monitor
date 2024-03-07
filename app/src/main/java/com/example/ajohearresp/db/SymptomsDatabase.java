/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ajohearresp.schema.HeartRespData;

//DATABASE FOR STORING AND RETRIEVING DATA
@Database(entities = {HeartRespData.class}, version = 1, exportSchema = false)
public abstract class SymptomsDatabase extends RoomDatabase {
    public abstract HeartRespDao heartRespDao();

    private static volatile SymptomsDatabase INSTANCE;

    public static SymptomsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SymptomsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    SymptomsDatabase.class, "Symptoms")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

