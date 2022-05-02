package com.example.migroscompass;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.GeomagneticField;

import android.hardware.GeomagneticField;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;


import com.google.android.gms.maps.model.LatLng;

import org.jsfr.json.Collector;
import org.jsfr.json.GsonParser;
import org.jsfr.json.JsonPathListener;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferGson;
import org.jsfr.json.ParsingContext;
import org.jsfr.json.ValueBox;
import org.jsfr.json.compiler.JsonPathCompiler;
import org.jsfr.json.provider.GsonProvider;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;



public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    TextView txtLat;
    TextView Status;
    String lat;
    String provider;
    Location location;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    String msg = "Android : ";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LatLng loc;
    List<migros> migrosArrayList = new ArrayList<migros>();
    Thread t;
    boolean LoadedMigis;
    DecimalFormat df = new DecimalFormat("#.#");
    SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    Sensor sensorGravity;
    Sensor sensorMagnetic;
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    private float[] smoothed = new float[3];
    float heading;
    float magneticDeclination = 0;
    float trueHeading;
    float oldHeading;
    ImageView imageViewCompass;
    float bearToNearest;
    float MigrosHeading;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        txtLat = (TextView) findViewById(R.id.my_textview);
        parseJson();
    }

    public void onStart() {
        super.onStart();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove listeners
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
    }


    public void parseJson() {

        t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {

                String jsonFileString = json.getJsonFromAssets(getApplicationContext(), "migros_data_conv.json");
                JsonSurfer surfer = json.getSurfer();
                LoadedMigis = false;
                for (int i = 0; i < json.getCount(jsonFileString, surfer, "$.stores[*]"); i++) {
                    //ValueBox<String> id = collector.collectOne("$.stores["+i+"].id", String.class);
                    Collector collector = surfer.collector(jsonFileString);
                    ValueBox<String> name = collector.collectOne("$.stores["+i+"].name", String.class);
                    ValueBox<String> lat = collector.collectOne("$.stores["+i+"].lat", String.class);
                    ValueBox<String> lon = collector.collectOne("$.stores["+i+"].lon", String.class);
                    ValueBox<String> type = collector.collectOne("$.stores["+i+"].type", String.class);
                    collector.exec();

                    migros currentMigros = new migros(i , name.get(), Double.parseDouble(lat.get()), Double.parseDouble(lon.get()), type.get(), 0, 0);
                    migrosArrayList.add(currentMigros);

                }
                LoadedMigis = true;
                migros migi =  Collections.min(migrosArrayList, Comparator.comparing(m -> m.dist));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Status = (TextView) findViewById(R.id.status);
                        Status.setText(R.string.loaded);

                    }
                });

            }
        });
            t.start();
            t.setName("migros");
            t.setPriority(Thread.MAX_PRIORITY);
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationChanged(Location location) {
        loc = new LatLng(location.getLatitude(),location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        txtLat = (TextView) findViewById(R.id.my_textview);
        txtLat.setText(loc.toString());
        magneticDeclination = new GeomagneticField((float) latitude,(float) longitude,(float) altitude,System.currentTimeMillis()/1000).getDeclination();
        if(LoadedMigis) {
            for (int i = 0; i < migrosArrayList.size(); i++) {
                migros cur_migros = migrosArrayList.get(i);
                LatLng MigLoc = new LatLng(cur_migros.lat, cur_migros.lon);
                cur_migros.dist = computeDistanceBetween(loc, MigLoc);
                cur_migros.bear = computeHeading(loc, MigLoc);
                migrosArrayList.set(i, cur_migros);
            }
            migros nearest_migi = Collections.min(migrosArrayList, Comparator.comparing(m -> m.dist));
            TextView nearest_migi_text = (TextView) findViewById(R.id.Nearest_Migi);
            nearest_migi_text.setText(nearest_migi.name+ " at " + df.format(nearest_migi.dist/1000) + " Km \nBearing: " + df.format(nearest_migi.bear)+ "°");
            bearToNearest = (float) nearest_migi.bear;

        }
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


    public void onSensorChanged(SensorEvent event) {

// get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //make sensor readings smoother using a low pass filter
            lowPassFilter(event.values.clone(), accelerometerReading);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            //make sensor readings smoother using a low pass filter
            lowPassFilter(event.values.clone(), magnetometerReading);
        }
        updateHeading();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public static float calculateHeading(float[] accelerometerReading, float[] magnetometerReading) {
        float Ax = accelerometerReading[0];
        float Ay = accelerometerReading[1];
        float Az = accelerometerReading[2];

        float Ex = magnetometerReading[0];
        float Ey = magnetometerReading[1];
        float Ez = magnetometerReading[2];

        //cross product of the magnetic field vector and the gravity vector
        float Hx = Ey * Az - Ez * Ay;
        float Hy = Ez * Ax - Ex * Az;
        float Hz = Ex * Ay - Ey * Ax;

        //normalize the values of resulting vector
        final float invH = 1.0f / (float) Math.sqrt(Hx * Hx + Hy * Hy + Hz * Hz);
        Hx *= invH;
        Hy *= invH;
        Hz *= invH;

        //normalize the values of gravity vector
        final float invA = 1.0f / (float) Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
        Ax *= invA;
        Ay *= invA;
        Az *= invA;

        //cross product of the gravity vector and the new vector H
        final float Mx = Ay * Hz - Az * Hy;
        final float My = Az * Hx - Ax * Hz;
        final float Mz = Ax * Hy - Ay * Hx;

        //arctangent to obtain heading in radians
        return (float) Math.atan2(Hy, My);
    }


    public static float convertRadtoDeg(float rad) {
        return (float) (rad / Math.PI) * 180;
    }

    //map angle from [-180,180] range to [0,360] range
    public static float map180to360(float angle) {
        return (angle + 360) % 360;
    }

    public static final float ALPHA = 0.15f;

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void updateHeading() {
        //oldHeading required for image rotate animation
        oldHeading = MigrosHeading;

        heading = calculateHeading(accelerometerReading, magnetometerReading);
        heading = convertRadtoDeg(heading);
        heading = map180to360(heading);

            trueHeading = heading + magneticDeclination;
            if(trueHeading > 360) { //if trueHeading was 362 degrees for example, it should be adjusted to be 2 degrees instead
                trueHeading = trueHeading - 360;
            }

        imageViewCompass = (ImageView) findViewById(R.id.compass_needle);

        MigrosHeading = trueHeading - bearToNearest;

        RotateAnimation rotateAnimation = new RotateAnimation(-oldHeading, -MigrosHeading, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);
        if(imageViewCompass != null) {

            imageViewCompass.startAnimation(rotateAnimation);

        }
    }


}




