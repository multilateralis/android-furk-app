package com.simple.furk.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.simple.furk.APIRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public abstract class FilesAdapter extends BaseAdapter implements APIRequest.APICallback {

    protected final Context context;
    protected JSONArrayChain jArrayChain;


    public FilesAdapter(Context context) {
        super();
        this.context = context;
        jArrayChain = new JSONArrayChain();
    }

    public abstract void Execute(Object... args);

   // public abstract void processAPIResponse(JSONObject jObj) throws JSONException;

    public JSONObject getJSONObject(int index) throws JSONException {
        return jArrayChain.getJSONObject(index);
    }

    @Override
    public int getCount() {
        return jArrayChain.length();
    }



public class JSONArrayChain {
    private ArrayList<JSONArray> innerList;

    public JSONArrayChain() {
        this.innerList = new ArrayList<JSONArray>();
    }
    public JSONArrayChain(JSONArray jArray) {
        this.innerList = new ArrayList<JSONArray>();
        innerList.add(jArray);
    }

    public void addJSONArray(JSONArray jarray)
    {
        innerList.add(jarray);
    }

    public JSONArray getJSONArray(int index)
    {
        return innerList.get(index);
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        int offset = index;
        JSONArray itemContainer = null;
        Iterator<JSONArray> arrayIterator = innerList.iterator();
        while (arrayIterator.hasNext())
        {
            itemContainer = arrayIterator.next();
            if(offset >= itemContainer.length())
                offset -= itemContainer.length();
            else
                break;
        }

        if(itemContainer != null)
        {
            return itemContainer.getJSONObject(offset);
        }
        else
            throw new JSONException("index out of bounds");
    }

    public int length()
    {
        int l = 0;
        for(JSONArray item : innerList)
        {
            l += item.length();
        }
        return l;
    }

    public void clear()
    {
        innerList.clear();
    }
}
}


