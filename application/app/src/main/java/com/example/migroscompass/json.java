package com.example.migroscompass;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class json {

    static String getJsonFromAssets(Context context) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open("migros_data_conv.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }


}
