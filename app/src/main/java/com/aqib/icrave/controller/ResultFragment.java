package com.aqib.icrave.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.aqib.icrave.R;
import com.aqib.icrave.model.CravingDecision;
import com.aqib.icrave.model.UserActionImage;
import com.aqib.icrave.model.UserActionImagesDataSource;
import com.aqib.icrave.model.UserActionsDataSource;

import java.sql.SQLException;
import java.util.Date;

public class ResultFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

        //get image id
        final long imageServerId = getActivity().getIntent().getLongExtra(ICraveOptionsActivity.IMAGE_SERVER_ID, -1);

        //get rating component
        final RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.rating);

        //set action listeners for the craving result buttons
        final Button buttonSave = (Button) rootView.findViewById(R.id.button_save);
        final Button buttonHealthy = (Button) rootView.findViewById(R.id.button_healthy_snack);
        final Button buttonUnhealthy = (Button) rootView.findViewById(R.id.button_unhealthy_snack);
        final Button buttonAnotherImage = (Button) rootView.findViewById(R.id.button_another_image);

        //create toast object once
        final Toast toastNoRating = Toast.makeText(getActivity().getApplicationContext(), R.string.error_no_rating, Toast.LENGTH_LONG);

        View.OnClickListener resultClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rating = (int) ratingBar.getRating();
                Log.d("ResultFragment", String.format("Rating: %s", rating));

                if (rating == 0) {
                    Log.d("ResultFragment", "User tried submitting without rating the image");
                    toastNoRating.show();
                } else {
                    //get the last UserAction and build the new UserActionImage record
                    UserActionImage image;
                    try {
                        UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
                        actionsDS.open();
                        long lastActionId = actionsDS.getLastId();
                        Log.d("ResultFragment", String.format("Last action ID = %s", lastActionId));
                        image = new UserActionImage(lastActionId, new Date(), imageServerId, rating, -1);
                        actionsDS.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }

                    if (view.getId() == R.id.button_save)
                        image.setEatingDecisionId(CravingDecision.SAVE);
                    else if (view.getId() == R.id.button_healthy_snack)
                        image.setEatingDecisionId(CravingDecision.EAT_HEALTHY);
                    else if (view.getId() == R.id.button_unhealthy_snack)
                        image.setEatingDecisionId(CravingDecision.EAT_UNHEALTHY);
                    else if (view.getId() == R.id.button_another_image)
                        image.setEatingDecisionId(CravingDecision.ANOTHER_IMAGE);

                    //insert UserActionImage record into DB
                    try {
                        UserActionImagesDataSource imagesDS = new UserActionImagesDataSource(getActivity().getApplicationContext());
                        imagesDS.open();
                        imagesDS.createUserAction(image);
                        imagesDS.close();
                    } catch (SQLException e) {
                        Log.e("ResultFragment", e.toString());
                        return;
                    }

                    //return to home screen
                    if (view.getId() == R.id.button_another_image)
                        getActivity().setResult(ICraveOptionsActivity.RESULT_VIEW_ANOTHER);
                    else
                        getActivity().setResult(ICraveOptionsActivity.RESULT_OK);
                    getActivity().finish();
                }
            }
        };

        //set action listeners
        //only show the save button if is enabled in the config
        if (getResources().getBoolean(R.bool.enable_save_for_later)) {
            buttonSave.setOnClickListener(resultClickListener);
        } else {
            buttonSave.setVisibility(View.INVISIBLE);
        }

        buttonHealthy.setOnClickListener(resultClickListener);
        buttonUnhealthy.setOnClickListener(resultClickListener);
        buttonAnotherImage.setOnClickListener(resultClickListener);

        return rootView;
    }

}
