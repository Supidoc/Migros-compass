package com.example.migroscompass;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    protected LocationManager locationManager;
    TextView Status;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LatLng loc;
    Thread t;
    boolean LoadedMigis = false;
    static DecimalFormat df = new DecimalFormat("#.#");
    SensorManager mSensorManager;

    float heading;
    float magneticDeclination = 0;
    float trueHeading;
    float oldHeading;
    ImageView imageViewCompass;
    float bearToNearest;
    float MigrosHeading;
    float oldHeadingMigros;
    ImageView imageViewMigros;
    public List<migros> migrosArrayList = new ArrayList<>();
    boolean proc = false;
    migros selected_migi;
    boolean loadedMigis = false;
    int selected_i = 0;

    int mAzimuth;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private final float[] mLastAccelerometer = new float[3];
    private final float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    ImageView[] m_icons = new ImageView[5];
    boolean click = false;
    boolean inflates = false;

    public MainActivity() {
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setContentView(R.layout.activity_main);
        createParseJson();
        if (savedInstanceState != null) {
            proc = savedInstanceState.getBoolean("proc");
            if (!loadedMigis) {
                loadedMigis = savedInstanceState.getBoolean("loadedMigis");
            }
        }
        if (!proc) {
            parseJson();
            proc = true;
        }

    }

    @Override
    protected final void onSaveInstanceState(@NonNull final Bundle outState) {
        // Save variables.
        super.onSaveInstanceState(outState);
        outState.putBoolean("proc", proc);
        outState.putBoolean("loadedMigis", loadedMigis);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createParseJson() {
        t = new Thread(() -> {

            String jsonFileString = json.getJsonFromAssets(getApplicationContext(), "migros_data_conv.json");

            ObjectMapper mapper = new ObjectMapper();
            try {


                migrosArrayList = Arrays.asList(mapper.readValue(jsonFileString, migros[].class));

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }


           nearest_migi();



            runOnUiThread(() -> {
                loadedMigis = true;
                Status = findViewById(R.id.status);
                Status.setText(R.string.loaded);

            });

        });
    }

    public void onStart() {
        super.onStart();
        TextView nearest_migi_text = findViewById(R.id.Selected_Migi);
        nearest_migi_text.setText(R.string.loadingLocation);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            onStart();
        } else {

            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
                if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                    System.out.println("no Sensors");
                }
                else {
                    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                    haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                    haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
                }
            }
            else{
                mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
            }



            startLocationUpdates();

        }

    }


    protected void startLocationUpdates() {

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
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }




    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }
    }

    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }

    }




    public void parseJson() {

            t.start();
            t.setName("migros");
            t.setPriority(Thread.MAX_PRIORITY);
    }

    public void nearest_migi() {

        if (loc != null && loadedMigis) {
            for (int i = 0; i < migrosArrayList.size(); i++) {
                migros cur_migros = migrosArrayList.get(i);
                LatLng MigLoc = new LatLng(cur_migros.lat, cur_migros.lon);
                cur_migros.dist = computeDistanceBetween(loc, MigLoc);
                cur_migros.bear = computeHeading(loc, MigLoc);
                migrosArrayList.set(i, cur_migros);
            }
            Collections.sort(migrosArrayList, new CustomComparator());
            selected_migi = migrosArrayList.get(selected_i);
            runOnUiThread(() -> {
                TextView selected_migi_text = findViewById(R.id.Selected_Migi);
                TextView selected_migi_dist_text = findViewById(R.id.Selected_Migi_dist);
                selected_migi_text.setText(selected_migi.name);
                selected_migi_dist_text.setText(df.format(selected_migi.dist / 1000) + " Km");
                if(!inflates){

                    ConstraintLayout container = findViewById(R.id.compass);

                    ImageView img = findViewById(R.id.compass_m_icon);

                    for (int i = 0; i < 5; i++) {
                        View view = LayoutInflater.from(this).inflate(R.layout.imageview_m_icon,container,true);
                        ImageView img2 = findViewById(R.id.compass_m_icon_inf);
                        img2.setId(1000+i);
                        m_icons[i] = img2;
                    }
                    inflates = true;
                }
            });
            bearToNearest = (float) selected_migi.bear;


        }


    }

    public void switch_migros(View view)
    {
        if(selected_i>4){
            selected_i=0;
        }
        else{

            selected_i++;

        }
        nearest_migi();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationChanged(Location location) {
        loc = new LatLng(location.getLatitude(),location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        magneticDeclination = new GeomagneticField((float) latitude,(float) longitude,(float) altitude,System.currentTimeMillis()/1000).getDeclination();

        nearest_migi();
    }


    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        heading = Math.round(mAzimuth);
        updateHeading();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }




    private void updateHeading() {
        //oldHeading required for image rotate animation
        oldHeading = trueHeading;
        oldHeadingMigros = MigrosHeading;
            trueHeading = heading + magneticDeclination;
            if(trueHeading > 360) { //if trueHeading was 362 degrees for example, it should be adjusted to be 2 degrees instead
                trueHeading = trueHeading - 360;
            }

        imageViewCompass = findViewById(R.id.compass_needle);
        imageViewMigros = findViewById(R.id.compass_m_icon);

        MigrosHeading = trueHeading - bearToNearest;
        if(MigrosHeading > 360) { //if trueHeading was 362 degrees for example, it should be adjusted to be 2 degrees instead
            MigrosHeading = MigrosHeading - 360;
        }


        RotateAnimation rotateAnimationCompass = new RotateAnimation(-oldHeading, -trueHeading, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimationCompass.setDuration(1);
        rotateAnimationCompass.setFillAfter(true);
        if(imageViewCompass != null) {

            imageViewCompass.startAnimation(rotateAnimationCompass);

        }
        RotateAnimation rotateAnimationMigros = new RotateAnimation(-oldHeadingMigros, -MigrosHeading, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, imageViewCompass.getPivotY());
        rotateAnimationMigros.setDuration(1);
        rotateAnimationMigros.setFillAfter(true);
        if(imageViewMigros != null) {

            imageViewMigros.startAnimation(rotateAnimationMigros);

        }
        RotateAnimation rotateAnimationIcons;
        if(inflates){
            for (int i = 0; i < m_icons.length; i++) {
                float Cur_bear = (float) migrosArrayList.get(i+1).bear;
                rotateAnimationIcons = new RotateAnimation(-oldHeading+Cur_bear, -trueHeading+Cur_bear, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, imageViewCompass.getPivotY());
                rotateAnimationIcons.setDuration(1);
                rotateAnimationIcons.setFillAfter(true);
                m_icons[i].startAnimation(rotateAnimationIcons);
        }
        }
    }


}




