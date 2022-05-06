package com.example.migroscompass;

import java.util.Comparator;

public class CustomComparator implements Comparator<migros> {
    @Override
    public int compare(migros o, migros t1) {
        return Double.compare(o.getDist(), t1.getDist());
    }
}
