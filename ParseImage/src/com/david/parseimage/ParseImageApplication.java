package com.david.parseimage;

import com.facebook.drawee.backends.pipeline.Fresco;

import android.app.Application;
import android.util.Log;

public class ParseImageApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d("david","david initialize fresco");
        Fresco.initialize(getApplicationContext());
    }

}
