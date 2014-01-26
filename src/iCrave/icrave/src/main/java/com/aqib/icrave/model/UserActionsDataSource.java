package com.aqib.icrave.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

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
        return db.insert(UserAction.TABLE_NAME, UserAction.COLUMN_NAME_UNDO_TIME, new ContentValues());
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

    public boolean deleteById (long id) {
        ContentValues values = new ContentValues();
        values.put(UserAction.COLUMN_NAME_ACTIVE, "FALSE");
        values.put(UserAction.COLUMN_NAME_UNDO_TIME, new Date().getTime());
        values.put(UserAction.COLUMN_NAME_SYNCHRONISED, "FALSE");
        return db.update(UserAction.TABLE_NAME, values, String.format("%s=?", UserAction.COLUMN_NAME_ID), new String[]{id + ""}) > 0 ? true : false;
    }
}
