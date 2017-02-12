package io.github.multilateralis.android_furk_app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import io.github.multilateralis.android_furk_app.adapter.TFilesAdapter;


public class FileActivity extends AppCompatActivity {


    private JSONObject file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        String fileString = getIntent().getStringExtra("file");
        String id = null;
        try {
            file = new JSONObject(fileString);
            id = file.getString("id");
        } catch (JSONException e) {
            Toast.makeText(this,"Can't open file",Toast.LENGTH_LONG).show();
            finish();
        }

        if(savedInstanceState == null) {

            TFilesFragment fragment = new TFilesFragment();
            fragment.setID(id);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        restoreActionBar();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file, menu);

        return true;
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        try {
            actionBar.setTitle(file.getString("name"));
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property: \"name\"",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        if(item.getItemId() == R.id.action_download)
        {
            downloadFile();
            return true;
        }
        else if(item.getItemId() == R.id.action_share)
        {
            shareFile();
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


    public void shareFile()
    {
        try {
            String name = file.getString("name");
            String url = file.getString("url_dl");
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, name);
            intent.putExtra(android.content.Intent.EXTRA_TEXT, url);
            startActivity(Intent.createChooser(intent,"Share via"));
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property in file",Toast.LENGTH_LONG).show();
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,"No activity can handle share",Toast.LENGTH_LONG).show();
        }

    }

    public void downloadFile()
    {
        try {
            String url = file.getString("url_dl");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(myIntent);
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property: url_dl",Toast.LENGTH_LONG).show();
        }
    }

    public void openFileinBrowser()
    {
        try {
        String pageUrl = file.getString("url_page");
        String url = "https://www.furk.net" + pageUrl;
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property: url_dl",Toast.LENGTH_LONG).show();
        }
    }

    public void searchFileInGoogle()
    {
        try {
        String name = file.getString("name");
        String url = "http://www.google.com/#q="+name;
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property: \"name\"",Toast.LENGTH_LONG).show();
        }
    }

    private void copyFilePropertyToClipboard(String property)
    {
        try {
        String text = file.getString(property);
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        } catch (JSONException e) {
            Toast.makeText(this,"Can't find property: "+ property,Toast.LENGTH_LONG).show();
        }
    }

    public static class TFilesFragment extends ListFragment
    {
        protected TFilesAdapter adapter;
        private String id;

        //public TFilesFragment(String id)
        //{
          //  this.id = id;
        //}

        public TFilesFragment()
        {

        }

        void setID(String id) {
            this.id = id;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new TFilesAdapter(TFilesFragment.this);
            setListAdapter(adapter);
            registerForContextMenu(getListView());

            if(!adapter.loadState(savedInstanceState)) {
                adapter.Execute(id);
            }
            else
            {
                id = savedInstanceState.getString("file_id");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState){
            super.onSaveInstanceState(outState);
            adapter.saveState(outState);
            outState.putString("file_id",id);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.context_files, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId())
            {
                case R.id.context_share:
                    shareFile(info.position);
                    return true;
                case R.id.context_copy_name:
                    copyToClipboard(getPropertyFromItem("name", info.position));
                    return true;
                case R.id.context_link_address:
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

        public void shareFile(int position)
        {
            try {
                JSONObject jObj = adapter.getJSONObject(position);
                String name = jObj.getString("name");
                String url = jObj.getString("url_dl");
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, name);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(intent,"Share via"));
            } catch (JSONException e) {
                Toast.makeText(getActivity(),"Can't find property in file",Toast.LENGTH_LONG).show();
            }
            catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(),"No activity can handle share",Toast.LENGTH_LONG).show();
            }

        }

        private String getPropertyFromItem(String property, int position)
        {
            try {
                JSONObject jObj = adapter.getJSONObject(position);
                return jObj.getString(property);
            } catch (JSONException e) {
                Toast.makeText(getActivity(),"Can't find property:"+ property,Toast.LENGTH_LONG).show();
                return "";
            }
        }
        private void copyToClipboard(String text)
        {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }

    }


}

