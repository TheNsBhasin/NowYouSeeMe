package com.nsbhasin.nowyouseeme.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nsbhasin.nowyouseeme.data.Location;
import com.nsbhasin.nowyouseeme.data.LocationRepository;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {
    private LocationRepository mRepository;
    private LiveData<List<Location>> mLocations;

    public LocationViewModel(@NonNull Application application) {
        super(application);

        mRepository = new LocationRepository(application);
        mLocations = mRepository.getLocations();
    }

    public LiveData<List<Location>> getLocations() {
        return mLocations;
    }

    public void insert(Location location) {
        mRepository.insert(location);
    }
}
