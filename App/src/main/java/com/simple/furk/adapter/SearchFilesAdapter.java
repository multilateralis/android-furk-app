package com.simple.furk.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simple.furk.APIClient;
import com.simple.furk.R;
import com.simple.furk.SearchActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SearchFilesAdapter extends FilesAdapter {

    private int loaderPos;
    private String searchQuery;
    private String sortQuery;
    private final SearchActivity.SearchFragment searchFragment;
//    private DisplayImageOptions options;
//    private ImageLoader imageLoader;


    public SearchFilesAdapter(Context context,SearchActivity.SearchFragment searchFragment) {
        super(context);
        this.loaderPos = 0;
        this.searchFragment = searchFragment;
        this.searchQuery = "";
        this.sortQuery = "cached";
    }

    public void Execute(Object... args)
    {
        searchFragment.setListShown(false);
        jArrayChain.clear();
        searchQuery = (String)args[0];
        sortQuery = (String)args[1];
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("q", searchQuery);
        params.put("sort",sortQuery);
        APIClient.get("plugins/metasearch", params,this);
    }


    public void processAPIResponse(JSONObject response){
        JSONArray jArray = null;
        searchFragment.setEmptyText("");
        String message = "Error";
        try {
            String totalFound = response.getJSONObject("stats").getString("total_found");
            if(Integer.parseInt(totalFound) > 0) {
                jArray = response.getJSONArray("files");
                jArrayChain.addJSONArray(jArray);
            }
            else{
                message = "No Matches Found";
            }
        } catch (Exception e) {
            message = "Invalid server response. " + e.getMessage();
        }
        finally {
            searchFragment.setEmptyText(message);
            searchFragment.setListShown(true);
            notifyDataSetChanged();
        }
    }

    @Override
    public void processAPIError(Throwable e) {

        try
        {
        searchFragment.setEmptyText(e.getMessage());
        searchFragment.setListShown(true);
        jArrayChain.clear();
        notifyDataSetChanged();
        }
        catch (IllegalStateException e1)
        {

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
        params.put("q", searchQuery);
        params.put("sort",sortQuery);
        params.put("offset", String.valueOf(jArrayChain.length()));
        APIClient.get("plugins/metasearch", params,this);
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