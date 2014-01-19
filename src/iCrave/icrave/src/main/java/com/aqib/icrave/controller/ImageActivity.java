package com.aqib.icrave.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.aqib.icrave.R;

public class ImageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.SHOW_INFO_BEFORE_IMAGE))
            showInfo(savedInstanceState);
        else
            showImageFragment(savedInstanceState);
    }

    /**
     * Notify the user that they need to imagine the text
     *
     * @param savedInstanceState
     */
    private void showInfo(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.image_info)
                .setTitle(R.string.info);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showImageFragment(savedInstanceState);
            }
        });

        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(HomeFragment.RESULT_CANCEL);
                finish();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                setResult(HomeFragment.RESULT_CANCEL);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Show the user the text which they need to imagine
     *
     * @param savedInstanceState
     */
    private void showImageFragment(Bundle savedInstanceState) {
        setContentView(R.layout.activity_image);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ImageFragment())
                    .commit();
        }
    }

}
