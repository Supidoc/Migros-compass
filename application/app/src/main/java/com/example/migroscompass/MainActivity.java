package com.example.migroscompass;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity{

    int listLength=6;

    TextView Status;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Thread t;
    boolean loadedMigis = false;
    static DecimalFormat df = new DecimalFormat("#.#");

    ImageView imageViewCompass;
    boolean proc = false;
    migros selected_migi;

    int selected_i = 0;

    ImageView[] m_icons = new ImageView[listLength];
    ConstraintLayout[] m_icons_container = new ConstraintLayout[listLength];
    boolean inflates = false;
    location location;
    sensor sensor;


    public MainActivity() {
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = new location(this,MainActivity.this, this);
        location.newManager(this);

        sensor = new sensor(this,MainActivity.this, this);
        sensor.newManager();


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

            String jsonFileString = json.getJsonFromAssets(getApplicationContext());

            ObjectMapper mapper = new ObjectMapper();
            try {


                migros.arrayList = Arrays.asList(mapper.readValue(jsonFileString, migros[].class));
                migros.loadedMigis = true;
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
        //location = new location(this,this, this );

        TextView nearest_migi_text = findViewById(R.id.Selected_Migi);
        nearest_migi_text.setText(R.string.loadingLocation);

        location.startUpdates(this);

        sensor.register();


    }



    @Override
    protected void onStop() {
        super.onStop();
        location.removeUpdates();
        sensor.unregister();

    }

    protected void onPause() {
        super.onPause();
        location.removeUpdates();
        sensor.unregister();

    }



    public void parseJson() {

            t.start();
            t.setName("migros");
            t.setPriority(Thread.MAX_PRIORITY);
    }


    public void nearest_migi(LatLng loc, Activity activity) {
        if (loc != null && migros.loadedMigis) {
            migros.createMigrosArrayList(loc);
            Collections.sort(migros.arrayList, new CustomComparator());
            selected_migi = migros.arrayList.get(selected_i);
            runOnUiThread(() -> {

                TextView selected_migi_text = activity.findViewById(R.id.Selected_Migi);
                TextView selected_migi_dist_text = activity.findViewById(R.id.Selected_Migi_dist);
                selected_migi_text.setText(selected_migi.name);
                selected_migi_dist_text.setText(df.format(selected_migi.dist / 1000) + " Km");
                if(!inflates){
                    inflates = true;
                    ConstraintLayout targetContainer = activity.findViewById(R.id.compass);



                    for (int i = 0; i < listLength; i++) {
                        LayoutInflater.from(activity).inflate(R.layout.imageview_m_icon,targetContainer,true);
                        ImageView img = activity.findViewById(R.id.compass_m_icon_inf);
                        ConstraintLayout container = activity.findViewById(R.id.compass_m_icon_container_inf);
                        img.setId(1000+i);
                        container.setId(2000+i);

                        m_icons[i] = img;
                        m_icons_container[i] = container;

                        activity.findViewById(R.id.compass_m_icon_inf_).setAlpha(0);


                        int finalI = i;
                        img.setOnClickListener(v -> {
                            switch_migros(finalI);
                            Log.i("asdf", "Test Switch");
                        });

                    }

                }
            });


        }


    }

    public void switch_migros(int id)
    {
        selected_i = id;
        nearest_migi(location.loc,this );
    }

    public void animateCompass( float trueHeading){

        imageViewCompass = findViewById(R.id.compass_needle);
        View Compass = findViewById(R.id.compass);

        if(Compass != null) {

            Compass.setRotation(-trueHeading);

        }



        if(inflates){

            for (int i = 0;  i < m_icons.length; i++) {

                float Cur_bear = (float) migros.arrayList.get(i).bear;

                if(migros.arrayList.get(i)==selected_migi){

                    m_icons[i].setScaleX((float) (0.80));
                    m_icons[i].setScaleY((float) (0.80));
                    m_icons[i].setAlpha((float) (1));

                }else {

                    double distFactor = 0.75;
                    double alphaFactor = 0.8;
                    if(migros.arrayList.get(i).dist<800){

                        m_icons[i].setScaleX((float) (1*distFactor));
                        m_icons[i].setScaleY((float) (1*distFactor));
                        m_icons[i].setAlpha((float) (1*alphaFactor));

                    } else if(migros.arrayList.get(i).dist<3330){

                        m_icons[i].setScaleX((float) (0.8*distFactor));
                        m_icons[i].setScaleY((float) (0.8*distFactor));
                        m_icons[i].setAlpha((float) (0.8*alphaFactor));

                    } else if(migros.arrayList.get(i).dist<8330){

                        m_icons[i].setScaleX((float) (0.6*distFactor));
                        m_icons[i].setScaleY((float) (0.6*distFactor));
                        m_icons[i].setAlpha((float) (0.6*alphaFactor));

                    } else{

                        m_icons[i].setScaleX((float) (0.4*distFactor));
                        m_icons[i].setScaleY((float) (0.4*distFactor));
                        m_icons[i].setAlpha((float) (0.4*alphaFactor));


                    }

                }


                m_icons_container[i].setRotation(Cur_bear);
                m_icons_container[i].setPivotX(findViewById(R.id.compass).getPivotX()-findViewById(R.id.compass).getWidth()/2+m_icons_container[i].getWidth()/2);
                m_icons_container[i].setPivotY(findViewById(R.id.compass).getPivotY());
                m_icons[i].setPivotX(m_icons[i].getWidth()/2);
                m_icons[i].setPivotY(m_icons[i].getHeight());

            }
        }

    }



}




