package com.simple.furk.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.furk.APIClient;
import com.simple.furk.FileActivity;
import com.simple.furk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Nicolas on 12/10/13.
 */
public class TFilesAdapter extends FilesAdapter {


    private FileActivity.TFilesFragment tFilesFragment;

    public TFilesAdapter(Context context, FileActivity.TFilesFragment tFilesFragment) {
        super(context);
        this.tFilesFragment = tFilesFragment;
    }

    public void Execute(Object... args)
    {
        tFilesFragment.setEmptyText("");
        tFilesFragment.setListShown(false);
        jArrayChain.clear();
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("id", (String) args[0]);
        params.put("t_files", "1");
        APIClient.get("file/get", params,this);
    }


    public void processAPIResponse(JSONObject response) {

        try {
            JSONArray jArray = response.getJSONArray("files").getJSONObject(0).getJSONArray("t_files");
            jArrayChain.addJSONArray(jArray);
        } catch (Exception e) {
            Toast.makeText(context, "Invalid server response",Toast.LENGTH_LONG).show();
        }
        finally {
            tFilesFragment.setListShown(true);
            notifyDataSetChanged();
        }

    }

    @Override
    public void processAPIError(Throwable e) {
        jArrayChain.clear();
        tFilesFragment.setEmptyText(e.getMessage());
        tFilesFragment.setListShown(true);
        notifyDataSetChanged();
    }


    public String getFileURL(int index)
    {
        try {
            return jArrayChain.getJSONObject(index).getString("url_dl");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCount() {
        return jArrayChain.length();
    }

    @Override
    public String getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView;

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
        TextView description = (TextView) rowView.findViewById(R.id.listview_description);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
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
//                imageLoader.displayImage(jsonObj.getJSONArray("ss_urls_tn ").getString(0), imageView);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


            // imageView.setImageResource(R.drawable.ic_launcher);


        return rowView;
    }

}
