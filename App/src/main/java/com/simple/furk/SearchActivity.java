package com.simple.furk;

import android.os.Build;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.loopj.android.http.RequestParams;
import com.simple.furk.adapter.FilesAdapter;
import com.simple.furk.adapter.SearchFilesAdapter;

import org.json.JSONObject;


public class SearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    private String query;
    private SearchFragment searchFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        query = getIntent().getExtras().getString("query");
        searchFragment = new SearchFragment(query);


        String title = getString(R.string.title_section4);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, searchFragment)
                .commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            MenuItem searchMenuItem = menu.findItem( R.id.action_search); // get my MenuItem with placeholder submenu
            searchMenuItem.expandActionView();
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            searchView.setQuery(query,false);
            searchView.clearFocus();
            searchView.setOnQueryTextListener(this);
            searchView.setIconifiedByDefault(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchFragment.changeQuery(s);
        searchFragment.makeSearch();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public static class SearchFragment extends ListFragment {
        protected SearchFilesAdapter adapter;
        private String query;

        public SearchFragment(String query)
        {
            this.query = query;
        }

        public SearchFragment()
        {
            this.query = "";
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setListShown(false);
            adapter = new SearchFilesAdapter(getActivity(),this);
            setListAdapter(adapter);
            adapter.Execute(query);
        }

        public void changeQuery(String query)
        {
            this.query = query;
        }
        public void makeSearch()
        {
            makeSearch(query,new RequestParams());
        }

        private void makeSearch(String query,RequestParams params)
        {
            this.query = query;
            setListShown(false);
            adapter.Execute(query);
        }

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
}
