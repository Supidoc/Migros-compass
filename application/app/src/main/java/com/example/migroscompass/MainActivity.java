package com.example.migroscompass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView Status;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Thread t;
    boolean loadedMigis = false;
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
    boolean proc = false;
    migros selected_migi;

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
    boolean inflates = false;
    location location;


    public MainActivity() {
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = new location(this,MainActivity.this, this);
        location.newLocationManager(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        setContentView(R.layout.activity_main);
        createParseJson();
        /*if (savedInstanceState != null) {
            proc = savedInstanceState.getBoolean("proc");
            if (!loadedMigis) {
                loadedMigis = savedInstanceState.getBoolean("loadedMigis");
            }
        }*/
        if (!proc) {
            parseJson();
            proc = true;
        }

    }

    @Override
    protected final void onSaveInstanceState(@NonNull final Bundle outState) {
        // Save variables.
        /*super.onSaveInstanceState(outState);
        outState.putBoolean("proc", proc);
        outState.putBoolean("loadedMigis", loadedMigis);
*/

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createParseJson() {
        t = new Thread(() -> {

            String jsonFileString = json.getJsonFromAssets(getApplicationContext(), "migros_data_conv.json");

            ObjectMapper mapper = new ObjectMapper();
            try {


                compass.migrosArrayList = Arrays.asList(mapper.readValue(jsonFileString, migros[].class));
                compass.loadedMigis = true;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                loadedMigis = true;


                Status = findViewById(R.id.status);
                Status.setText(R.string.loaded);

            });

            nearest_migi(location.loc,this);
            Log.i("Num","1");
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onStart() {
        super.onStart();
        location = new location(this,this, this );

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



            location.startLocationUpdates(this );

        }

    }





    @Override
    protected void onStop() {
        super.onStop();
        location.locationManagerRemoveUpdates(this);

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
        location.locationManagerRemoveUpdates(this);
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
    Context mContext = this;

    public void nearest_migi(LatLng loc, Activity activity) {
        if (loc != null && compass.loadedMigis) {
            compass.createMigrosArrayList(loc);
            Collections.sort(compass.migrosArrayList, new CustomComparator());
            selected_migi = compass.migrosArrayList.get(selected_i);
            runOnUiThread(() -> {

                TextView selected_migi_text = activity.findViewById(R.id.Selected_Migi);
                TextView selected_migi_dist_text = activity.findViewById(R.id.Selected_Migi_dist);
                selected_migi_text.setText(selected_migi.name);
                selected_migi_dist_text.setText(df.format(selected_migi.dist / 1000) + " Km");
                if(!inflates){
                    inflates = true;
                    ConstraintLayout container = activity.findViewById(R.id.compass);

                    ImageView img = activity.findViewById(R.id.compass_m_icon);

                    for (int i = 0; i < 5; i++) {
                        View view = LayoutInflater.from(activity).inflate(R.layout.imageview_m_icon,container,true);
                        ImageView img2 = activity.findViewById(R.id.compass_m_icon_inf);
                        img2.setId(1000+i);
                        m_icons[i] = img2;
                    }

                    Log.i("asdf", String.valueOf(m_icons));
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
        nearest_migi(location.loc,this );
        Log.i("Num","2");
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

        RotateAnimation rotateAnimationIcons;
        if(inflates){
            int o = 0;
            for (int i = 0;  i < m_icons.length+1; i++) {
                float Cur_bear = (float) compass.migrosArrayList.get(i).bear;
                rotateAnimationIcons = new RotateAnimation(-oldHeading + Cur_bear, -trueHeading + Cur_bear, Animation.RELATIVE_TO_SELF, 0.5f, Animation.ABSOLUTE, imageViewCompass.getPivotY());
                rotateAnimationIcons.setDuration(1);
                rotateAnimationIcons.setFillAfter(true);
                if(compass.migrosArrayList.get(i)==selected_migi){

                    if(imageViewMigros != null) {

                        imageViewMigros.startAnimation(rotateAnimationIcons);

                    }

                }else {

                    double distFactor = 0.65;
                    double alphaFactor = 0.8;
                    if(compass.migrosArrayList.get(i).dist<800){

                        m_icons[o].setScaleX((float) (1*distFactor));
                        m_icons[o].setScaleY((float) (1*distFactor));
                        m_icons[o].setAlpha((float) (1*alphaFactor));

                    } else if(compass.migrosArrayList.get(i).dist<3330){

                        m_icons[o].setScaleX((float) (0.8*distFactor));
                        m_icons[o].setScaleY((float) (0.8*distFactor));
                        m_icons[o].setAlpha((float) (0.8*alphaFactor));

                    } else if(compass.migrosArrayList.get(i).dist<8330){

                        m_icons[o].setScaleX((float) (0.6*distFactor));
                        m_icons[o].setScaleY((float) (0.6*distFactor));
                        m_icons[o].setAlpha((float) (0.6*alphaFactor));

                    } else{

                        m_icons[o].setScaleX((float) (0.4*distFactor));
                        m_icons[o].setScaleY((float) (0.4*distFactor));
                        m_icons[o].setAlpha((float) (0.4*alphaFactor));

                    }
                    m_icons[o].startAnimation(rotateAnimationIcons);

                    o++;
                }
        }
        }
    }


}




