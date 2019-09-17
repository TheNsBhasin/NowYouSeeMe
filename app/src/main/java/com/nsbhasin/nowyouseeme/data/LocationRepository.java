package com.nsbhasin.nowyouseeme.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LocationRepository {
    private LocationDao mLocationDao;
    private LiveData<List<Location>> mLocations;


    public LocationRepository(Application application) {
        LocationDatabase db = LocationDatabase.getInstance(application);
        mLocationDao = db.locationDao();
        mLocations = mLocationDao.getAll();
    }

    public LiveData<List<Location>> getLocations() {
        return mLocations;
    }

    public void insert(Location location) {
        new insertAsyncTask(mLocationDao).execute(location);
    }

    private static class insertAsyncTask extends AsyncTask<Location, Void, Void> {

        private LocationDao mAsyncTaskDao;

        insertAsyncTask(LocationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Location... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
