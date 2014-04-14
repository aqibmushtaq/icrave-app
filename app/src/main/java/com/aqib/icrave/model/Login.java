package com.aqib.icrave.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.aqib.icrave.R;
import com.aqib.icrave.controller.LoginActivity;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.concurrent.ExecutionException;

/**
 * Created by aqib on 11/04/14.
 */
public class Login {

    public static final int REQUEST_UNSUCCESSFUL = -100;

    public static final int LOGIN_UNSUCCESSFUL = 0;
    public static final int LOGIN_SUCCESSFUL = 1;

    public static final int REGISTER_EMAIL_EXISTS = -1;
    public static final int REGISTER_UNSUCCESSFUL = 0;
    public static final int REGISTER_SUCCESSFUL = 1;

    public static final String REGEX_EMAIL_FORMAT = "([A-Za-z0-9_\\.-]+)@([\\dA-Za-z\\.-]+)\\.([A-Za-z\\.]{2,6})";
    public static final String FORMAT_PASSWORD_FORMAT = "[A-Za-z0-9_-]{6,30}";

    /**
     * Authenticate the user and store the user's ID in the passed in SharedPreferences editor if the credentials are valid
     * @param context
     * @param email User's email address
     * @param password User's password
     * @param editor
     * @return LOGIN_UNSUCCESSFUL, LOGIN_SUCCESSFUL or REQUEST_UNSUCCESSFUL
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static int login(Context context, String email, String password, SharedPreferences.Editor editor) throws ExecutionException, InterruptedException {
        String url = buildLoginUrl(context, email, password);
        String strResponse = ServerUtils.get(url);

        if (strResponse == null)
            return REQUEST_UNSUCCESSFUL;

        //Parse JSON object and get the result
        JSONParser parser = new JSONParser();
        JSONObject response;
        try {
            response = ((JSONObject) parser.parse(strResponse));
        } catch (ParseException e) {
            e.printStackTrace();
            return REQUEST_UNSUCCESSFUL;
        }

        int result = ((Long)response.get("result")).intValue();

        //store the credentials if the login attempt was successful
        if (result == Login.LOGIN_SUCCESSFUL) {
            long userId = ((Long)response.get("id"));
            editor.putLong(LoginActivity.PREF_USER_ID_NAME, userId);
            editor.commit();
        }

        return result;
    }

    /**
     * Register the user and store the user's ID in the passed in SharedPreferences editor if the registration was successful
     * @param context
     * @param email User's email address
     * @param password User's password
     * @param editor
     * @return REGISTER_EMAIL_EXISTS, REGISTER_UNSUCCESSFUL, REGISTER_SUCCESSFUL or REQUEST_UNSUCCESSFUL
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static int register(Context context, String email, String password, SharedPreferences.Editor editor) throws ExecutionException, InterruptedException {
        String url = buildRegisterUrl(context, email, password);
        String strResponse = ServerUtils.get(url);

        if (strResponse == null)
            return REQUEST_UNSUCCESSFUL;

        //Parse JSON object and get the result
        JSONParser parser = new JSONParser();
        JSONObject response;
        try {
            response = ((JSONObject) parser.parse(strResponse));
        } catch (ParseException e) {
            e.printStackTrace();
            return REQUEST_UNSUCCESSFUL;
        }

        int result = ((Long)response.get("result")).intValue();

        //store the credentials if the registration attempt was successful
        if (result == Login.REGISTER_SUCCESSFUL) {
            long userId = ((Long)response.get("id"));
            editor.putLong(LoginActivity.PREF_USER_ID_NAME, userId);
            editor.commit();
        }

        return result;
    }

    private static String buildLoginUrl(Context context, String email, String password) {
        String endpoint = context.getString(R.string.server_rest_url_users_login);
        return String.format("%s&%s=%s&%s=%s",
                ServerUtils.getBaseUrl(context, endpoint),
                context.getString(R.string.server_rest_param_email),
                email,
                context.getString(R.string.server_rest_param_password),
                password
        );
    }

    private static String buildRegisterUrl(Context context, String email, String password) {
        String endpoint = context.getString(R.string.server_rest_url_users_register);
        return String.format("%s&%s=%s&%s=%s",
                ServerUtils.getBaseUrl(context, endpoint),
                context.getString(R.string.server_rest_param_email),
                email,
                context.getString(R.string.server_rest_param_password),
                password
        );
    }

    public static boolean isLoggedIn(Activity activity) {
        return getUserId(activity) != -1;

    }

    public static long getUserId(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(LoginActivity.PREF_FILE_NAME, Activity.MODE_PRIVATE);
        return prefs.getLong(LoginActivity.PREF_USER_ID_NAME, -1);
    }
}
