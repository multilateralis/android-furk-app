package com.simple.furk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by Nicolas on 12/7/13.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, APIRequest.APICallback {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Furk.getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("api_key")) {
            APIRequest req = new APIRequest(this);
            APIRequest.setAPIKEY(sharedPreferences.getString(s, ""));
            req.execute("ping");
        }
    }

    @Override
    public void processAPIResponse(JSONObject result) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Furk.getContext());
        String status = "failed";
        try {
             status = result.getString("status");
        }
        catch (Exception e)
        {
        }

        if("ok".equals(status))
            Toast.makeText(Furk.getContext(),"Connection to furk.net successful",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(Furk.getContext(),"Connection to furk.net unsuccessful. Probably because an invalid api key",Toast.LENGTH_LONG).show();
    }
}
