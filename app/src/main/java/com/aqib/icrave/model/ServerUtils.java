package com.aqib.icrave.model;

import android.content.Context;
import android.os.AsyncTask;

import com.aqib.icrave.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * Created by aqib on 12/04/14.
 */
public class ServerUtils {

    /**
     * Makes a HTTP get request to the server with the provided URL
     * @param url The url to request
     * @return String representing server response or NULL if an error occurred
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String get(String url) throws ExecutionException, InterruptedException {
        AsyncTask<String, Integer, String> request = new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... urls) {
                String strResponse = null;
                try {
                    strResponse = request(urls[0].toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return strResponse;
            }
        };

        request.execute(url);
        return request.get();
    }

    private static String request(String url) throws IOException {
        //RESTful request
        HttpClient client = MySSLSocketFactory.getNewHttpClient();
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        return rd.readLine();
    }

    public static String getBaseUrl(Context context, String endpoint) {
        String address = context.getString(R.string.server_address);
        String apiKeyParam = context.getString(R.string.server_rest_param_api_key);
        String apiKey = context.getString(R.string.server_api_key);

        return String.format("%s%s?%s=%s", address, endpoint, apiKeyParam, apiKey);
    }

}
