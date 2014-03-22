package com.aqib.icrave.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aqib on 22/01/14.
 */
public class UserActionsDataSource {

    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;

    public UserActionsDataSource(Context context) {
        dbHandler = DatabaseHandler.getDatabaseHandlder(context);
    }

    /**
     * Creates a connection to the database
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        db = dbHandler.getWritableDatabase();
    }

    /**
     * Closes the connection to the database
     */
    public void close() {
        db.close();
    }

    /**
     * Creates a new User Action with the default values
     *
     * @return _id of the new User Action
     */
    public long createUserAction() {
        ContentValues values = new ContentValues();
        values.put(UserAction.COLUMN_NAME_CREATED_TIME, new Date().getTime() / 1000);
        return db.insert(UserAction.TABLE_NAME, null, values);
    }

    /**
     * Get the last user action ID
     *
     * @return ID as long
     */
    public long getLastId() {
        Cursor c = db.query(UserAction.TABLE_NAME, new String[]{UserAction.COLUMN_NAME_ID}, null, null, null, null, String.format("%s DESC", UserAction.COLUMN_NAME_ID), "1");
        c.moveToFirst();

        return c.getLong(0);
    }

    /**
     * Get the last *active* user action ID which has a UserActionImage
     *
     * @return ID as long
     */
    public long getLastActiveId() {
        Cursor c = db.rawQuery(MessageFormat.format(
            "SELECT {1}.{0} " +
                    "FROM {1} " +
                    "INNER JOIN {2} " +
                    "ON {1}.{0} = {2}.{3} " +
                    "WHERE {1}.{5} = \"TRUE\" " +
                    "ORDER BY {1}.{4} DESC " +
                    "LIMIT 1;",
                UserAction.COLUMN_NAME_ID,
                UserAction.TABLE_NAME,
                UserActionImage.TABLE_NAME,
                UserActionImage.COLUMN_NAME_USER_ACTION_ID,
                UserAction.COLUMN_NAME_CREATED_TIME,
                UserAction.COLUMN_NAME_ACTIVE
        ), null);
        c.moveToFirst();

        return c.getLong(0);
    }

    /**
     * The the last created time of the most recent active record.
     * @return The date if the action exists, otherwise a newly initialised date object.
     */
    public Date getLastCreatedTime() {
        long lastId = getLastActiveId();
        Cursor c = db.query(
                UserAction.TABLE_NAME,
                new String[] {UserAction.COLUMN_NAME_CREATED_TIME},
                String.format("%s=?", UserAction.COLUMN_NAME_ID),
                new String[] {String.valueOf(lastId)},
                null,
                null,
                null
        );
        if (c.moveToFirst())
            return new Date(c.getLong(c.getColumnIndex(UserAction.COLUMN_NAME_CREATED_TIME)));
        return new Date();
    }

    public List<UserAction> getAllUnsynced() throws ParseException {
        List<UserAction> actions = new ArrayList<UserAction>();

        Cursor c = db.query(UserAction.TABLE_NAME, UserAction.ALL_COLUMNS, String.format("%s=?", UserAction.COLUMN_NAME_SYNCHRONISED), new String[]{"FALSE"}, null, null, null);
        while (c.moveToNext()) {
            UserAction action = new UserAction();
            action.setId(c.getInt(c.getColumnIndex(UserAction.COLUMN_NAME_ID)));
            action.setActive(c.getString(c.getColumnIndex(UserAction.COLUMN_NAME_ACTIVE)).equals("1"));
            action.setCreatedTime(new Date(c.getLong(c.getColumnIndex(UserAction.COLUMN_NAME_CREATED_TIME))));
            action.setSynchronised(c.getString(c.getColumnIndex(UserAction.COLUMN_NAME_SYNCHRONISED)).equals("1"));
            action.setUndoTime(new Date(c.getLong(c.getColumnIndex(UserAction.COLUMN_NAME_UNDO_TIME))));
            actions.add(action);
        }

        Log.d("UserActionDataSource/getAllUnsynced", String.format("Return %d user actions", actions.size()));
        return actions;
    }

    public Cursor queryAllHistory () {
        String query = MessageFormat.format(
                "SELECT {3}.{0}, {4}.{1}, {3}.{2}, {4}.{6} " +
                        "FROM {3} " +
                        "INNER JOIN {4} ON {3}.{0}={4}.{5} " +
                        "WHERE {3}.{7}=\"TRUE\" " +
                        "AND {4}.{6} != {8} " +
                        "ORDER BY {4}.{1} DESC;",
                UserAction.COLUMN_NAME_ID,
                UserActionImage.COLUMN_NAME_CREATED_TIME,
                UserAction.COLUMN_NAME_SYNCHRONISED,
                UserAction.TABLE_NAME,
                UserActionImage.TABLE_NAME,
                UserActionImage.COLUMN_NAME_USER_ACTION_ID,
                UserActionImage.COLUMN_NAME_EATING_DECISION_ID,
                UserAction.COLUMN_NAME_ACTIVE,
                CravingDecision.ANOTHER_IMAGE
        );
        Log.d("UserActionDataSource", String.format("queryAllHistory: %s", query));
        return db.rawQuery(query, null);
    }

