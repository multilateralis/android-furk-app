package io.github.multilateralis.android_furk_app.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.multilateralis.android_furk_app.APIClient;
import io.github.multilateralis.android_furk_app.Furk;
import io.github.multilateralis.android_furk_app.R;

public class ActiveFilesAdapter extends FilesAdapter {

    private JSONArray active;
    private final Furk.ActiveFilesFragment activeFilesFragment;
    public ActiveFilesAdapter(Furk.ActiveFilesFragment activeFilesFragment)
    {
        super(activeFilesFragment.getActivity());
        this.activeFilesFragment = activeFilesFragment;
        active = new JSONArray();
    }

    @Override
    public void Execute(Object... args) {
        activeFilesFragment.setEmptyText("Loading..");
        jArrayChain.clear();
        APIClient.get(activeFilesFragment.getActivity(),"dl/get",this);
        ((Furk)context).setRefreshing();
    }

    public JSONObject getJSONObject(int index) throws JSONException {

        return jArrayChain.getJSONObject(index);
    }

    @Override
    public void processAPIResponse(JSONObject response){
        JSONArray jArray = null;
        active = new JSONArray();
        JSONArray failed = new JSONArray();
        Boolean startedFailed = false;
        String message = "No downloads";
        try {
            jArray = response.getJSONArray("torrents");
            for(int i = 0; i < jArray.length();i++)
            {
                JSONObject iObj = jArray.getJSONObject(i);
                String status = iObj.getString("dl_status");
                if(status.equals("active"))
                {
                    active.put(jArray.get(i));
                }
                else
                {
                    String name = iObj.getString("name");
                    if(name != "null")
                        failed.put(jArray.get(i));
                }
                jArrayChain.addJSONArray(active);
                jArrayChain.addJSONArray(failed);
            }
        } catch (Exception e) {
                jArrayChain.clear();
                message = "Invalid server response. "+ e.getMessage();
        }
        finally {
            activeFilesFragment.setEmptyText(message);
            notifyDataSetChanged();
            ((Furk) context).doneRefrshing();
        }

    }

    @Override
    public void processAPIError(Throwable e) {
        try {
            activeFilesFragment.setEmptyText(e.getMessage());
            ((Furk) context).doneRefrshing();
        }
        catch (IllegalStateException e1)
        {
            Log.d("furk", "fragment disposed before async api request finished");
        }
        finally {
            jArrayChain.clear();
            notifyDataSetChanged();
        }
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
    public int getCount() {
        return jArrayChain.length();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        View rowView;

        Boolean failedFile = i > active.length() - 1;
        //Recycle convertView. Check for loader list item because it can't be recycled
        if(!failedFile && (view == null || view.getId() != R.id.listItemViewActive))
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_file_active, viewGroup, false);
        }
        else if(failedFile && (view == null || view.getId() != R.id.listItemViewFailed))
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_file_failed, viewGroup, false);
        }
        else
        {
            rowView = view;
        }

        TextView title = (TextView) rowView.findViewById(R.id.listview_title);
        //TextView speed = (TextView) rowView.findViewById(R.id.listview_active_speed);

        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        try
        {
            JSONObject jsonObj =  jArrayChain.getJSONObject(i);
            title.setText(Html.fromHtml(jsonObj.getString("name")).toString());

            if(!failedFile)
            {
                ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
                TextView percent = (TextView) rowView.findViewById(R.id.listview_active_percent);
                float have =  Float.parseFloat(jsonObj.getString("have"));
                progressBar.setProgress((int)have);
                percent.setText(Integer.toString((int)have)+"%");
            }
            /*long size = Long.parseLong(jsonObj.getString("size"));
            String sizePref = "b/s";
            if(size >= 1073741824)
            {
                size = size/1073741824;
                sizePref = "gb/s";
            }
            else if(size >= 1048576)
            {
                size = size/1048576;
                sizePref = "mb/s";
            }
            else if(size >= 1024)
            {
                size = size/1024;
                sizePref = "kb/s";
            }*/
            //speed.setText(size+sizePref);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return rowView;
    }
}
