package com.aqib.icrave.model;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by aqib on 19/01/14.
 */
public class UserAction {

    public static final String TABLE_NAME = "user_actions";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_CREATED_TIME = "created_time";
    public static final String COLUMN_NAME_UNDO_TIME = "undo_time";
    public static final String COLUMN_NAME_ACTIVE = "active";
    public static final String COLUMN_NAME_SYNCHRONISED = "synchronised";
    public static final String[] ALL_COLUMNS = new String[]{COLUMN_NAME_ID, COLUMN_NAME_CREATED_TIME, COLUMN_NAME_UNDO_TIME, COLUMN_NAME_ACTIVE, COLUMN_NAME_SYNCHRONISED};

    public static final String CREATE_USER_ACTION_TABLE = MessageFormat.format(
            "CREATE TABLE IF NOT EXISTS {0} (" +
                    "{1} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "{2} TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "{3} TIMESTAMP, " +
                    "{4} BOOLEAN DEFAULT TRUE, " +
                    "{5} BOOLEAN DEFAULT FALSE" +
                    ")",
            TABLE_NAME, COLUMN_NAME_ID, COLUMN_NAME_CREATED_TIME, COLUMN_NAME_UNDO_TIME, COLUMN_NAME_ACTIVE, COLUMN_NAME_SYNCHRONISED
    );

    private long id;
    private Date createdTime;
    private Date undoTime;
    private boolean active;
    private boolean synchronised;

    public UserAction() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUndoTime() {
        return undoTime;
    }

    public void setUndoTime(Date undoTime) {
        this.undoTime = undoTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSynchronised() {
        return synchronised;
    }

    public void setSynchronised(boolean synchronised) {
        this.synchronised = synchronised;
    }
}
