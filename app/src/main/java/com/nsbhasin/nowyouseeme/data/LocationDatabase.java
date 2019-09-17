package com.nsbhasin.nowyouseeme.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Location.class}, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();

    private static final String LOCATION_DB_NAME = "location.db";

    private static volatile LocationDatabase locationDatabase;


    public static LocationDatabase getInstance(Context context) {
        if (locationDatabase == null) {
            synchronized (LocationDatabase.class) {
                if (locationDatabase == null) {
                    locationDatabase = Room.databaseBuilder(context.getApplicationContext(), LocationDatabase.class, LOCATION_DB_NAME).build();
                }
            }
        }

        return locationDatabase;
    }
}
