package com.simple.furk;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class APIRequest extends AsyncTask<String, String, JSONObject> {

    private static final String API_BASE = "https://www.furk.net/api/";
    private static  String API_KEY = "";
    private final APICallback callback;

    public APIRequest(APICallback callback)
    {
        this.callback = callback;
    }

    public static void setAPIKEY(String api_key)
    {
        API_KEY = api_key;
    }
    @Override
    protected JSONObject doInBackground(String... strings) {
            return makeAPIRequest(strings);
    }

    private JSONObject makeAPIRequest(String... strings)
    {
        JSONObject jsonObj = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(API_BASE);
            sb.append(strings[0]);
            sb.append("?api_key=" + API_KEY);
            for (int i = 1; i< strings.length;i++)
                sb.append("&"+strings[i]);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        }
        catch (MalformedURLException e)
        {
            publishProgress("Error processing request");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            publishProgress("Error connecting to Furk.net");
            e.printStackTrace();
        }
        finally
        {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            jsonObj = new JSONObject(jsonResults.toString());
            return jsonObj;
        }
        catch (JSONException e)
        {
            publishProgress("Cannot process results");
            e.printStackTrace();
        }

        return jsonObj;
    }

    private boolean checkApiKey(){

        return true;
    }
    @Override
    protected void onPostExecute(JSONObject result)
    {
        callback.processAPIResponse(result);
    }

    @Override
    protected void onProgressUpdate(String... progress)
    {
        Toast.makeText(Furk.getContext(),progress[0],Toast.LENGTH_LONG).show();
    }

    public interface APICallback
    {
        public void processAPIResponse(JSONObject result);
    }
}
