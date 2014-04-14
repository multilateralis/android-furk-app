package com.simple.furk;



import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;


public class APIClient {

    private static final String BASE_URL = "https://www.furk.net/api/";
    private static  String API_KEY = "";
    //private static AsyncHttpClient client = new AsyncHttpClient();


    public static void get(String url, HashMap<String,String> params, APICallback callback) {
        checkAPIKey();
        APIResponseHandler handler = new APIResponseHandler(callback);
        Ion.with(FurkApplication.getAppContext(),getAbsoluteUrl(url,params))
                .asString()
                .setCallback(handler);
    }

    public static void get(String url,APICallback callback) {
        checkAPIKey();
        APIResponseHandler handler = new APIResponseHandler(callback);
        Ion.with(FurkApplication.getAppContext(),getAbsoluteUrl(url))
                .asString()
                .setCallback(handler);
    }

    public void post(String url, HashMap<String,String> params, APICallback callback) {
        checkAPIKey();
    }

    private static void checkAPIKey()
    {
        if(API_KEY.isEmpty()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FurkApplication.getAppContext());
            String apiKey = preferences.getString("api_key", "");
            API_KEY = apiKey;
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {

        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append(relativeUrl);
        sb.append("?api_key=" + API_KEY);

        return sb.toString();
    }
    private static String getAbsoluteUrl(String relativeUrl,HashMap<String,String> params)  {

        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append(relativeUrl);
        sb.append("?api_key=" + API_KEY);
        Iterator<String> itr = params.keySet().iterator();
        while (itr.hasNext())
        {
            String key = itr.next();
            try {
                sb.append("&" + key + "=" + URLEncoder.encode(params.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public interface APICallback
    {
        public void processAPIResponse(JSONObject response);
        public void processAPIError(Throwable e);
    }


    private  static  class APIResponseHandler implements FutureCallback<String> {
        private APICallback callback;
        APIResponseHandler(APICallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void onCompleted(Exception e, String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                if(jObj.has("error") && jObj.getString("error").equals("access denied"))
                    callback.processAPIError(new Throwable("Access denied. Please logout and login again"));
                else
                    callback.processAPIResponse(jObj);

            } catch (Exception e1) {
                if(e != null)
                    callback.processAPIError(e);
                else
                    callback.processAPIError(e1);
            }
        }
    }

}
