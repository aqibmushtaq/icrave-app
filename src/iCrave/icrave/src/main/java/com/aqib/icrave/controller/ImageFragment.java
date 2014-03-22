package com.aqib.icrave.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aqib.icrave.R;
import com.aqib.icrave.model.Image;
import com.aqib.icrave.model.ImagesDataSource;
import com.aqib.icrave.model.UserActionImagesDataSource;

import java.sql.SQLException;

public class ImageFragment extends Fragment {

    private static final int UPDATE_PROGRESS_BAR = 0;
    private static final int UPDATE_TEXT = 1;

    protected AsyncTask<Void, Void, Void> task;

    private int progress = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        //get the last image that was shown to the user
        UserActionImagesDataSource userActionImagesDS = new UserActionImagesDataSource(getActivity().getApplicationContext());
        ImagesDataSource imagesDS = new ImagesDataSource(getActivity().getApplicationContext());
        try {
            userActionImagesDS.open();
            imagesDS.open();
        } catch (SQLException e) {
            Log.e("ImageFragment", e.toString());
            return null;
        }
        long lastImageId = userActionImagesDS.getLastImageId();
        Log.i("ImageFragment", String.format("Last image ID: %s", lastImageId));

        //get the next image ID
        long totalImages = imagesDS.getRowCount();
        long nextImageId = (lastImageId + 1) % totalImages;
        final Image image = imagesDS.getImageById(nextImageId);
        String imageTitle = image.getTitle();
        Log.i("ImageFragment", String.format("Next image ID: %s", nextImageId));
        Log.i("ImageFragment", String.format("Next image title: %s", imageTitle));

        //close DB connections
        userActionImagesDS.close();
        imagesDS.close();

        //Countdown UI components
        ((TextView) rootView.findViewById(R.id.imageDesc)).setText(imageTitle);
        final ProgressBar countdownBar = (ProgressBar) rootView.findViewById(R.id.countdown);
        final TextView timeRemaining = (TextView) rootView.findViewById(R.id.timeRemaining);

        //Countdown UI handler
        final Handler uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int operation = msg.arg1;
                int value = msg.arg2;

                if (operation == UPDATE_PROGRESS_BAR) {
                    Log.d("ImageFragment", "Updating progress bar");
                    countdownBar.setProgress(value);
                } else if (operation == UPDATE_TEXT) {
                    Log.d("ImageFragment", "Updating text field");
                    timeRemaining.setText(value + "");
                }
            }
        };

        //Countdown thread
        task = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                while (progress++ < 100) {
                    if (isCancelled())
                        return null;
                    else if (getActivity() == null) { //Ensure that the activity still exists
                        Log.d("ImageFragment", "Activity doesn't exists therefore stopping the countdown process");
                        cancel(true);
                    } else { //update the user interface to show the countdown
                        Log.d("ImageFragment", "Timer: " + progress);
                        if (progress % 10 == 0) { //update the text field showing the remaining time in seconds
                            Message m = new Message();
                            m.arg1 = UPDATE_TEXT;
                            m.arg2 = 10 - progress / 10;
                            uiHandler.sendMessage(m);
                        }

                        //update the progress bar
                        Message m = new Message();
                        m.arg1 = UPDATE_PROGRESS_BAR;
                        m.arg2 = progress;
                        uiHandler.sendMessage(m);

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                getActivity().setResult(ICraveOptionsActivity.RESULT_OK, new Intent().putExtra(ICraveOptionsActivity.IMAGE_SERVER_ID, image.getServerId()));
                getActivity().finish();
                cancel(true);
                return null;
            }
        };
        task.execute((Void[]) null);

        return rootView;
    }

}
