package com.simple.furk;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.loopj.android.http.*;


public class APIClient {

    private static final String BASE_URL = "https://www.furk.net/api/";
    private static  String API_KEY = "";
    private final AsyncHttpResponseHandler responseHandler;
    private static AsyncHttpClient client = new AsyncHttpClient();

    public APIClient(APICallback callback)
    {
        this.responseHandler = new APIResponseHandler(callback);

    }

    public static void setAPIKEY(String api_key)
    {
        API_KEY = api_key;
    }


    public void get(String url, RequestParams params) {
        params.add("api_key",API_KEY);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void get(String url) {
        RequestParams params = new RequestParams();
        params.add("api_key",API_KEY);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params) {
        params.add("api_key",API_KEY);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {

        return BASE_URL + relativeUrl;
    }

    public interface APICallback
    {
        public void processAPIResponse(JSONObject response);
        public void processAPIError(Throwable e, JSONObject errorResponse);
    }

    private class APIResponseHandler extends JsonHttpResponseHandler
    {
        private APICallback callback;
        APIResponseHandler(APICallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void onSuccess(JSONObject response) {
          callback.processAPIResponse(response);

        }

        @Override
        public void onFailure(Throwable e, JSONObject errorResponse) {
            Toast.makeText(Furk.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            callback.processAPIError(e,errorResponse);
        }
    }
}
