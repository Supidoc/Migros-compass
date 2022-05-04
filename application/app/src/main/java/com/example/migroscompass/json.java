package com.example.migroscompass;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.jsfr.json.GsonParser;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.compiler.JsonPathCompiler;
import org.jsfr.json.provider.GsonProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class json {

    public  static JsonSurfer getSurfer(){

        return new JsonSurfer(GsonParser.INSTANCE, GsonProvider.INSTANCE);

    }

    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long getCount(String json, JsonSurfer surfer, String key) {
        Iterator iterator = surfer.iterator(json, JsonPathCompiler.compile(key));
        Stream stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
        return stream.count();
    }




}
