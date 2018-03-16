package com.example.bubbleup;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSettings extends AppCompatActivity{

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

    String url_posts = "https://bubbleup-api.herokuapp.com/posts/";
    String url_links_by_ids ="https://bubbleup-api.herokuapp.com/user/image/byids/";

    HashMap<Integer, String> profileNameStorage;
    HashMap<Integer, Bitmap> profilePictureStorageBitmap;

    String token;

    Integer myId;

    List<BubbleMarker> myBubbles;

    ArrayList<Integer> user_id_list;

    boolean log_status = false;

    public static final String TOKEN_PREF = "user_token";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

        //Create a request queue
        queue = Volley.newRequestQueue(getApplicationContext());


        //Array for storing user ID that have been returned from the query
        user_id_list = new ArrayList();

        //make a request for getting all users posts
        StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_posts + myId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("BubbleUp", "JSOn Post Get Response Successful");
                        Log.d("BubbleUp", response);

                        try {
                            //We convert the response into an JSONArray object so as to iterate through the posts.
                            JSONArray json_response = new JSONArray(response.toString());

                            myBubbles.clear();//empty the array first.

                            //Iterating through the JSON object array.
                            for (int i = 0; i < json_response.length(); i++)
                                jsonToBubbleMarker((JSONObject) json_response.get(i), true);

                            //After finishing the post querying we proceed to request the user names and profile pictures link for the user in the user array.

                            Log.d("BubbleUp", "JSON Requesting ID Links");

                            //Building request URL
                            String url_links_ids = url_links_by_ids;
                            boolean c = true;
                            for (int id : user_id_list) {
                                if (c) {
                                    url_links_ids = url_links_ids.concat(Integer.toString(id));
                                    c = false;
                                } else {
                                    url_links_ids = url_links_ids.concat("," + Integer.toString(id));
                                }
                            }

                            StringRequest ids_profile_link_request = new StringRequest(Request.Method.GET, url_links_ids,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                //We get back an array with data for the requested IDs
                                                JSONArray json_response = new JSONArray(response);
                                                //Iterate through the array
                                                for (int i = 0; i < json_response.length(); i++) {
                                                    JSONObject myJson = (JSONObject) json_response.get(i);
                                                    String link = myJson.getString("profile_image");
                                                    String username = myJson.getString("name");
                                                    Integer id = myJson.getInt("id");

                                                    //Storing the ID on a table with his corresponding username
                                                    profileNameStorage.put(id, username);

                                                    //We proceed to use the internet link to try fetch the user's profile picture
                                                    fetchImageAsync2 imageFetch = new fetchImageAsync2();
                                                    imageFetch.execute(Pair.create(id, link));
                                                }
                                            } catch (JSONException e) {
                                                Log.d("BubbleUp", "JSON IDs GET problem!");
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("BubbleUp", "ID get JSOn Response Error! " + error.toString());
                                }
                            }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> headers = new HashMap();
                                    headers.put("Authorization", "JWT " + token);
                                    return headers;
                                }
                            };

                            queue.add(ids_profile_link_request);
                        } catch (JSONException e) {
                            Log.d("BubbleUp", "JSON object problem!");
                            Toast.makeText(getApplicationContext(), "JSON Error", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BubbleUp", " : Bubble Loader Response Error! " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap();
                params.put("Authorization", "JWT " + token);
                return params;
            }
        };
        queue.add(tokenRequest);





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

    public void jsonToBubbleMarker(JSONObject myJson, boolean react){
        int user_id;
        int post_id;
        int likeCount;
        int commentCount;
        String body;
        boolean visible = false;
        int type;
        double lat;
        double lng;
        String date;

        try {
            post_id = Integer.parseInt(myJson.get("id").toString());
            user_id = Integer.parseInt(myJson.get("user_id").toString());
            body = myJson.get("body").toString();
            likeCount = Integer.parseInt(myJson.get("like").toString());
            commentCount = Integer.parseInt(myJson.get("comment_count").toString());
            type = Integer.parseInt(myJson.get("content_type").toString());

            String date_str = myJson.get("created_at").toString().substring(5, 10);
            Integer year = Integer.parseInt(myJson.get("created_at").toString().substring(0, 4));
            Integer month = Integer.parseInt(date_str.substring(0, 2));
            Integer day = Integer.parseInt(date_str.substring(3, 5));
            String time_str = myJson.get("created_at").toString().substring(11, 16);
            Integer hour = Integer.parseInt(time_str.substring(0, 2));
            Integer minute = Integer.parseInt(time_str.substring(3, 5));

            //User reaction (like etc)
            int reaction;
            //When Posting post doesnt return "i_like"
            if(react)
                reaction = myJson.getInt("i_like");
            else
                reaction = 0;

            //We check if the user of the current post is already on our user array.
            if (user_id_list != null && !user_id_list.contains(user_id)) {
                user_id_list.add(user_id);
                Log.d("BubbleUp", "Added User to List " + user_id);
            }

            //Calculating Post Times.

            DateTime bubbleTime = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
            DateTime currentTime = new DateTime();//Local Date Time

            int dayDiff = Days.daysBetween(bubbleTime, currentTime).getDays();
            int hourDiff = Hours.hoursBetween(bubbleTime, currentTime).getHours();
            int minDiff = Minutes.minutesBetween(bubbleTime, currentTime).getMinutes();

            double size_calc = (120 + likeCount * 20) * Math.pow(0.65, minDiff / (1440.0 + (likeCount * 200))) + 70;
            int size = (int) size_calc;

            date = date_str + " " + time_str;

            //Parsing bubble coordinates.

            visible = Boolean.parseBoolean(myJson.get("visible").toString());
            lat = Double.parseDouble(myJson.get("lat").toString());
            lng = Double.parseDouble(myJson.get("lng").toString());

            //Creating the bubble marker objects.
            BubbleMarker newBubble = new BubbleMarker(new LatLng(lat, lng), user_id, reaction, likeCount, commentCount, type, post_id, body + " #" + post_id, "#" + user_id + " " + date, "", size, size, minDiff, hourDiff, dayDiff, getApplicationContext(), null);

            //Adding the bubble to the array so as to iteratively update their status.
            myBubbles.add(newBubble);

        }catch (JSONException e) {
            Log.d("BubbleUp", "Failure While Converting JSON to Bubble");
            Toast.makeText(getApplicationContext(), "JSON Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //This method tries to fetch a image from the internet given it recives a valid URL.
    private class fetchImageAsync2 extends AsyncTask<Pair<Integer,String>, Void, Pair<Integer,Bitmap>> {
        @Override
        protected Pair<Integer,Bitmap> doInBackground(Pair<Integer,String>... params) {
            int usrId = params[0].first;
            String url = params[0].second;

            Bitmap profile_image;
            try {
                Log.d("BubbleUp", "Trying profile picture fetch. From: " + url);
                profile_image = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                Log.d("BubbleUp", "Success profile picture fetch. From: " + url);
            } catch (Exception e) {
                Log.d("BubbleUp", "Profile picture fetch failed. " + e.toString());
                profile_image = null;
            }

            return Pair.create(usrId, profile_image);
        }

        @Override
        protected void onPostExecute(Pair<Integer,Bitmap> usr_id_image) {
            if(usr_id_image.second != null) {
                //If an image has been fetched successfully then we store it on a table using the user ID as the key.
                profilePictureStorageBitmap.put(usr_id_image.first,usr_id_image.second);
            }
        }
    }
}

