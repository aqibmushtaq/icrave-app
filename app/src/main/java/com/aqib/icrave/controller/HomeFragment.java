package com.aqib.icrave.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aqib.icrave.R;
import com.aqib.icrave.model.CravingDecision;
import com.aqib.icrave.model.ImagesDataSource;
import com.aqib.icrave.model.Login;
import com.aqib.icrave.model.UserAction;
import com.aqib.icrave.model.UserActionImage;
import com.aqib.icrave.model.UserActionImagesDataSource;
import com.aqib.icrave.model.UserActionsDataSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Home fragment
 */
public class HomeFragment extends Fragment {

    private Fragment historyFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        setCounts(rootView);

        final ICraveDisabledDialogFragment dialog = new ICraveDisabledDialogFragment();
        final Toast imageToast = Toast.makeText(getActivity().getApplicationContext(), R.string.cannot_connect_to_server, Toast.LENGTH_SHORT);

        //set on click listener to iCrave button
        rootView.findViewById(R.id.icrave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //ensure the user has logged in to continue
                    if(!Login.isLoggedIn(getActivity())) {
                        imageToast.setText(R.string.msg_login_to_continue);
                        imageToast.show();
                        return;
                    }

                    //get the amount of time the user has to wait until they can use icrave
                    long timeRemaining = (long)Math.ceil((double)getTimeRemaining() / (double)60);  //round up to the nearest minute
                    if (!hasImages()) {  //get the images if they haven't already been downloaded
                        imageToast.show();
                        return;
                    }

                    if (timeRemaining <= 0) {
                        startICraveOptionsActivity();
                    } else {
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

    public void setCounts(View rootView) {
        UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
        try {
            actionsDS.open();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        //set total cravings
        long totalCravings = actionsDS.getActiveRowCount();
        ((TextView)rootView.findViewById(R.id.total_cravings)).setText(totalCravings + "");

        //set saved cravings
        long totalSaved = actionsDS.getRowCount(CravingDecision.SAVE);
        ((TextView)rootView.findViewById(R.id.total_saved)).setText(totalSaved + "");

        //set healthy cravings
        long totalHealthy = actionsDS.getRowCount(CravingDecision.EAT_HEALTHY);
        ((TextView)rootView.findViewById(R.id.total_healthy)).setText(totalHealthy + "");

        //set unhealthy cravings
        long totalUnhealthy = actionsDS.getRowCount(CravingDecision.EAT_UNHEALTHY);
        ((TextView)rootView.findViewById(R.id.total_unhealthy)).setText(totalUnhealthy + "");
    }

    private boolean hasImages() {
        ImagesDataSource imagesDS = new ImagesDataSource(getActivity().getApplicationContext());
        try {
            imagesDS.open();
        } catch (SQLException e) {
            Log.e("HomeFragment", e.toString());
            return false;
        }

        if (imagesDS.getRowCount() == 0) {
            try {
                imagesDS.downloadAndInsertImages();  //download images
                return true;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public long getTimeRemaining() throws SQLException, CursorIndexOutOfBoundsException {
        //get the time of the last user action
        UserActionsDataSource actionDS = new UserActionsDataSource(getActivity().getApplicationContext());
        UserActionImagesDataSource imageDS = new UserActionImagesDataSource(getActivity().getApplicationContext());
        actionDS.open();
        imageDS.open();

        UserAction action;
        try {
            action = actionDS.getLastCreated();
        } catch (CursorIndexOutOfBoundsException e) {
            Log.d("HomeFragment", "No previous record found");
            return -1;
        }
        UserActionImage image = imageDS.getByUserActionId(action.getId());

        //if the last action was a healthy or unhealthy snack then the user does not need to wait to press iCrave
        boolean noWaitForSnack = !getResources().getBoolean(R.bool.ICRAVE_TIME_INTERVAL_FOR_HEALTHY_OR_UNHEALTHY_SNACKS);
        if (noWaitForSnack && (image.getEatingDecisionId() == CravingDecision.EAT_HEALTHY || image.getEatingDecisionId() == CravingDecision.EAT_UNHEALTHY))
            return -1;

        //calculate how long the user needs to wait
        long lastCreatedDate = action.getCreatedTime().getTime();
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

        setCounts(getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
    }
}