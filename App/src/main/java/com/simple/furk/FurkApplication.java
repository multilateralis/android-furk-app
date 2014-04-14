package com.simple.furk;

import android.app.Application;
import android.content.Context;

/**
 * Created by Nicolas on 4/13/2014.
 */
public class FurkApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        FurkApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return FurkApplication.context;
    }
}
