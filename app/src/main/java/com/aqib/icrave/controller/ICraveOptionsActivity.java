package com.aqib.icrave.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aqib.icrave.R;
import com.aqib.icrave.model.CravingDecision;
import com.aqib.icrave.model.UserActionImage;
import com.aqib.icrave.model.UserActionImagesDataSource;
import com.aqib.icrave.model.UserActionsDataSource;

import java.sql.SQLException;
import java.util.Date;

public class ICraveOptionsActivity extends RootActivity {

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_VIEW_ANOTHER = 3;

    public static final String IMAGE_SERVER_ID = "image_server_id";

    public static final int SHOW_IMAGE = 0;
    public static final int GET_CRAVING_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icrave_options);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ICraveOptionsFragment())
                    .commit();
        }
    }

    private void startImageActivity() {
        startActivityForResult(new Intent(this, ImageActivity.class), SHOW_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHOW_IMAGE) {
            Log.d("HomeFragment", "Result: " + resultCode);
            if (resultCode == RESULT_OK) {
                //get the image that was shown and show the craving options
                long imageServerId = -1;
                if (data != null)
                    imageServerId = data.getLongExtra(IMAGE_SERVER_ID, -1);
                showCravingOptions(imageServerId);
            }
        } else if (requestCode == GET_CRAVING_RESULT) { //the user has provided a result
            //check if the user wants to view another image
            if (resultCode == RESULT_VIEW_ANOTHER) {
                startImageActivity();   //show another image
            } else {
                //send the user back to the main activity once they've viewed the image and provided a result
                finish();
            }
        }
    }

    private void showCravingOptions(long imageServerId) {
        startActivityForResult(new Intent(this, ResultActivity.class).putExtra(IMAGE_SERVER_ID, imageServerId), GET_CRAVING_RESULT);
    }

    private class ICraveOptionsFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_icrave_options, container, false);

            //only show the visualise image button if is enabled in the config
            if (getResources().getBoolean(R.bool.enable_visualise)) {
                View.OnClickListener visualiseAction = new VisualiseActionListener();
                rootView.findViewById(R.id.button_visualise).setOnClickListener(visualiseAction);
            } else {
                rootView.findViewById(R.id.button_visualise).setVisibility(View.INVISIBLE);
            }

            View.OnClickListener saveAction = new SaveUserActionListener();

            //only show the save button if is enabled in the config
            if (getResources().getBoolean(R.bool.enable_save_for_later)) {
                rootView.findViewById(R.id.button_save).setOnClickListener(saveAction);
            } else {
                rootView.findViewById(R.id.button_save).setVisibility(View.INVISIBLE);
            }

            rootView.findViewById(R.id.button_healthy_snack).setOnClickListener(saveAction);
            rootView.findViewById(R.id.button_unhealthy_snack).setOnClickListener(saveAction);

            return rootView;
        }

    }

    private class VisualiseActionListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            UserActionsDataSource userActionsDS = new UserActionsDataSource(getApplicationContext());
            try {
                userActionsDS.open();
                userActionsDS.createUserAction();
                userActionsDS.close();
                startActivityForResult(new Intent(ICraveOptionsActivity.this, ImageActivity.class), SHOW_IMAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                setResult(RESULT_CANCEL);
                finish();
            }
        }
    }

    private class SaveUserActionListener implements View.OnClickListener {

        public SaveUserActionListener() {
            super();
        }

        @Override
        public void onClick(View view) {
            //find out which craving decision was made by the user
            int eatingDecision = -1;
            if (view.getId() == R.id.button_save)
                eatingDecision = CravingDecision.SAVE;
            else if (view.getId() == R.id.button_healthy_snack)
                eatingDecision = CravingDecision.EAT_HEALTHY;
            else if (view.getId() == R.id.button_unhealthy_snack)
                eatingDecision = CravingDecision.EAT_UNHEALTHY;
            else if (view.getId() == R.id.button_another_image)
                eatingDecision = CravingDecision.ANOTHER_IMAGE;

            // create a new user action and image
            try {
                //create the UserAction
                UserActionsDataSource userActionsDS = new UserActionsDataSource(getApplicationContext());
                userActionsDS.open();
                long id = userActionsDS.createUserAction();

                //create the UserActionImage without an image and rating
                UserActionImage image = new UserActionImage(id, new Date(), -1, -1, eatingDecision);
                UserActionImagesDataSource imagesDS = new UserActionImagesDataSource(getApplicationContext());
                imagesDS.open();
                imagesDS.createUserAction(image);

                //close the DB connection
                userActionsDS.close();
                Log.d("HomeFragment", String.format("Created new user action, id = %s", id));
            } catch (SQLException e) {
                Log.e("HomeFragment", e.toString());
                return;
            }

            //return to home activity
            setResult(RESULT_OK);
            finish();
        }
    }

}
