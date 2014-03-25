package com.aqib.icrave.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aqib.icrave.R;
import com.aqib.icrave.model.UserActionsDataSource;

import java.sql.SQLException;
import java.util.Date;

/**
 * Home fragment
 */
public class HomeFragment extends Fragment {

    private Fragment historyFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        final ICraveDisabledDialogFragment dialog = new ICraveDisabledDialogFragment();

        //set on click listener to iCrave button
        rootView.findViewById(R.id.icrave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    long timeRemaining = (long)Math.ceil((double)getTimeRemaining() / (double)60);  //round up to the nearest minute
                    if (timeRemaining < 0)
                        startICraveOptionsActivity();
                    else {
                        dialog.setTimeRemaining(timeRemaining);
                        dialog.show(getFragmentManager(), "");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e("HomeFragment", "Database error, could not determine whether the icrave button is active or not.");
                    return;
                }
            }
        });

        return rootView;
    }

    public long getTimeRemaining() throws SQLException {
        //get the time of the last user action
        UserActionsDataSource actionDS = new UserActionsDataSource(getActivity().getApplicationContext());
        actionDS.open();
        long lastCreatedDate = actionDS.getLastCreatedTime().getTime();
        long now = new Date().getTime() / 1000;
        long delay = getResources().getInteger(R.integer.ICRAVE_TIME_INTERVAL);
        long timeRemaining = (lastCreatedDate + delay) - now;   // 1200 milliseconds is 20 minutes
        Log.d("HomeFragment", String.format("%d + %d - %d", lastCreatedDate, delay, now));
        Log.d("HomeFragment", String.format("timeRemaining = %d", timeRemaining));
        return timeRemaining;
    }

    public class ICraveDisabledDialogFragment extends DialogFragment {

        private long timeRemaining;

        public void setTimeRemaining(long timeRemaining) {
            this.timeRemaining = timeRemaining;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.error_icrave_disabled, timeRemaining))
                    .setPositiveButton(R.string.undo, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //remove from database
                            UserActionsDataSource actionDS = new UserActionsDataSource(getActivity().getApplicationContext());
                            try {
                                actionDS.open();
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return;
                            }
                            long actionId = actionDS.getLastActiveId();
                            Log.d("HistoryFragment", String.format("Deleting last action ID found: %s", actionId));
                            actionDS.deleteById(actionId);

                            actionDS.close();
                            startICraveOptionsActivity();
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void startICraveOptionsActivity() {
        startActivityForResult(new Intent(getActivity().getApplicationContext(), ICraveOptionsActivity.class), 0);
    }

    public void setHistoryFragment (Fragment historyFragment) {
        this.historyFragment = historyFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Refresh the history fragment list
        UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
        try {
            actionsDS.open();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ((HistoryFragment)historyFragment).resetListView(actionsDS);
        actionsDS.close();
    }
}