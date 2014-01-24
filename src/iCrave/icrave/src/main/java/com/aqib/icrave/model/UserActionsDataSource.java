package com.aqib.icrave.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

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

}
