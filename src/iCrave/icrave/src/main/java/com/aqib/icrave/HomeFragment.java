package com.aqib.icrave;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Home fragment
 */
public class HomeFragment extends Fragment {

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 2;

    private static final int GET_CRAVING_RESULT = 0;
    private static final int CRAVING_RESULT_PASS = 0;
    private static final int CRAVING_RESULT_EAT_HEALTHY = 1;
    private static final int CRAVING_RESULT_EAT_UNHEALTHY = 2;
    private static final int CRAVING_RESULT_ANOTHER_IMAGE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rootView.findViewById(R.id.icrave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImageActivity();
            }
        });

        return rootView;
    }

    private void startImageActivity() {
        startActivityForResult(new Intent(getActivity(), ImageActivity.class), GET_CRAVING_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_CRAVING_RESULT) {
            Log.d("HomeFragment", "Result: " + resultCode);
            if (resultCode == RESULT_OK)
                showCravingOptions();
        }
    }

    private void showCravingOptions() {
        Log.d("ImageFragment", "showCravingOptions");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose)
                .setItems(R.array.image_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("ImageFragment", "Craving result: " + which);
                        //record result

                        if (which == CRAVING_RESULT_ANOTHER_IMAGE)
                            startImageActivity();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d("ImageFragment", "Craving options cancelled by the user");
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}