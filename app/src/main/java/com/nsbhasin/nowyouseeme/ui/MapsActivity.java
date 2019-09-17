package com.nsbhasin.nowyouseeme.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nsbhasin.nowyouseeme.R;
import com.nsbhasin.nowyouseeme.data.Location;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    private LocationViewModel mLocationViewModel;
    private List<Location> mLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mLocationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

        mLocationViewModel.getLocations().observe(this, new Observer<List<Location>>() {
            @Override
            public void onChanged(List<com.nsbhasin.nowyouseeme.data.Location> locations) {
                mLocations = locations;
                Log.i(TAG, String.valueOf(mLocations));

                if (mMap == null) {
                    return;
                }

                mMap.clear();

                for (Location location : mLocations) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(position)
                            .title(DateFormat.getTimeInstance().format(location.getTimeStamp())));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocations == null) {
            return;
        }

        for (Location location : mLocations) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        }
    }
}
