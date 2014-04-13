package com.aqib.icrave.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.aqib.icrave.R;

public class RootActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //if the user has already logged in then hide the login button
        MenuItem loginItem = menu.findItem(R.id.action_login);
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_FILE_NAME, MODE_PRIVATE);
        long userId = prefs.getLong(LoginActivity.PREF_USER_ID_NAME, -1);
        loginItem.setVisible(userId == -1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_portion_sizes:
                startActivity(new Intent(this, PortionSizesActivity.class));
                return true;
            case R.id.action_consent:
                startActivity(new Intent(this, ConsentActivity.class));
                return true;
            case R.id.action_login:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
