package com.aqib.icrave.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aqib on 22/01/14.
 */
public class ImagesDataSource {

    private SQLiteDatabase db;
    private DatabaseHandler dbHandler;
    private Context context;

    public ImagesDataSource(Context context) {
        dbHandler = DatabaseHandler.getDatabaseHandlder(context);
        this.context = context;
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
     * Get the total number of images in the database
     *
     * @return a count of images
     */
    public long getRowCount() {
        Cursor c = db.rawQuery(String.format("SELECT count(%s) FROM %s", Image.COLUMN_NAME_ID, Image.TABLE_NAME), null);
        c.moveToNext();
        return c.getLong(0);
    }

    /**
     * Get the next image
     *
     * @return ID as long
     */
    public Image getImageById(long id) {
        Cursor c = db.query(Image.TABLE_NAME, new String[]{Image.COLUMN_NAME_SERVER_ID, Image.COLUMN_NAME_TITLE}, "_id=?", new String[]{id + ""}, null, null, null, "1");
        c.moveToFirst();
        return new Image(id, c.getLong(0), c.getString(1));
    }

    public static List<Image> getAllImagesFromServer(String url) throws IOException, ParseException {
        //RESTful request
        HttpClient client = MySSLSocketFactory.getNewHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String json = rd.readLine();

        //Parse JSON object and get all the image titles
        JSONParser parser = new JSONParser();

        Log.d("ImageDataSource", String.format("JSON = %s", json));
        Log.d("ImageDataSource", String.format("parser = %s", parser.toString()));
        Object obj = parser.parse(json);

        JSONArray arrImages = (JSONArray) obj;

        Iterator<JSONObject> iterator = arrImages.iterator();
        List<Image> images = new ArrayList<Image>();
        int id = 1;
        while (iterator.hasNext()) {
            JSONObject image = iterator.next();
            images.add(new Image(id++, (Long) image.get("id"), (String) image.get("title")));
        }

        return images;
    }

}
