package com.example.migroscompass;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

@RequiresApi(api = Build.VERSION_CODES.N)
public class location extends MainActivity implements LocationListener {
    static LocationManager manager;
    Context mContext;
    LatLng loc = null;
    public Activity activity;
    MainActivity mainActivity;
    protected float magneticDeclination;

    public location(Context _mContext,Activity _activity,MainActivity _mainActivity) {

        mContext = _mContext;
        this.activity = _activity;
        mainActivity = _mainActivity;

    }

    public void newManager(Context mContext) {

        manager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

    }

    public void removeUpdates() {

        manager.removeUpdates(this);

    }


    protected void startUpdates(Context mContext) {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(20000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(mContext);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && androidx.core.app.ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());

    }

    public void onLocationChanged(Location location) {
        loc = new LatLng(location.getLatitude(),location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        magneticDeclination = new GeomagneticField((float) latitude,(float) longitude,(float) altitude,System.currentTimeMillis()/1000).getDeclination();
        mainActivity.nearest_migi(loc, this.activity);

        Log.i("Num","3");
    }
}
