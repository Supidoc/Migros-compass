package com.example.migroscompass;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class compass {

    static List<migros> migrosArrayList = new ArrayList<>();
    public static boolean loadedMigis;

    public static void createMigrosArrayList(LatLng cur_loc){
        for (int i = 0; i < migrosArrayList.size(); i++) {
            migros cur_migros = migrosArrayList.get(i);
            LatLng MigLoc = new LatLng(cur_migros.lat, cur_migros.lon);
            cur_migros.dist = computeDistanceBetween(cur_loc, MigLoc);
            cur_migros.bear = computeHeading(cur_loc, MigLoc);
            migrosArrayList.set(i, cur_migros);

        }


    }

}
