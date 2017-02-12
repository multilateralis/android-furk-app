package io.github.multilateralis.android_furk_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent().getAction().equals("io.github.multilateralis.android_furk_app.LOGOUT"))
        {
            preferences.edit().remove("api_key").commit();
        }


        String apiKey = preferences.getString("api_key","");

        if(apiKey.isEmpty())
        {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivityForResult(intent,1);
        }
        else {
            Intent intent = new Intent(this,Furk.class);
            intent.setAction(getIntent().getAction());
            intent.setData(getIntent().getData());
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == 200)
        {
            Intent intent = new Intent(this,Furk.class);
            intent.setData(getIntent().getData());
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

}
