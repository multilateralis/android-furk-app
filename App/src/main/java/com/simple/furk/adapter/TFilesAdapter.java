package com.simple.furk.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.furk.APIClient;
import com.simple.furk.APIUtils;
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

    public TFilesAdapter(FileActivity.TFilesFragment tFilesFragment) {
        super(tFilesFragment.getActivity());
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
        APIClient.get(tFilesFragment.getActivity(),"file/get", params,this);
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
        try {
            tFilesFragment.setEmptyText(e.getMessage());
            tFilesFragment.setListShown(true);
        }
        catch (IllegalStateException e1)
        {
            Log.d("furk","fragment disposed before async api request finished");
        }
        finally {
            jArrayChain.clear();
            notifyDataSetChanged();
        }

    }

    public void saveState(Bundle savedInstanceState)
    {
        if(jArrayChain.length() > 0)
        {
            savedInstanceState.putString("file_tfiles",jArrayChain.getJSONArray(0).toString());
        }
    }

    public boolean loadState(Bundle savedInstanceState)
    {
        if(savedInstanceState != null && savedInstanceState.containsKey("file_tfiles"))
        {
            try {
                JSONArray jArray = new JSONArray(savedInstanceState.getString("file_tfiles"));
                jArrayChain = new JSONArrayChain(jArray);
            } catch (JSONException e) {
                return false;
            }
            tFilesFragment.setEmptyText("");
            tFilesFragment.setListShown(true);
            notifyDataSetChanged();
            return true;
        }
        else
            return false;
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

        String strTitle = "Unknown";
        String strDescription = "";
        try
        {
            JSONObject jsonObj =  jArrayChain.getJSONObject(position);
            strTitle = Html.fromHtml(jsonObj.getString("name")).toString();
            strDescription = APIUtils.formatSize(jsonObj.getString("size"));
            if(jsonObj.has("bitrate"))
                strDescription += "  "+ APIUtils.formatBitRate(jsonObj.getString("bitrate"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        finally {
            title.setText(strTitle);
            description.setText(strDescription);
        }


            // imageView.setImageResource(R.drawable.ic_launcher);


        return rowView;
    }

}
