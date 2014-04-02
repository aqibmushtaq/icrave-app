package com.aqib.icrave.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.aqib.icrave.R;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by aqib on 22/01/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 17;
    private static final String DATABASE_NAME = "user_local";

    private static DatabaseHandler handler;

    private Context context;

    public static synchronized DatabaseHandler getDatabaseHandlder(Context context) {
        if (handler == null)
            handler = new DatabaseHandler(context);
        return handler;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DatabaseHandler", "Creating database...");
        sqLiteDatabase.execSQL(UserAction.CREATE_USER_ACTION_TABLE);
        sqLiteDatabase.execSQL(UserActionImage.CREATE_USER_ACTION_IMAGE_TABLE);
        sqLiteDatabase.execSQL(Image.CREATE_IMAGES_TABLE);

        //download images from server
        AsyncTask<String, Integer, List<Image>> downloadImages = new AsyncTask<String, Integer, List<Image>>() {
            @Override
            protected List<Image> doInBackground(String... urls) {
                try {
                    List<Image> images = ImagesDataSource.getAllImagesFromServer(urls[0].toString());
                    Log.d("DatabaseHandler", String.format("Downloaded %s images", images.size()));
                    return images;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.d("DatabaseHandler", "Returning null images");
                return null;
            }
        };

        String address = context.getString(R.string.server_address);
        String endpoint = context.getString(R.string.server_rest_url_images_all);
        String apiKeyParam = context.getString(R.string.server_rest_param_api_key);
        String apiKey = context.getString(R.string.server_api_key);
        downloadImages.execute(String.format("%s%s?%s=%s", address, endpoint, apiKeyParam, apiKey));

        //insert the downloaded images into the database
        try {
            List<Image> images = downloadImages.get();
            for (Image image : images) {
                ContentValues values = new ContentValues();
                values.put(Image.COLUMN_NAME_ID, image.getId());
                values.put(Image.COLUMN_NAME_SERVER_ID, image.getServerId());
                values.put(Image.COLUMN_NAME_TITLE, image.getTitle());
                sqLiteDatabase.insert(Image.TABLE_NAME, null, values);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.d("DatabaseHandler", "Upgrading database...");
        //TODO complete implementation
        // (1) extract data

        // (2) drop table
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", UserAction.TABLE_NAME));
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", UserActionImage.TABLE_NAME));
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s", Image.TABLE_NAME));

        // (3) recreate table
        onCreate(sqLiteDatabase);

        // (4) restore data
    }
}
