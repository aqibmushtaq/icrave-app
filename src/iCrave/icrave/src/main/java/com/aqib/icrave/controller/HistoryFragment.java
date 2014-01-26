package com.aqib.icrave.controller;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * History fragment which shows the user's activity history.
 */
public class HistoryFragment extends ListFragment {

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        listView = (ListView) rootView.findViewById(android.R.id.list);
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

        MyAdapter adapter = new MyAdapter(getActivity().getApplicationContext(), R.layout.history_item, actionsDS.queryAllHistory());
        listView.setAdapter(adapter);
    }


    private class MyAdapter extends ResourceCursorAdapter {

        @SuppressWarnings("deprecation")
        public MyAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView decision = (TextView) view.findViewById(R.id.history_item_decision);
            decision.setText(CravingDecision.getStringValue(cursor.getInt(cursor.getColumnIndex(UserActionImage.COLUMN_NAME_EATING_DECISION_ID))));

            TextView date = (TextView) view.findViewById(R.id.history_item_date);
            Date createdDate = new Date(cursor.getLong(cursor.getColumnIndex(UserAction.COLUMN_NAME_CREATED_TIME)));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yy");
            date.setText(sdf.format(createdDate));
        }
    }
}