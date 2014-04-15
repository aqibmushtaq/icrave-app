package com.aqib.icrave.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aqib.icrave.R;
import com.aqib.icrave.model.FileLoader;
import com.aqib.icrave.model.Login;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends ActionBarActivity {

    public static final String PREF_FILE_NAME = "login";
    public static final String PREF_USER_ID_NAME = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment())
                    .commit();
        }
    }

    public static class LoginFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);

            ((TextView)rootView.findViewById(R.id.password_instructions)).setText(FileLoader.readRawTextFile(getActivity().getApplicationContext(), R.raw.registration_instructions));

            //button listeners
            setRegisterListener(rootView);
            setLoginListener(rootView);

            return rootView;
        }

        /**
         * Validate the login/register input fields
         * @param email Email address entered
         * @param password Password entered
         * @param errorToast The toast object used to display messages
         * @return True if the input fields are valid, false otherwise
         */
        private boolean validate(String email, String password, Toast errorToast) {
            if (email.equals("")) {
                errorToast.setText(R.string.error_no_email);
                errorToast.show();
                return false;
            } else if (!email.matches(Login.REGEX_EMAIL_FORMAT)) {
                errorToast.setText(R.string.error_wrong_email_format);
                errorToast.show();
                return false;
            } else if (password.equals("")) {
                errorToast.setText(R.string.error_no_password);
                errorToast.show();
                return false;
            } else if (!password.matches(Login.FORMAT_PASSWORD_FORMAT)) {
                errorToast.setText(R.string.error_wrong_password_format);
                errorToast.show();
                return false;
            } else
                return true;
        }

        private void setLoginListener(View rootView) {
            final Toast errorToast = Toast.makeText(getActivity().getApplicationContext(), "", Toast.LENGTH_SHORT);

            rootView.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity a = getActivity();
                    String email = ((EditText)a.findViewById(R.id.email)).getText().toString().toLowerCase();
                    String password = ((EditText)a.findViewById(R.id.password)).getText().toString();


                    if (validate(email, password, errorToast)) {
                        //successfully passed pre-request requirements
                        int result = Login.REQUEST_UNSUCCESSFUL;
                        try {
                            //attempt to authenticate the user
                            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            result = Login.login(getActivity().getApplicationContext(), email, password, editor);
                            Log.d("LoginActivity", String.format("Login result = %s", result));
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (result == Login.LOGIN_SUCCESSFUL) {
                            errorToast.setText(R.string.msg_login_successful);

                            //restart the application to refresh the main menu
                            Intent i = getActivity().getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else if (result == Login.LOGIN_UNSUCCESSFUL)
                            errorToast.setText(R.string.msg_login_unsuccessful);
                        else
                            errorToast.setText(R.string.error_server_not_available);

                        errorToast.show();
                    }
                }
            });
        }

        private void register() {
            final Toast errorToast = Toast.makeText(getActivity().getApplicationContext(), "", Toast.LENGTH_SHORT);

            Activity a = getActivity();
            final String email = ((EditText)a.findViewById(R.id.email)).getText().toString().toLowerCase();
            final String password = ((EditText)a.findViewById(R.id.password)).getText().toString();

            if (validate(email, password, errorToast)) {
                //successfully passed pre-request requirements

                //request access code
                final EditText accessCode = new EditText(getActivity().getApplicationContext());
                accessCode.setInputType(InputType.TYPE_CLASS_NUMBER);
                accessCode.setTextColor(Color.BLACK);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Update Status")
                        .setMessage("Access code")
                        .setView(accessCode)
                        .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String strAccessCode = ((Editable) accessCode.getText()).toString();

                                try {
                                    if (Integer.parseInt(strAccessCode) == getResources().getInteger(R.integer.registration_access_code)) {
                                        processRegistration(email, password, errorToast);
                                        return;
                                    }
                                } catch (NumberFormatException e) { }

                                //only reaches this point of an exception was thrown
                                errorToast.setText(R.string.msg_register_access_code_incorrect);
                                errorToast.show();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
            }
        }

        private void processRegistration(String email, String password, Toast errorToast) {
            int result = Login.REQUEST_UNSUCCESSFUL;
            try {
                //attempt to register the user
                SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                result = Login.register(getActivity().getApplicationContext(), email, password, editor);
                Log.d("LoginActivity", String.format("Register result = %s", result));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (result == Login.REGISTER_SUCCESSFUL) {
                errorToast.setText(R.string.msg_register_successful);

                //restart the application to refresh the main menu
                Intent i = getActivity().getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } else if (result == Login.REGISTER_UNSUCCESSFUL)
                errorToast.setText(R.string.msg_register_unsuccessful);
            else if (result == Login.REGISTER_EMAIL_EXISTS)
                errorToast.setText(R.string.msg_register_email_taken);
            else
                errorToast.setText(R.string.error_server_not_available);

            errorToast.show();
        }

        private void setRegisterListener(View rootView) {
            rootView.findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    register();
                }
            });
        }
    }
}