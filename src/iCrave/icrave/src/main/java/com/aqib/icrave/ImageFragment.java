package com.aqib.icrave;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class ImageFragment extends Fragment {

    private int progress = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        ((TextView) rootView.findViewById(R.id.imageDesc)).setText("Some image description here");
        final ProgressBar countdownBar = (ProgressBar) rootView.findViewById(R.id.countdown);

        //Declare the timer
        final Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.d("ImageFragment", "Timer: " + progress);
                countdownBar.setProgress(progress);
                if (progress >= 100) {
                    Log.d("ImageFragment", "End timer");
                    t.cancel();
                    t.purge();

                    //start the options activity

                }
                progress += 1;
            }

        }, 0, 100);

        return rootView;
    }

}
