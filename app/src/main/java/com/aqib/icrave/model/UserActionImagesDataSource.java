package com.aqib.icrave.model;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    /**
     * Get the User Action Image by the related User Action id. If there are many User Action
     * Images then the most recent is return.
     * @param actionId
     * @return UserActionImage for the UserAction
     */
    public UserActionImage getByUserActionId(long actionId) {
        Cursor c = db.query(
                false,
                UserActionImage.TABLE_NAME,
                UserActionImage.ALL_COLUMNS,
                String.format("%s=?", UserActionImage.COLUMN_NAME_USER_ACTION_ID),
                new String[] {String.valueOf(actionId)},
                null,
                null,
                String.format("%s DESC", UserActionImage.COLUMN_NAME_CREATED_TIME),
                "1"
        );
        if (c.moveToFirst())
            return getUserActionImage(c);

        throw new CursorIndexOutOfBoundsException("No row to return");
    }

    public List<UserActionImage> getAllUnsynced() throws ParseException {
        List<UserActionImage> images = new ArrayList<UserActionImage>();

        Cursor c = db.query(UserActionImage.TABLE_NAME, UserActionImage.ALL_COLUMNS, String.format("%s=?", UserActionImage.COLUMN_NAME_SYNCHRONISED), new String[]{"FALSE"}, null, null, null);
        while (c.moveToNext()) {
            UserActionImage image = getUserActionImage(c);
            images.add(image);
        }

        Log.d("UserActionDataSource/getAllUnsynced", String.format("Return %d user actions", images.size()));
        return images;
    }

    private UserActionImage getUserActionImage(Cursor c) {
        UserActionImage image = new UserActionImage();
        image.setId(c.getInt(c.getColumnIndex(UserActionImage.COLUMN_NAME_ID)));
        image.setUserActionId(c.getInt(c.getColumnIndex(UserActionImage.COLUMN_NAME_USER_ACTION_ID)));
        image.setCreatedTime(new Date(c.getLong(c.getColumnIndex(UserActionImage.COLUMN_NAME_CREATED_TIME))));
        image.setServerImageId(c.getInt(c.getColumnIndex(UserActionImage.COLUMN_NAME_SERVER_IMAGE_ID)));
        image.setRating(c.getInt(c.getColumnIndex(UserActionImage.COLUMN_NAME_RATING)));
        image.setEatingDecisionId(c.getInt(c.getColumnIndex(UserActionImage.COLUMN_NAME_EATING_DECISION_ID)));
        return image;
    }
}
