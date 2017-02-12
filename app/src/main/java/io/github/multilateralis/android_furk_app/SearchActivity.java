package io.github.multilateralis.android_furk_app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import io.github.multilateralis.android_furk_app.adapter.FilesAdapter;
import io.github.multilateralis.android_furk_app.adapter.SearchFilesAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;



public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String searchQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchQuery = getIntent().getExtras().getString("query");


        if(savedInstanceState == null) {
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setSearchQuery(searchQuery);
            searchFragment.setSortQuery("cached");
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.container, searchFragment,"CURRENT")
                    .commit();
        }

    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.title_section4));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);

        restoreActionBar();

        MenuItem searchMenuItem = menu.findItem( R.id.action_search);
        searchMenuItem.expandActionView();
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQuery(searchQuery,false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);

        return true;
    }



    private String getSortQuery(int itemId)
    {
        if(itemId == R.id.menuSortCached)
            return "cached";
        else if(itemId == R.id.menuSortRelevance)
            return "relevance";
        else if(itemId == R.id.menuSortSize)
            return "size";
        else if(itemId == R.id.menuSortSizeAsc)
            return "size_asc";
        else if(itemId == R.id.menuSortDate)
            return "date";
        else if(itemId == R.id.menuSortDateAsc)
            return "date_asc";
        else
            return "";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(item.isCheckable() && !item.isChecked())
        {
            item.setChecked(true);
            SearchFragment searchFragment = getSearchFragment();
            searchFragment.changeSortQuery(getSortQuery(id));
            searchFragment.makeSearch();
            searchFragment.getView().requestFocus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        SearchFragment searchFragment = getSearchFragment();
        searchFragment.changeSearchQuery(s);
        searchFragment.makeSearch();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

     private SearchFragment getSearchFragment()
     {
         FragmentManager fragmentManager = getSupportFragmentManager();
         return (SearchFragment)fragmentManager.findFragmentByTag("CURRENT");
     }

    public static class SearchFragment extends ListFragment implements APIClient.APICallback{
        protected SearchFilesAdapter adapter;
        private String searchQuery;
        private String sortQuery;

        public SearchFragment()
        {
            this.searchQuery = "";
            this.sortQuery = "cached";
        }

        public void setSearchQuery(String searchQuery)
        {
            this.searchQuery = searchQuery;
        }

        public void setSortQuery(String sortQuery)
        {
            this.sortQuery = sortQuery;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            adapter = new SearchFilesAdapter(this);
            setListAdapter(adapter);

            if(adapter.loadState(savedInstanceState)) {
                searchQuery = savedInstanceState.getString("search_query");
                sortQuery = savedInstanceState.getString("search_sort_query");
            }
            else
            {
                setListShown(false);
                adapter.Execute(searchQuery, sortQuery);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            super.onSaveInstanceState(outState);
            adapter.saveState(outState);
        }

        public void changeSearchQuery(String query)
        {
            this.searchQuery = query;
        }

        public void changeSortQuery(String query)
        {
            this.sortQuery = query;
        }

        public void makeSearch()
        {
            setListShown(false);
            adapter.Execute(searchQuery, sortQuery);
        }


        private void showRetryDialog(final JSONObject jObj)
        {
            try {

                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                //adb.setView(alertDialogView);
                adb.setTitle("Retry");
                String name = jObj.getString("name");
                adb.setMessage("Do you want to add the file \""+ name + "\" to downloads?");
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                try {
                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put("info_hash", jObj.getString("info_hash"));
                                    APIClient.get(getActivity().getApplicationContext(),"dl/add", params, SearchFragment.this);
                                } catch (JSONException e) {
                                    Toast.makeText(getActivity(),"Failed to add file. " + e.getMessage() ,Toast.LENGTH_LONG).show();
                                }
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
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {

            try {
                JSONObject jObj = ((FilesAdapter) l.getAdapter()).getJSONObject(position);
                if(jObj.getString("is_ready").equals("1")) {
                    Intent intent = new Intent(getActivity(), FileActivity.class);
                    intent.putExtra("file", jObj.toString());
                    startActivity(intent);
                }
                else
                {
                    showRetryDialog(jObj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        @Override
        public void processAPIResponse(JSONObject response) {

            try {
                String name = response.getString("name");
                Toast.makeText(getActivity(),"File" + name + "added",Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(getActivity(),"File added",Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void processAPIError(Throwable e) {
            Toast.makeText(getActivity(),"Failed to add file. " + e.getMessage() ,Toast.LENGTH_LONG).show();
        }
    }
}
