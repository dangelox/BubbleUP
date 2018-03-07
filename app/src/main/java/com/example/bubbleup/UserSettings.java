package com.example.bubbleup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UserSettings extends AppCompatActivity {

    Button set_username;
    //TextView username_text;

    Button set_profpic_link;

    TextView display_username;

    ImageButton profpic;

    String background_color;

    RequestQueue queue;

    String newLink;

    String url_set_name = "https://bubbleup-api.herokuapp.com/user/name";
    String url_set_profile_pic = "https://bubbleup-api.herokuapp.com/user/image";

    String saved_token;
    EditText textEdit;

    String newUserName;

    String saved_profile_link;

    SharedPreferences settings;

    SharedPreferences settings2;

    fetchProfImageAsync fetcher;

    public static final String TOKEN_PREF = "user_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        set_username = (Button) findViewById(R.id.button_set_username);
        //username_text = (TextView) findViewById(R.id.edit_username);

        set_profpic_link = (Button) findViewById(R.id.button_profile_picture_link);

        display_username = (TextView) findViewById((R.id.textView));

        profpic = (ImageButton) findViewById(R.id.imageButton2);

        settings = getSharedPreferences(TOKEN_PREF, 0);

        display_username.setText(settings.getString("saved_username", "Could not find"));
        saved_token = settings.getString("saved_token", null);
        saved_profile_link = settings.getString("profile_link", "");

        //The sharedPreferences settigs was taking a different key string than the one in MapsActivity that stored the backGround_color
        settings2 = getSharedPreferences("previous_location", 0);
        background_color = settings2.getString("backGround_Color", "#CC564A");

        findViewById(R.id.ConsBackground).setBackgroundColor(Color.parseColor(background_color));

        fetcher = new fetchProfImageAsync();
        fetcher.execute(saved_profile_link);

        queue = Volley.newRequestQueue(getApplicationContext());

        set_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserSettings.this);

                LinearLayout layout = new LinearLayout(UserSettings.this);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(parms);

                layout.setGravity(Gravity.CLIP_VERTICAL);
                layout.setPadding(2, 2, 2, 2);

                textEdit = new EditText(UserSettings.this);

                layout.addView(textEdit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                alertDialogBuilder.setView(layout);
                alertDialogBuilder.setTitle("Input your new username!");

                // alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setCancelable(false);

                // Setting Negative "Cancel" Button
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                // Setting Positive "OK" Button
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        newUserName = textEdit.getText().toString();

                        //Toast.makeText(getApplicationContext(), newUserName, Toast.LENGTH_SHORT).show();

                        if(!newUserName.equals("")) {

                            StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_name,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Toast.makeText(getApplicationContext(), "Changed Name to: " + newUserName, Toast.LENGTH_SHORT).show();

                                            settings.edit().putString("saved_username", newUserName).commit();

                                            display_username.setText(newUserName);

                                            Log.d("BubbleUp", "Success Username Change: " + response);
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("BubbleUp", "Unsuccessful change");
                                    Toast.makeText(getApplicationContext(), "Unsuccessful Name Change", Toast.LENGTH_SHORT).show();

                                }
                            }) {
                                @Override
                                public byte[] getBody() throws AuthFailureError {
                                    String httpPostBody = "{\"name\": \"" + newUserName + "\"}";
                                    return httpPostBody.getBytes();
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> params = new HashMap();
                                    params.put("Content-Type", "application/json");
                                    params.put("Authorization", "JWT " + saved_token);
                                    return params;
                                }
                            };

                            queue.add(loginRequest);
                        } else {
                            Toast.makeText(getApplicationContext(), "Empty Name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                try {
                    alertDialog.show();
                    Log.d("BubbleUp","Dialog Success");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("BubbleUp","Dialog Fail");
                }
            }
        });

        set_profpic_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserSettings.this);

                LinearLayout layout = new LinearLayout(UserSettings.this);
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(parms);

                layout.setGravity(Gravity.CLIP_VERTICAL);
                layout.setPadding(2, 2, 2, 2);

                textEdit = new EditText(UserSettings.this);

                layout.addView(textEdit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                alertDialogBuilder.setView(layout);
                alertDialogBuilder.setTitle("Input your new profile pic link!");

                // alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setCancelable(false);

                // Setting Negative "Cancel" Button
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                // Setting Positive "OK" Button
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        newLink = textEdit.getText().toString();

                        if (newLink == null || newLink.equals("")) {
                            Toast.makeText(UserSettings.this, "Empty Link!", Toast.LENGTH_SHORT).show();
                        } else {
                            StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_profile_pic,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            settings.edit().putString("profile_link", newLink).commit();

                                            fetchProfImageAsync fetcher_reload = new fetchProfImageAsync();
                                            fetcher_reload.execute(newLink);

                                            Toast.makeText(UserSettings.this, "Successful! Link Set!", Toast.LENGTH_SHORT).show();
                                            Log.d("BubbleUp", "PUT Request Successful");
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("BubbleUp", error.toString());
                                    Toast.makeText(UserSettings.this, "Communication Error!", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                public byte[] getBody() throws AuthFailureError {
                                    String httpPostBody = "{\"profile_image\": \"" + newLink + "\"}";
                                    return httpPostBody.getBytes();
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> params = new HashMap();
                                    params.put("Content-Type", "application/json");
                                    params.put("Authorization", "JWT " + saved_token);
                                    return params;
                                }
                            };

                            Log.d("BubbleUp", "Requesting Link Set");
                            queue.add(loginRequest);
                        }


                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                try {
                    alertDialog.show();
                    Log.d("BubbleUp","Dialog Success");
                } catch (Exception e) {
                    // WindowManager$BadTokenException will be caught and the app would
                    // not display the 'Force Close' message
                    e.printStackTrace();
                    Log.d("BubbleUp","Dialog Fail");
                    Log.d("BubbleUp",e.toString());
                    Log.d("BubbleUp",e.getLocalizedMessage());
                }
            }

        });
    }

    //This method tries to fetch a image from the internet given it recives a valid URL.
    private class fetchProfImageAsync extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];

            Bitmap profile_image;
            try {
                Log.d("BubbleUp", "Trying profile picture fetch. From: " + url);
                profile_image = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                Log.d("BubbleUp", "Success profile picture fetch. From: " + url);
            } catch (Exception e) {
                Log.d("BubbleUp", "Profile picture fetch failed. " + e.toString());
                profile_image = null;
            }

            return profile_image;
        }

        @Override
        protected void onPostExecute(Bitmap usr_image) {
            if(usr_image != null) {
                //If an image has been fetched successfully then we store it on a table using the user ID as the key.
                profpic.setImageBitmap(usr_image);
                profpic.setBackgroundResource(0);
            }
        }
    }
}
