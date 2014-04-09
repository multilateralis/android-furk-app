package com.simple.furk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.simple.furk.adapter.ActiveFilesAdapter;
import com.simple.furk.adapter.FilesAdapter;
import com.simple.furk.adapter.MyFilesAdapter;
import com.simple.furk.adapter.SearchFilesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Furk extends ActionBarActivity
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
    private static Context context;
    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler(this.getBaseContext()));
        context = Furk.this;
        setContentView(R.layout.activity_furk);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        String scheme = getIntent().getScheme();
        if (scheme != null) {
             if (scheme.equals("magnet") || scheme.equals("http") || scheme.equals("https")) {
                String torrent = getIntent().getDataString();
                APIClient request = new APIClient(this);
                request.execute("dl/add", "url=" + torrent);
                Toast.makeText(getApplicationContext(), "Adding torrent", Toast.LENGTH_LONG).show();
            }
        }

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
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MyFilesFragment(),"CURRENT")
                    .commit();
        }
        else
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
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
        FragmentManager fragmentManager = getSupportFragmentManager();
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
            getMenuInflater().inflate(R.menu.options_menu_main, menu);
            restoreActionBar();

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                searchView.setOnQueryTextListener(this);
                searchView.setIconifiedByDefault(false);
            }
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
        mTitle = getString(R.string.title_section4);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new SearchFragment(s),"CURRENT")
                .commit();

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public void processAPIResponse(JSONObject result) {

        try {
            if(result.getString("status").equals("ok"))
            {
                Toast.makeText(getApplicationContext(),"Torrent added",Toast.LENGTH_LONG).show();

                if(result.has("files"))
                {
                    JSONArray files = result.getJSONArray("files");
                    if(files.length() > 0)
                    {
                        FileActivity.FILE = files.getJSONObject(0);
                        Intent intent = new Intent(this,FileActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, new ActiveFilesFragment())
                                .commit();
                        mTitle = getString(R.string.title_section2);
                    }

                }

            }
            else
                Toast.makeText(getApplicationContext(),"Error downloading",Toast.LENGTH_LONG).show();



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_files, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_active_downloads, container, false);
                    break;
/*                case 3:
                    rootView = inflater.inflate(R.layout.fragment_settings, container, false);
                    break;*/
            }
            //View rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    public static abstract class FilesFragment extends ListFragment {

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {

            try {
                JSONObject jObj = ((FilesAdapter) l.getAdapter()).getJSONObject(position);
                FileActivity.FILE = jObj;
                Intent intent = new Intent(getActivity(),FileActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static class MyFilesFragment extends FilesFragment {

        protected MyFilesAdapter adapter;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            adapter = new MyFilesAdapter(getActivity());
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
            adapter.saveMyFiles();
        }
//
//        @Override
//        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//            super.onCreateContextMenu(menu, v, menuInfo);
//
//            MenuInflater inflater = getMenuInflater();
//            inflater.inflate(R.menu.context_my_files, menu);
//        }
//
//        @Override
//        public boolean onContextItemSelected(MenuItem item) {
//            try {
//                JSONObject jObj = adapter.getJSONObject(getListView().getSelectedItemPosition());
//                String url = jObj.getString("url_dl");
//
//                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
//                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    clipboard.setText(url);
//                } else {
//                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", url);
//                    clipboard.setPrimaryClip(clip);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return super.onContextItemSelected(item);
//        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(1);

        }
    }

    public static class SearchFragment extends FilesFragment {
        protected SearchFilesAdapter adapter;
        private final String query;
        public SearchFragment(String query)
        {
            this.query = query;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setEmptyText("Fetching..");
            adapter = new SearchFilesAdapter(getActivity());
            setListAdapter(adapter);
            adapter.Execute(query);
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(4);
        }

    }

    public static class ActiveFilesFragment extends ListFragment implements APIClient.APICallback {

        protected ActiveFilesAdapter adapter;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            //setEmptyText("Fetching..");
            adapter = new ActiveFilesAdapter(getActivity());
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
                adb.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        APIClient request = new APIClient(ActiveFilesFragment.this);
                        try {
                            request.execute("dl/add","info_hash="+ jObj.getString("info_hash"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } });


                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    } });
                adb.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void retryDownload()
        {

        }

        @Override
        public void processAPIResponse(JSONObject result) {
            Toast.makeText(getActivity(),"File added",Toast.LENGTH_LONG).show();
            this.refresh();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Furk) activity).onSectionAttached(2);
        }
    }
}


