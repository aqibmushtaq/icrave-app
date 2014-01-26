package com.aqib.icrave.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aqib.icrave.R;
import com.aqib.icrave.model.UserActionsDataSource;

import java.sql.SQLException;

/**
 * Home fragment
 */
public class HomeFragment extends Fragment {

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_VIEW_ANOTHER = 3;

    public static final String IMAGE_SERVER_ID = "image_server_id";

    private static final int SHOW_IMAGE = 0;
    private static final int GET_CRAVING_RESULT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rootView.findViewById(R.id.icrave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a new user action
                try {
                    UserActionsDataSource userActionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
                    userActionsDS.open();
                    long id = userActionsDS.createUserAction();
                    userActionsDS.close();
                    Log.d("HomeFragment", String.format("Created new user action, id = %s", id));
                } catch (SQLException e) {
                    Log.e("HomeFragment", e.toString());
                    return;
                }

                // move on to image activity
                startImageActivity();
            }
        });

        return rootView;
    }

    private void startImageActivity() {
        startActivityForResult(new Intent(getActivity(), ImageActivity.class), SHOW_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_IMAGE) {
            Log.d("HomeFragment", "Result: " + resultCode);
            if (resultCode == RESULT_OK) {
                //get the image that was shown and show the craving options
                long imageServerId = -1;
                if (data != null)
                    imageServerId = data.getLongExtra(IMAGE_SERVER_ID, -1);
                showCravingOptions(imageServerId);
            }
        } else if (requestCode == GET_CRAVING_RESULT) {
            //check if the user wants to view another image
            if (resultCode == RESULT_VIEW_ANOTHER) {
                startActivityForResult(new Intent(getActivity(), ImageActivity.class), SHOW_IMAGE);
            }
        }
    }

    private void showCravingOptions(long imageServerId) {
        startActivityForResult(new Intent(getActivity(), ResultActivity.class).putExtra(IMAGE_SERVER_ID, imageServerId), GET_CRAVING_RESULT);
    }

}