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

public class ResultFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

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
                float rating = ratingBar.getRating();
                Log.d("ResultFragment", "Rating: " + rating);
                if (rating == 0) {
                    Log.d("ResultFragment", "User tried submitting without rating the image");
                    toastNoRating.show();
                } else {
                    //record the result

                    //return to home screen
                    getActivity().setResult(HomeFragment.RESULT_OK);
                    getActivity().finish();
                }
            }
        };

        buttonSave.setOnClickListener(resultClickListener);
        buttonHealthy.setOnClickListener(resultClickListener);
        buttonUnhealthy.setOnClickListener(resultClickListener);
        buttonAnotherImage.setOnClickListener(resultClickListener);

        return rootView;
    }

}
