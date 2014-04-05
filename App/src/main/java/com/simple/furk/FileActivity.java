package com.simple.furk;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.simple.furk.adapter.TFilesAdapter;

import org.json.JSONException;
import org.json.JSONObject;


public class FileActivity extends ActionBarActivity {


    public static JSONObject FILE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getProperty("name"));
        }
        if(FILE == null)
            Log.d("Furk","File is null");

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new TFilesFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_file, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_search:
//                onSearchRequested();
//                return true;
//            case android.R.id.home:
//                Intent intent = new Intent(this, Furk.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                return true;
//            default:
//                return false;
//        }
        if(item.getItemId() == android.R.id.home)
        {
            Intent intent = new Intent(this, Furk.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.action_download)
        {
            downloadFile();
            return true;
        }
        else if(item.getItemId() == R.id.context_open_browser)
        {
            openFileinBrowser();
            return true;
        }
        else if(item.getItemId() == R.id.context_search_google)
        {
            searchFileInGoogle();
            return true;
        }
        else if(item.getItemId() == R.id.context_copy_name)
        {
            copyFilePropertyToClipboard("name");
            return true;
        }
        else if(item.getItemId() == R.id.context_copy_link)
        {
            copyFilePropertyToClipboard("url_dl");
            return true;
        }
        else
        {
            return false;
        }
    }

    private String getProperty(String property)
    {
        try {
           return FILE.getString(property);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Can't find property "+property,Toast.LENGTH_LONG);
            return null;
        }
    }

    public void downloadFile()
    {
        String url = getProperty("url_dl");
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
    }

    public void openFileinBrowser()
    {
        String pageUrl = getProperty("url_page");
        String url = "https://www.furk.net" + pageUrl;
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
    }

    public void searchFileInGoogle()
    {
        String name = getProperty("name");
        String url = "http://www.google.com/#q="+name;
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
    }

    private void copyFilePropertyToClipboard(String property)
    {
        String text = getProperty(property);
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public static class TFilesFragment extends ListFragment
    {
        protected TFilesAdapter adapter;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new TFilesAdapter(getActivity());
            setListAdapter(adapter);
            try {
                adapter.Execute(FILE.getString("id"));
                registerForContextMenu(getListView());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.context_my_files, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId())
            {
                case R.id.menu_copy_name:
                    copyToClipboard(getPropertyFromItem("name",info.position));
                    return true;
                case R.id.menu_copy_link:
                    copyToClipboard(getPropertyFromItem("url_dl",info.position));
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }

        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            String strURL = getPropertyFromItem("url_dl",position);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strURL));
            startActivity(myIntent);
        }

        private String getPropertyFromItem(String property, int position)
        {
            try {
                JSONObject jObj = adapter.getJSONObject(position);
                return jObj.getString(property);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        private void copyToClipboard(String text)
        {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);
            }
        }

    }


}
