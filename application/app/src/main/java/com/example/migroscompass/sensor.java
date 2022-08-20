package com.example.migroscompass;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

@RequiresApi(api = Build.VERSION_CODES.N)
public class sensor extends MainActivity implements SensorEventListener {

    Activity activity;
    MainActivity mainActivity;
    private Sensor mAccelerometer, mMagnetometer, mRotationV;
    int mAzimuth;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private final float[] mLastAccelerometer = new float[3];
    private final float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float trueHeading;
    Context mContext;

    public sensor(Context _mContext, Activity _activity, MainActivity _mainActivity) {

        this.mContext = _mContext;
        this.activity = _activity;
        mainActivity = _mainActivity;


    }

    SensorManager mSensorManager;
    public void newManager() {

        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);

    }

    public void register() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && androidx.core.app.ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            mainActivity.onStart();
        }

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

    }

    public void unregister(){

        if(haveSensor && haveSensor2){
            mSensorManager.unregisterListener(this,mAccelerometer);
            mSensorManager.unregisterListener(this,mMagnetometer);
        }
        else{
            if(haveSensor)
                mSensorManager.unregisterListener(this,mRotationV);
        }

    }

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

        int heading = Math.round(mAzimuth);
        updateHeading(heading);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateHeading(float heading) {
        //oldHeading required for image rotate animation
        float oldHeading = trueHeading;
        if(location != null){

            trueHeading = heading + location.magneticDeclination;

        } else{

            trueHeading = heading;

        }
        if(trueHeading > 360) { //if trueHeading was 362 degrees for example, it should be adjusted to be 2 degrees instead
            trueHeading = trueHeading - 360;
        }

        mainActivity.animateCompass(oldHeading,trueHeading);

    }





}
