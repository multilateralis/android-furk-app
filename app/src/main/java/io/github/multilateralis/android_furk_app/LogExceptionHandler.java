package io.github.multilateralis.android_furk_app;

import android.content.Context;
import android.content.SharedPreferences;

public class LogExceptionHandler implements Thread.UncaughtExceptionHandler {

    private SharedPreferences mPrefs;

    public LogExceptionHandler(Context context)
    {
        mPrefs = context.getSharedPreferences("furk_exceptions",0);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        SharedPreferences.Editor ed = mPrefs.edit();
        String stackTrace = "";
        for(StackTraceElement e : throwable.getStackTrace())
            stackTrace += e.toString()+"\n";
        ed.putString(throwable.hashCode()+"",throwable.toString()+ "\n\n"+ stackTrace);
        ed.commit();

            throw new RuntimeException(throwable.getMessage());

    }
}
