package com.aqib.icrave;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * History fragment which shows the user's activity history.
 */
public class HistoryFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] values = new String[]{
                "Item1", "Item2", "Item3", "Item4", "Item5",
                "Item6", "Item7", "Item8", "Item9", "Item10",
                "Item11", "Item12", "Item13", "Item14", "Item15"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data

    }
}