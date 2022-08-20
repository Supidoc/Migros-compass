package com.example.migroscompass;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "lat",
        "lon",
        "type"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class migros implements Serializable
{

    @JsonProperty("name")
    String name;
    @JsonProperty("lat")
    Double lat;
    @JsonProperty("lon")
    Double lon;
    @JsonProperty("type")
    String type;
    double dist;
    double bear;
    private final static long serialVersionUID = -5679260257182767596L;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("lat")
    public Double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(Double lat) {
        this.lat = lat;
    }

    @JsonProperty("lon")
    public Double getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public void setLon(Double lon) {
        this.lon = lon;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public double getDist() {
        return dist;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    static List<migros> arrayList = new ArrayList<>();
    public static boolean loadedMigis;

    public static void createMigrosArrayList(LatLng cur_loc){
        for (int i = 0; i < arrayList.size(); i++) {
            migros cur_migros = arrayList.get(i);
            LatLng MigLoc = new LatLng(cur_migros.lat, cur_migros.lon);
            cur_migros.dist = computeDistanceBetween(cur_loc, MigLoc);
            cur_migros.bear = computeHeading(cur_loc, MigLoc);
            arrayList.set(i, cur_migros);

        }


    }

}