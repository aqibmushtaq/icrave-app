package com.aqib.icrave.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aqib.icrave.R;
import com.aqib.icrave.model.CravingDecision;
import com.aqib.icrave.model.UserAction;
import com.aqib.icrave.model.UserActionImage;
import com.aqib.icrave.model.UserActionImagesDataSource;
import com.aqib.icrave.model.UserActionsDataSource;
import com.aqib.icrave.view.StatusCircle;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * History fragment which shows the user's activity history.
 */
public class HistoryFragment extends ListFragment {

    private ListView listView;
    private MyAdapter listAdapter;
    private Toast toastSyncResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);

        //set on click listener to undo button
        rootView.findViewById(R.id.undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show confirmation dialog
                showUndoConfirmation();
            }
        });

        //set on click listener to refresh button
        rootView.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserActionsDataSource actionDS = new UserActionsDataSource(getActivity().getApplicationContext());
                try {
                    actionDS.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
                resetListView(actionDS);
            }
        });


        toastSyncResult = Toast.makeText(getActivity().getApplicationContext(), "", Toast.LENGTH_SHORT);
        //set on click listener to sync button
        rootView.findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncHistory();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
        try {
            actionsDS.open();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        resetListView(actionsDS);
        actionsDS.close();
    }

    private void syncHistory() {
        AsyncTask<String, Integer, List<UserAction>> syncUserActions = new AsyncTask<String, Integer, List<UserAction>>() {
            @Override
            protected List<UserAction> doInBackground(String... urls) {
                UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
                UserActionImagesDataSource actionImagesDS = new UserActionImagesDataSource(getActivity().getApplicationContext());
                try {
                    actionsDS.open();
                    actionImagesDS.open();
                    List<UserAction> allUnsyncedActions = actionsDS.getAllUnsynced();
                    List<UserActionImage> allUnsyncedImages = actionImagesDS.getAllUnsynced();
                    UserActionsDataSource.putUserActions(urls[0], allUnsyncedActions, allUnsyncedImages);
                    return allUnsyncedActions;
                } catch (ParseException e) {
                    e.printStackTrace();
                    showCouldNotSyncMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    showCouldNotSyncMessage();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showCouldNotSyncMessage();
                }

                return null;
            }
        };
        String address = getString(R.string.server_address);
        String endpoint = getString(R.string.server_rest_url_user_action_create);
        String apiKeyParam = getString(R.string.server_rest_param_api_key);
        String apiKey = getString(R.string.server_api_key);
        syncUserActions.execute(String.format("%s%s?%s=%s", address, endpoint, apiKeyParam, apiKey));

        try {
            List<UserAction> results = syncUserActions.get();
            if (results.size() == 0) {
                toastSyncResult.setText(getString(R.string.nothing_to_sync_msg));
                toastSyncResult.show();
                return;
            }

            UserActionsDataSource actionsDS = new UserActionsDataSource(getActivity().getApplicationContext());
            actionsDS.open();
            int updated = 0;
            for (UserAction result : results)
                if (result.isSynchronised())
                    updated += actionsDS.updateSync(result) ? 1 : 0;

            resetListView(actionsDS);
            toastSyncResult.setText(getString(R.string.sync_result_msg, updated, results.size()));
            toastSyncResult.show();
            actionsDS.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCouldNotSyncMessage() {
        toastSyncResult.setText(R.string.could_not_sync_msg);
        toastSyncResult.show();
    }

    private void showUndoConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_are_you_sure)
                .setTitle(R.string.info);

        builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
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

                //refresh the list view to reflect the change
                resetListView(actionDS);

                actionDS.close();
            }
        });

        builder.setNegativeButton(getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void resetListView(UserActionsDataSource actionDS) {
        listAdapter = new MyAdapter(getActivity().getApplicationContext(), R.layout.history_item, actionDS.queryAllHistory());
        listView.setAdapter(listAdapter);
    }

    private class MyAdapter extends ResourceCursorAdapter {

        @SuppressWarnings("deprecation")
        public MyAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView decisionView = (TextView) view.findViewById(R.id.history_item_decision);
            String decision = CravingDecision.getStringValue(cursor.getInt(cursor.getColumnIndex(UserActionImage.COLUMN_NAME_EATING_DECISION_ID)));
            decisionView.setText(decision);

            TextView dateView = (TextView) view.findViewById(R.id.history_item_date);
            Date createdDate = new Date(cursor.getLong(cursor.getColumnIndex(UserAction.COLUMN_NAME_CREATED_TIME)));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yy");
            dateView.setText(sdf.format(createdDate));

            boolean isSynced = cursor.getString(cursor.getColumnIndex(UserAction.COLUMN_NAME_SYNCHRONISED)).equals("1");
            StatusCircle syncStatusView = (StatusCircle) view.findViewById(R.id.sync_status);
            syncStatusView.setSynced(isSynced);
        }
    }
}