    /**
     * Update the sync attribute of a UserAction
     *
     * @param action The action to update
     * @return Whether or not the record was updated
     */
    public boolean updateSync(UserAction action) {
        ContentValues values = new ContentValues();
        values.put(UserAction.COLUMN_NAME_SYNCHRONISED, action.isSynchronised());
        return db.update(UserAction.TABLE_NAME, values, String.format("%s=?", UserAction.COLUMN_NAME_ID), new String[]{action.getId() + ""}) > 0;
    }

    public boolean deleteById (long id) {
        ContentValues values = new ContentValues();
        values.put(UserAction.COLUMN_NAME_ACTIVE, "FALSE");
        values.put(UserAction.COLUMN_NAME_UNDO_TIME, new Date().getTime() / 1000);
        values.put(UserAction.COLUMN_NAME_SYNCHRONISED, "FALSE");
        return db.update(UserAction.TABLE_NAME, values, String.format("%s=?", UserAction.COLUMN_NAME_ID), new String[]{id + ""}) > 0;
    }

    public static boolean putUserActions(String url, List<UserAction> actions, List<UserActionImage> images) throws IOException {
        //Build JSON object
        JSONArray jsonActions = new JSONArray();
        Log.d("UserActionsDataSource/putUserActions", String.format("Adding %d actions to the JSON array", actions.size()));
        for (UserAction action : actions) {
            JSONObject jsonAction = new JSONObject();
            jsonAction.put(UserAction.COLUMN_NAME_ID, action.getId());
            jsonAction.put(UserAction.COLUMN_NAME_CREATED_TIME, action.getCreatedTime().getTime());
            jsonAction.put(UserAction.COLUMN_NAME_ACTIVE, action.isActive() ? 1 : 0);
            jsonAction.put(UserAction.COLUMN_NAME_UNDO_TIME, action.getUndoTime().getTime());

            //array for all images associated with this action
            JSONArray jsonImages = new JSONArray();
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).getUserActionId() == action.getId()) {    //If the image belongs to this 'action'
                    JSONObject image = new JSONObject();

                    image.put(UserActionImage.COLUMN_NAME_SERVER_IMAGE_ID, images.get(i).getServerImageId());
                    image.put(UserActionImage.COLUMN_NAME_CREATED_TIME, images.get(i).getCreatedTime().getTime());
                    image.put(UserActionImage.COLUMN_NAME_RATING, images.get(i).getRating());
                    image.put(UserActionImage.COLUMN_NAME_EATING_DECISION_ID, images.get(i).getEatingDecisionId());

                    jsonImages.put(image);

                    //remove this image from the list so we do less work for the next action
                    images.remove(i--);
                }
            }
            Log.d("UserActionsDataSource/putUserActions", String.format("Adding %d images to the action", jsonImages.length()));
            jsonAction.put("userActionImages", jsonImages); //Add the images to their associated actions
            jsonActions.put(jsonAction);    //Add the action to the array of actions
        }

        //RESTful request
        HttpClient client = MySSLSocketFactory.getNewHttpClient();
        HttpPut request = new HttpPut(url);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");

        Log.d("UserActionsDataSource/putUserActions", jsonActions.toString());
        StringEntity input = new StringEntity(jsonActions.toString());
        request.setEntity(input);

        //Execute the query
        HttpResponse response = client.execute(request);
        Log.v("UserActionDataSource/putUserActions", String.format("status line: %s", response.getStatusLine()));

        //Get the server response
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);
        content = content.substring(1, content.length() - 1);
        content = content.replaceAll("\\\\", "");
        Log.v("UserActionDataSource/putUserActions", String.format("content: %s", content));
        JSONArray result;
        try {
            result = new JSONArray(content);
        } catch (JSONException e) {
            Log.d("UserActionDataSource/putUserActions", String.format("Result JSON array could not be parsed: %s", content));
            e.printStackTrace();
            return false;
        }

        // Go through all the user actions and check which ones have been updated on the server
        // Update successful records to have synchronised = 1
        boolean passed = true;
        for (int i = 0; i < result.length(); i++) {
            try {
                if (!result.getJSONObject(i).getBoolean("result"))
                    passed = false;
                else
                    //set synced
                    for (UserAction action : actions)
                        if (action.getId() == result.getJSONObject(i).getInt("actionId"))
                            action.setSynchronised(true);
            } catch (JSONException e) {
                e.printStackTrace();
                passed = false;
            }
        }

        return passed;
    }
}
