package com.nsbhasin.nowyouseeme.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationDao {
    @Query("SELECT * FROM location ORDER BY time_stamp ASC")
    LiveData<List<Location>> getAll();

    @Insert
    void insert(Location location);

    @Insert
    void insertAll(Location... locations);

    @Delete
    void delete(Location location);
}
