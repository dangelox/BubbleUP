package com.example.bubbleup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    private static final int log_off = 1;

    public static final String TOKEN_PREF = "user_token";

    String url_token ="https://bubbleup-api.herokuapp.com/user";

    RequestQueue queue;

    private String saved_token = "";
    private String profile_pic_link;
    private Integer myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
        saved_token = settings.getString("saved_token", null);

        if(saved_token != null && !saved_token.equals("") && saved_token.length() != 0){
            // String Request for new user registration.
            StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            int id;
                            String user_name = "";
                            String email = "";
                            try {
                                Log.d("BubbleUp","Splash Token Response:" + response);
                                JSONObject json_response = new JSONObject(response.toString());
                                profile_pic_link = (String) json_response.get("profile_image");
                                myId = (Integer) json_response.get("id");
                                user_name = (String) json_response.get("name");
                                email = (String) json_response.get("email");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("saved_username", user_name);
                            editor.putString("profile_link", profile_pic_link);
                            editor.commit();

                            Intent token_success = new Intent(SplashScreen.this, MainActivity.class);
                            token_success.putExtra("myToken", saved_token);
                            token_success.putExtra("log_status", true);
                            token_success.putExtra("myId", myId);
                            token_success.putExtra("profile_link", profile_pic_link);
                            token_success.putExtra("myUsernName", user_name);
                            //startActivity(token_success);
                            startActivity(token_success);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("BubbleUp","Response error:" + error.toString());
                    Log.d("BubbleUp","Response error msg:" + error.getMessage());

                    saved_token = null;

                    SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("saved_token", saved_token);
                    editor.commit();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap();
                    params.put("Authorization", "JWT " + saved_token);
                    return params;
                }
            };

            //In case the first attempt gets timed out
            tokenRequest.setRetryPolicy(new DefaultRetryPolicy(2500,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(tokenRequest);
        } else {
            Intent normalStartup = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(normalStartup);
            finish();
        }
    }
}
