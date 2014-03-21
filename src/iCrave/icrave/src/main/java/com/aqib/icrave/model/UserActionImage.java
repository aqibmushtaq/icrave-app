package com.aqib.icrave.model;

import android.content.ContentValues;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by aqib on 19/01/14.
 */
public class UserActionImage {

    public static final String TABLE_NAME = "user_action_images";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_USER_ACTION_ID = "user_action_id";
    public static final String COLUMN_NAME_CREATED_TIME = "created_time";
    public static final String COLUMN_NAME_SERVER_IMAGE_ID = "server_image_id";
    public static final String COLUMN_NAME_RATING = "rating";
    public static final String COLUMN_NAME_EATING_DECISION_ID = "eating_decision_id";
    public static final String COLUMN_NAME_SYNCHRONISED = "synchronised";
    public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_USER_ACTION_ID, COLUMN_NAME_CREATED_TIME, COLUMN_NAME_SERVER_IMAGE_ID, COLUMN_NAME_RATING, COLUMN_NAME_EATING_DECISION_ID, COLUMN_NAME_SYNCHRONISED};


    public static final String CREATE_USER_ACTION_IMAGE_TABLE = MessageFormat.format(
            "CREATE TABLE IF NOT EXISTS {0} (" +
                    "{1} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "{2} INTEGER, " +
                    "{3} TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "{4} INTEGER, " +
                    "{5} INTEGER, " +
                    "{6} INTEGER, " +
                    "{7} BOOLEAN DEFAULT FALSE" +
                    ")",
            TABLE_NAME, COLUMN_NAME_ID, COLUMN_NAME_USER_ACTION_ID, COLUMN_NAME_CREATED_TIME, COLUMN_NAME_SERVER_IMAGE_ID, COLUMN_NAME_RATING, COLUMN_NAME_EATING_DECISION_ID, COLUMN_NAME_SYNCHRONISED
    );

    private long _id;
    private long userActionId;
    private Date createdTime;
    private long serverImageId;
    private int rating;
    private long eatingDecisionId;
    private boolean synchronised;

    public UserActionImage() {}

    public UserActionImage(long userActionId, Date createdTime, long serverImageId, int rating, long eatingDecisionId) {
        this.userActionId = userActionId;
        this.createdTime = createdTime;
        this.serverImageId = serverImageId;
        this.rating = rating;
        this.eatingDecisionId = eatingDecisionId;
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public long getUserActionId() {
        return userActionId;
    }

    public void setUserActionId(long userActionId) {
        this.userActionId = userActionId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public long getServerImageId() {
        return serverImageId;
    }

    public void setServerImageId(long serverImageId) {
        this.serverImageId = serverImageId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public long getEatingDecisionId() {
        return eatingDecisionId;
    }

    public void setEatingDecisionId(long eatingDecisionId) {
        this.eatingDecisionId = eatingDecisionId;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(UserActionImage.COLUMN_NAME_USER_ACTION_ID, getUserActionId());
        values.put(UserActionImage.COLUMN_NAME_CREATED_TIME, getCreatedTime().getTime());
        values.put(UserActionImage.COLUMN_NAME_SERVER_IMAGE_ID, getServerImageId());
        values.put(UserActionImage.COLUMN_NAME_RATING, getRating());
        values.put(UserActionImage.COLUMN_NAME_EATING_DECISION_ID, getEatingDecisionId());
        return values;
    }

    public boolean isSynchronised() {
        return synchronised;
    }

    public void setSynchronised(boolean synchronised) {
        this.synchronised = synchronised;
    }
}
