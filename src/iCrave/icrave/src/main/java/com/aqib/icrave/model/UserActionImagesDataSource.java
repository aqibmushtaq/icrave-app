package com.aqib.icrave.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * Created by aqib on 22/01/14.
 */
public class UserActionImagesDataSource {

    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;

    public UserActionImagesDataSource(Context context) {
        dbHandler = DatabaseHandler.getDatabaseHandlder(context);
    }

    /**
     * Creates a connection to the database
     *
     * @throws java.sql.SQLException
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
     * Creates a new User Action Image with the given image object
     *
     * @param image Image object to be saved
     * @return ID
     */
    public long createUserAction(UserActionImage image) {
        return db.insert(UserActionImage.TABLE_NAME, null, image.getValues());
    }

    /**
     * Get the last image._id which was shown to the user
     *
     * @return _id field of image
     */
    public long getLastImageId() {
        Cursor c = db.rawQuery(
                MessageFormat.format("SELECT i.{0} " +
                        "FROM {1} AS uai " +
                        "INNER JOIN {2} AS i " +
                        "ON uai.{3} = i.{4} " +
                        "ORDER BY uai.{5} DESC " +
                        "LIMIT 1",

                        Image.COLUMN_NAME_ID,
                        UserActionImage.TABLE_NAME,
                        Image.TABLE_NAME,
                        UserActionImage.COLUMN_NAME_SERVER_IMAGE_ID,
                        Image.COLUMN_NAME_SERVER_ID,
                        UserActionImage.COLUMN_NAME_ID),
                null);

        //first run - user is shown the first image
        if (c.getCount() == 0)
            return 0;

        c.moveToFirst();
        return c.getLong(0);
    }

}
