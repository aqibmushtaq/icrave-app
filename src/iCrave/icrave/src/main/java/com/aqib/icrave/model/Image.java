package com.aqib.icrave.model;

import java.text.MessageFormat;

/**
 * Created by aqib on 22/01/14.
 */
public class Image {

    public static final String TABLE_NAME = "images";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_SERVER_ID = "server_id";
    public static final String COLUMN_NAME_TITLE = "title";

    public static final String CREATE_IMAGES_TABLE = MessageFormat.format(
            "CREATE TABLE IF NOT EXISTS {0} (" +
                    "{1} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "{2} INTEGER, " +
                    "{3} VARCHAR" +
                    ")",
            TABLE_NAME, COLUMN_NAME_ID, COLUMN_NAME_SERVER_ID, COLUMN_NAME_TITLE
    );

    private long id;
    private long serverId;
    private String title;

    public Image(long id, long serverId, String title) {
        this.id = id;
        this.serverId = serverId;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long server_id) {
        this.serverId = server_id;
    }
}
