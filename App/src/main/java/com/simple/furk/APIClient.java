package com.simple.furk;



import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.*;


public class APIClient {

    private static final String BASE_URL = "https://www.furk.net/api/";
    private static  String API_KEY = "";
    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void setAPIKEY(String api_key)
    {
        API_KEY = api_key;
    }

    public static void get(String url, RequestParams params,APICallback callback) {
        APIResponseHandler responseHandler = new APIResponseHandler(callback);
        params.add("api_key",API_KEY);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void get(String url,APICallback callback) {
        APIResponseHandler responseHandler = new APIResponseHandler(callback);
        RequestParams params = new RequestParams();
        params.add("api_key",API_KEY);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params,APICallback callback) {
        APIResponseHandler responseHandler = new APIResponseHandler(callback);
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


    private static class APIResponseHandler extends  AsyncHttpResponseHandler
    {
        private APICallback callback;
        APIResponseHandler(APICallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void onSuccess(String response) {
            try {
                JSONObject jobj = new JSONObject(response);
                callback.processAPIResponse(jobj);
            } catch (JSONException e) {
                Toast.makeText(Furk.getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

        }


        @Override
        public void onFailure(Throwable error){
            Toast.makeText(Furk.getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            callback.processAPIError(error,new JSONObject());
        }

    }
}
