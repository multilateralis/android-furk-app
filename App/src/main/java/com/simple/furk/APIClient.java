package com.simple.furk;



import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;



public class APIClient {

    private static final String BASE_URL = "https://www.furk.net/api/";
    private static  String API_KEY = "";
    //private static AsyncHttpClient client = new AsyncHttpClient();


    public static void get(Context context,String url, HashMap<String,String> params, APICallback callback) {
        checkAPIKey(context);
        APIResponseHandler handler = new APIResponseHandler(callback);
        Ion.with(context,getAbsoluteUrl(url,params))
                .asString()
                .setCallback(handler);
    }

    public static void get(Context context, String url,APICallback callback) {
        checkAPIKey(context);
        APIResponseHandler handler = new APIResponseHandler(callback);
        Ion.with(context,getAbsoluteUrl(url))
                .asString()
                .setCallback(handler);
    }

    public void post(Context context, String url, HashMap<String,String> params, APICallback callback) {
        checkAPIKey(context);
    }

    private static void checkAPIKey(Context context)
    {
        if(API_KEY.isEmpty()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            API_KEY = preferences.getString("api_key", "");
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {

        return BASE_URL + relativeUrl + "?api_key=" + API_KEY;
    }
    private static String getAbsoluteUrl(String relativeUrl,HashMap<String,String> params)  {

        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append(relativeUrl);
        sb.append("?api_key=").append(API_KEY);
        for (String key : params.keySet()) {
            try {
                sb.append("&").append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
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
