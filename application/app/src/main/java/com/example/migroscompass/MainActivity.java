package com.example.migroscompass;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.heatmaps.WeightedLatLng;

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

public class MainActivity extends AppCompatActivity implements LocationListener {

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



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtLat = (TextView) findViewById(R.id.my_textview);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void calculate(View view) {
        Status = (TextView) findViewById(R.id.status);

        runthread();
        Status.setText(R.string.loaded);
        System.out.println("loaded");

    }

    private void runthread() {

        t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {

                String jsonFileString = json.getJsonFromAssets(getApplicationContext(), "migros_data_conv.json");
                JsonSurfer surfer = json.getSurfer();

                for (int i = 0; i < json.getCount(jsonFileString, surfer, "$.stores[*]"); i++) {
                    //ValueBox<String> id = collector.collectOne("$.stores["+i+"].id", String.class);
                    Collector collector = surfer.collector(jsonFileString);
                    ValueBox<String> name = collector.collectOne("$.stores["+i+"].name", String.class);
                    ValueBox<String> lat = collector.collectOne("$.stores["+i+"].lat", String.class);
                    ValueBox<String> lon = collector.collectOne("$.stores["+i+"].lon", String.class);
                    ValueBox<String> type = collector.collectOne("$.stores["+i+"].type", String.class);
                    collector.exec();
                    LatLng MigLoc = new LatLng(Double.parseDouble(lat.get()), Double.parseDouble(lon.get()));
                    double dist = computeDistanceBetween(loc, MigLoc);
                    double bear = computeHeading(loc, MigLoc);
                    migros currentMigros = new migros(i , name.get(), Double.parseDouble(lat.get()), Double.parseDouble(lon.get()), type.get(), dist, bear);
                    migrosArrayList.add(currentMigros);
                    String finalI = String.valueOf(i);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Status.setText(finalI);

                        }
                    });

                }
                migros migi =  Collections.min(migrosArrayList, Comparator.comparing(m -> m.dist));
                System.out.println(migi.name);
                System.out.println(json.getCount(jsonFileString, surfer, "$.stores[*]"));


            }
        });
            t.start();
            t.setName("migros");
            t.setPriority(Thread.MAX_PRIORITY);
    }




    @Override
    public void onLocationChanged(Location location) {
        loc = new LatLng(location.getLatitude(),location.getLongitude());

        txtLat = (TextView) findViewById(R.id.my_textview);
        txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());


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
}




