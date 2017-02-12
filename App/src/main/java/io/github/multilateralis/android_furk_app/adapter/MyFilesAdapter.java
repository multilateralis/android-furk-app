package io.github.multilateralis.android_furk_app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.github.multilateralis.android_furk_app.APIClient;
import io.github.multilateralis.android_furk_app.APIUtils;
import io.github.multilateralis.android_furk_app.Furk;
import io.github.multilateralis.android_furk_app.R;

public class MyFilesAdapter extends FilesAdapter {

    private SharedPreferences mPrefs;
    private int loaderPos;
    private Boolean firstLoad;
    private final Furk.MyFilesFragment myFilesFragment;

    public MyFilesAdapter(Furk.MyFilesFragment myFilesFragment) {
        super(myFilesFragment.getActivity());
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

        APIClient.get(myFilesFragment.getActivity(),"file/get",this);
        ((Furk)context).setRefreshing();
    }


    public void saveState()
    {
        if(jArrayChain.length() > 0)
        {
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putString("my_files_cache",jArrayChain.getJSONArray(0).toString());
            ed.commit();
        }
    }

    public void processAPIResponse(JSONObject response){
        String message = "No files";
            try {
                JSONArray jArray = response.getJSONArray("files");
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
            ((Furk) context).doneRefrshing();
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
        APIClient.get(myFilesFragment.getActivity(),"file/get", params,this);
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
            TextView description = (TextView) rowView.findViewById(R.id.listview_description);

            String strTitle = "Unknown";
            String strDescription = "";
            try {
                JSONObject jsonObj = jArrayChain.getJSONObject(position);
                strTitle = Html.fromHtml(jsonObj.getString("name")).toString();
                strDescription = APIUtils.formatSize(jsonObj.getString("size"));
                strDescription += "  " + APIUtils.formatDate(jsonObj.getString("ctime"));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            finally {
                title.setText(strTitle);
                description.setText(strDescription);
            }
        }

        return rowView;
    }


}