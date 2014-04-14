package com.simple.furk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.furk.APIClient;
import com.simple.furk.Furk;
import com.simple.furk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MyFilesAdapter extends FilesAdapter {

    private SharedPreferences mPrefs;
    private int loaderPos;
    private Boolean firstLoad;
    private final Furk.MyFilesFragment myFilesFragment;

    public MyFilesAdapter(Context context, Furk.MyFilesFragment myFilesFragment) {
        super(context);
        this.myFilesFragment = myFilesFragment;
        mPrefs = context.getSharedPreferences("furk_cache",0);
        loaderPos = 0;
        firstLoad = true;
    }

    @Override
    public void Execute(Object... args)
    {
        if(mPrefs.contains("my_files_cache"))
            try {
                jArrayChain = new JSONArrayChain(new JSONArray(mPrefs.getString("my_files_cache",null)));
            } catch (JSONException e) {
                jArrayChain.clear();
            }
        firstLoad = true;

        APIClient.get("file/get",this);
        ((Furk)context).setRefreshing();
    }


    public void saveMyFiles()
    {
        if(jArrayChain.length() > 0)
        {
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putString("my_files_cache",jArrayChain.getJSONArray(0).toString());
            ed.commit();
        }
    }

    public void processAPIResponse(JSONObject response){
        JSONArray jArray = null;
        String message = "No files";
            try {
                jArray = response.getJSONArray("files");
                if (firstLoad) {
                    jArrayChain.clear();
                    firstLoad = false;
                }
                jArrayChain.addJSONArray(jArray);
            }
            catch (Exception e) {
                    jArrayChain.clear();
                    message =  "Invalid server response. "+ e.getMessage();
            }
            finally {
                myFilesFragment.setEmptyText(message);
                notifyDataSetChanged();
                ((Furk) context).doneRefrshing();
            }
        }

    @Override
    public void processAPIError(Throwable e) {

        try
        {
            myFilesFragment.setEmptyText(e.getMessage());
            jArrayChain.clear();
            notifyDataSetChanged();
            ((Furk) context).doneRefrshing();
        }
        catch (IllegalStateException e1)
        {
            Log.d("furk","fragment disposed before async api request finished");
        }
    }


    @Override
    public int getCount() {
        int length = jArrayChain.length();
        if(length > 0)
           //Add an extra listview item for the loading spinner
           return length + 1;
        else
            return 0;
    }

    @Override
    public String getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    private void getMoreFiles()
    {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("offset", String.valueOf(jArrayChain.length()));
        APIClient.get("file/get", params,this);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView;
        if (position == jArrayChain.length())
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_loading, parent, false);

            //Avoid calling getMoreFiles more than once
            if(loaderPos != position)
            {
                loaderPos = position;
                getMoreFiles();
            }
        }
        else
        {
            //Recycle convertView. Check for loader list item because it can't be recycled
            if(convertView == null || convertView.getId() == R.id.listViewLoader)
            {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_file, parent, false);
            }
            else
            {
                rowView = convertView;
            }

            TextView title = (TextView) rowView.findViewById(R.id.listview_title);
            //TextView icon = (TextView) rowView.findViewById(R.id.icon);
            TextView description = (TextView) rowView.findViewById(R.id.listview_description);
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            try
            {
                JSONObject jsonObj =  jArrayChain.getJSONObject(position);
                title.setText(Html.fromHtml(jsonObj.getString("name")).toString());
                long size = Long.parseLong(jsonObj.getString("size"));
                String sizePref = "B";
                if(size >= 1073741824)
                {
                    size = size/1073741824;
                    sizePref = "GB";
                }
                else if(size >= 1048576)
                {
                    size = size/1048576;
                    sizePref = "MB";
                }
                else if(size >= 1024)
                {
                    size = size/1024;
                    sizePref = "KB";
                }

                //String typeLetter = jsonObj.getString("type").toUpperCase().substring(0,1);
                description.setText("Size: " + size +" "+ sizePref);
                //icon.setText(typeLetter);
//                imageLoader.displayImage(jsonObj.getJSONArray("ss_urls_tn ").getString(0), imageView);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return rowView;
    }

}