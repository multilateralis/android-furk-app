package io.github.multilateralis.android_furk_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import io.github.multilateralis.android_furk_app.adapter.ActiveFilesAdapter;
import io.github.multilateralis.android_furk_app.adapter.FilesAdapter;
import io.github.multilateralis.android_furk_app.adapter.MyFilesAdapter;

public class Furk extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,OnQueryTextListener,APIClient.APICallback {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private boolean refreshing;
    private boolean collapseSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshing = false;
        collapseSearch = false;

        setContentView(R.layout.activity_furk);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        handleIntent();
    }

    private void handleIntent() {

        if (getIntent().getAction().equals("io.github.multilateralis.android_furk_app.TORRENT_SEARCH")) {
            torrentSearch();
        }
        else if (getIntent().getScheme() != null) {
            addTorrent(getIntent().getScheme());
        }
    }

    private void addTorrent(String scheme) {
        String url = getIntent().getDataString();
        if(scheme.contains("magnet")) {
            try {
                url = java.net.URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("url", url);
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Adding torrent");
        dialog.setIndeterminate(true);
        dialog.show();
        APIClient.get(this, "dl/add", params, this, dialog);
    }

    private void torrentSearch() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Searching episode");
        dialog.setIndeterminate(true);
        dialog.show();

        String url = getIntent().getDataString();
        Ion.with(this, url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {

                        int start = result.indexOf("<torrent:infoHash>") + "<torrent:infoHash>".length();
                        int end = result.indexOf("</torrent:infoHash>");

                        if (start < end) {
                            String hashInfo = result.substring(start, end);
                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("url", hashInfo);
                            APIClient.get(Furk.this, "dl/add", params, Furk.this, dialog);
                        } else {
                            String query = getIntent().getExtras().getString("query");
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            try {
                                intent.setData(Uri.parse("http://thepiratebay.se/search/"+ URLEncoder.encode(query,"UTF-8")+"/0/0/1"));
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                            }
                            finally {
                                dialog.dismiss();
                                startActivity(intent);
                            }

                        }

                    }

                });
    }



    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if(position == 2)
        {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }
        else if(position == 0)
        {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MyFilesFragment(),"CURRENT")
                    .commit();
        }
        else
        {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new ActiveFilesFragment(),"CURRENT")
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void setRefreshing()
    {
       refreshing = true;
       invalidateOptionsMenu();
    }

    public void doneRefrshing()
    {
        refreshing = false;
        invalidateOptionsMenu();
    }

    private void refreshCurrentFragmet()
    {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag("CURRENT");
        if(currentFragment.getClass().equals(MyFilesFragment.class))
        {
            ((MyFilesFragment)currentFragment).refresh();
        }
        else if(currentFragment.getClass().equals(ActiveFilesFragment.class))
        {
            ((ActiveFilesFragment)currentFragment).refresh();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.furk, menu);
            restoreActionBar();

            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            searchView.setOnQueryTextListener(this);
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Search Furk.net");

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean  onPrepareOptionsMenu (Menu menu)
    {
        //MenuItem refreshButton =  (MenuItem)findViewById(R.id.action_refresh);
        MenuItem refreshButton = menu.findItem(R.id.action_refresh);
        if(refreshButton != null)
        {
            if(refreshing)
            {
                refreshButton.setActionView(R.layout.actionbar_refresh_progress);
            }
            else
            {
                refreshButton.setActionView(null);
            }

            if(collapseSearch)
            {
                MenuItem search = menu.findItem(R.id.action_search);
                search.collapseActionView();
                collapseSearch = false;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }
        else if(id == R.id.action_refresh)
        {
            //item.setActionView(R.layout.actionbar_refresh_progress);
            refreshCurrentFragmet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        collapseSearch = true;
        invalidateOptionsMenu();
        Intent intent = new Intent(this,SearchActivity.class);
        intent.putExtra("query",s);
        startActivity(intent);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public void processAPIResponse(JSONObject response) {

        try {
            if(response.getString("status").equals("ok"))
            {
                if(response.has("files"))
                {
                    JSONArray files = response.getJSONArray("files");
                    if(files.length() > 0)
                    {
                        try {
                            files.getJSONObject(0).getString("url_dl");
                            Intent intent = new Intent(this, FileActivity.class);
                            intent.putExtra("file", files.getJSONObject(0).toString());
                            startActivity(intent);
                        }
                        catch (JSONException e)
                        {
                            FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.container, new ActiveFilesFragment())
                                    .commit();
                            mTitle = getString(R.string.title_section2);
                        }
                    }
                    else
                    {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, new ActiveFilesFragment())
                                .commit();
                        mTitle = getString(R.string.title_section2);
                    }

                }

            }
            else
                Toast.makeText(this,"Error downloading",Toast.LENGTH_LONG).show();



        } catch (JSONException e) {
            Toast.makeText(this,"Error downloading",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void processAPIError(Throwable e) {
        Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();

    }


    public static class MyFilesFragment extends ListFragment {

        protected MyFilesAdapter adapter;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new MyFilesAdapter(this);
            setListAdapter(adapter);
            adapter.Execute();

//            registerForContextMenu(getListView());
        }

        public void refresh()
        {
           adapter.Execute();
        }

        @Override
        public void onStop() {
            super.onStop();
            adapter.saveState();
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(1);

        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {

            try {
                JSONObject jObj = ((FilesAdapter) l.getAdapter()).getJSONObject(position);
                Intent intent = new Intent(getActivity(),FileActivity.class);
                intent.putExtra("file",jObj.toString());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    public static class ActiveFilesFragment extends ListFragment implements APIClient.APICallback {

        protected ActiveFilesAdapter adapter;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new ActiveFilesAdapter(this);
            setListAdapter(adapter);
            adapter.Execute();
        }

        public void refresh()
        {
            adapter.Execute();
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {

            try {
                JSONObject jObj = ((ActiveFilesAdapter)l.getAdapter()).getJSONObject(position);
                if(jObj.getString("dl_status").equals("failed"))
                {
                    showRetryDialog(jObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void showRetryDialog(final JSONObject jObj)
        {
            try {

                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                //adb.setView(alertDialogView);
                adb.setTitle("Retry");
                String name = jObj.getString("name");
                adb.setMessage("Do you want to retry the download " + name +"?");
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                         retryDownload(jObj);
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                adb.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void retryDownload(JSONObject jObj)
        {
            try {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("info_hash", jObj.getString("info_hash"));
                APIClient.get(getActivity().getApplicationContext(),"dl/add", params, ActiveFilesFragment.this);
            } catch (JSONException e) {
                Toast.makeText(getActivity(),"Error adding file to downloads", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void processAPIResponse(JSONObject response) {
            Toast.makeText(getActivity(),"File added",Toast.LENGTH_LONG).show();
            this.refresh();
        }

        @Override
        public void processAPIError(Throwable e) {
            Toast.makeText(getActivity(),"Error adding file to downloads. "+e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(2);
        }
    }
}


