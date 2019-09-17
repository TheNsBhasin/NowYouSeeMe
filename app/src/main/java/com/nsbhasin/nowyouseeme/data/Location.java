package com.nsbhasin.nowyouseeme.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity
@TypeConverters(DateConverter.class)
public class Location {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "time_stamp")
    public Date timeStamp;

    public double latitude;

    public double longitude;

    public Location(Date timeStamp, double latitude, double longitude) {
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "[" + String.valueOf(id) + " " + String.valueOf(timeStamp) + " " + String.valueOf(latitude) + " " + String.valueOf(longitude) + "]";
    }
}
