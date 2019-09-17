package com.nsbhasin.nowyouseeme.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.nsbhasin.nowyouseeme.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES";
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_LAST_UPDATED_TIME_STRING = "KEY_LAST_UPDATED_TIME_STRING";

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;

    private Boolean mRequestingLocationUpdates;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    private FusedLocationProviderClient mFusedLocationClient;

    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private Button mOpenGoogleMaps;

    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeText;

    private LocationViewModel mLocationViewModel;
    private List<com.nsbhasin.nowyouseeme.data.Location> mLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

        mLocationViewModel.getLocations().observe(this, new Observer<List<com.nsbhasin.nowyouseeme.data.Location>>() {
            @Override
            public void onChanged(List<com.nsbhasin.nowyouseeme.data.Location> locations) {
                mLocations = locations;
                Log.i(TAG, String.valueOf(mLocations));
            }
        });

        mStartUpdatesButton = findViewById(R.id.start_updates_button);
        mStopUpdatesButton = findViewById(R.id.stop_updates_button);
        mOpenGoogleMaps = findViewById(R.id.open_maps);

        mLatitudeText = findViewById(R.id.latitude_text);
        mLongitudeText = findViewById(R.id.longitude_text);
        mLastUpdateTimeText = findViewById(R.id.last_update_time_text);

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        setUpOnClickHandlers();

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        outState.putParcelable(KEY_LOCATION, mCurrentLocation);
        outState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPermissions()) {
            requestPermissions();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i(TAG, "User agreed to make required location settings changes.");
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(TAG, "User chose not to make required location settings changes.");
                    mRequestingLocationUpdates = false;
                    updateUI();
                    break;
            }
        }
    }

    private void setUpOnClickHandlers() {
        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRequestingLocationUpdates) {
                    mRequestingLocationUpdates = true;
                    setButtonsEnabledState();
                    startLocationUpdates();
                }
            }
        });

        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRequestingLocationUpdates) {
                    mRequestingLocationUpdates = false;
                    setButtonsEnabledState();
                    stopLocationUpdates();
                }
            }
        });

        mOpenGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                Date currentTime = new Date();
                mLastUpdateTime = DateFormat.getTimeInstance().format(currentTime);

                com.nsbhasin.nowyouseeme.data.Location location = new com.nsbhasin.nowyouseeme.data.Location(currentTime,
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude());

                updateLocationDb(location);
                updateLocationUI();
            }
        };
    }

    private void updateLocationDb(com.nsbhasin.nowyouseeme.data.Location location) {
        mLocationViewModel.insert(location);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    Activity#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for Activity#requestPermissions for more details.
                                return;
                            }
                        }

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade location settings");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
        }

        if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
            mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
        }

        updateUI();
    }

    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStopUpdatesButton.setEnabled(false);
            mStartUpdatesButton.setEnabled(true);
        }
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));
            mLongitudeText.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
            mLastUpdateTimeText.setText(String.format(Locale.ENGLISH, "%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }
}
