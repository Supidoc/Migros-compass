package com.example.migroscompass;

import java.util.List;

public class migros {
    int id;
    String name;
    double lat;
    double lon;
    String type;
    double dist;
    double bear;

    public migros(Integer id, String name, double lat, double lon, String type, double dist, double bear) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.dist = dist;
        this.bear = bear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public double getDist() {
        return dist;
    }
}
