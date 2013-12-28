package com.aqib.icrave;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageFragment extends Fragment {

    protected AsyncTask<Integer, Integer, Integer> task;

    private int progress = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        ((TextView) rootView.findViewById(R.id.imageDesc)).setText("Some image description here");
        final ProgressBar countdownBar = (ProgressBar) rootView.findViewById(R.id.countdown);

        task = new AsyncTask<Integer, Integer, Integer>() {
            protected Integer doInBackground(Integer... ints) {
                while (progress++ < 100) {
                    if (isCancelled())
                        return -1;
                    else if (getActivity() == null) { //Ensure that the activity still exists
                        Log.d("ImageFragment", "Activity doesn't exists therefore stopping the countdown process");
                        cancel(true);
                    } else {
                        Log.d("ImageFragment", "Timer: " + progress);
                        countdownBar.setProgress(progress);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                getActivity().setResult(HomeFragment.RESULT_OK);
                getActivity().finish();
                cancel(true);
                return 1;
            }
        };
        task.execute(null);

        return rootView;
    }


}
