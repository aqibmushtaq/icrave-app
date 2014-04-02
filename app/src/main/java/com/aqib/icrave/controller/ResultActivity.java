package com.aqib.icrave.controller;

import android.os.Bundle;

import com.aqib.icrave.R;

public class ResultActivity extends RootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ResultFragment())
                    .commit();
        }
    }

}
