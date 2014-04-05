package com.simple.furk.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.furk.APIRequest;
import com.simple.furk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchFilesAdapter extends FilesAdapter {

    private int loaderPos;
    private String query;
//    private DisplayImageOptions options;
//    private ImageLoader imageLoader;


    public SearchFilesAdapter(Context context) {
        super(context);

        loaderPos = 0;
    }

    public void Execute(Object... args)
    {
        jArrayChain.clear();
        query = (String)args[0];
        APIRequest apiRequest = new APIRequest(this);
        apiRequest.execute("plugins/metasearch","q="+query);
    }


    public void processAPIResponse(JSONObject jObj){
        JSONArray jArray = null;
        try {
            jArray = jObj.getJSONArray("files");
            jArrayChain.addJSONArray(jArray);
            notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(context, "Invalid server response", Toast.LENGTH_LONG);
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
        APIRequest apiRequest;
        apiRequest = new APIRequest(this);
        apiRequest.execute("plugins/metasearch","q="+query,"offset="+jArrayChain.length());
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
                rowView.setBackgroundColor(0xFFEBEBEB);
            }

            TextView title = (TextView) rowView.findViewById(R.id.listview_title);
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

                description.setText("Size: " + size +" "+ sizePref);
                String is_ready = jsonObj.getString("is_ready");
                if(is_ready.equals("0"))
                    rowView.setBackgroundColor(0xFFB9B9B9);
//                imageLoader.displayImage(jsonObj.getJSONArray("ss_urls_tn ").getString(0), imageView);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }


            // imageView.setImageResource(R.drawable.ic_launcher);
        }

        return rowView;
    }

}