package com.aqib.icrave.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.aqib.icrave.R;
import com.aqib.icrave.model.CravingDecision;
import com.aqib.icrave.model.UserAction;
import com.aqib.icrave.model.UserActionImage;
import com.aqib.icrave.model.UserActionsDataSource;
import com.aqib.icrave.view.StatusCircle;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * History fragment which shows the user's activity history.
 */
public class HistoryFragment extends ListFragment {

    private ListView listView;
    private MyAdapter listAdapter;

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



        return rootView;
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

    private void resetListView(UserActionsDataSource actionDS) {
        listAdapter = new MyAdapter(getActivity().getApplicationContext(), R.layout.history_item, actionDS.queryAllHistory());
        listView.setAdapter(listAdapter);
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

            boolean isSynced = cursor.getString(cursor.getColumnIndex(UserAction.COLUMN_NAME_SYNCHRONISED)).equals("TRUE") ? true : false;
            StatusCircle syncStatusView = (StatusCircle) view.findViewById(R.id.sync_status);
            syncStatusView.setSynced(isSynced);
        }
    }
}