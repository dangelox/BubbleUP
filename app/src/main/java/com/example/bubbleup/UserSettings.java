package com.example.bubbleup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

public class UserSettings extends AppCompatActivity {

    Button set_username;
    TextView username_text;

    Button set_profpic_link;
    TextView profpic_link;

    RequestQueue queue;

    String newLink;

    String url_set_profile_pic = "https://bubbleup-api.herokuapp.com/user/image";

    String saved_token;

    public static final String TOKEN_PREF = "user_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        set_username = (Button) findViewById(R.id.button_set_username);
        username_text = (TextView) findViewById(R.id.edit_username);

        set_profpic_link = (Button) findViewById(R.id.button_profile_picture_link);
        profpic_link = (TextView) findViewById(R.id.text_profile_picture_link);

        queue = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
        saved_token = settings.getString("saved_token", null);

        set_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        set_profpic_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newLink = profpic_link.getText().toString();

                if(newLink == null || newLink.equals("")){
                    Toast.makeText(UserSettings.this, "Empty Link!", Toast.LENGTH_SHORT).show();
                }else{
                    StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_profile_pic,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(UserSettings.this, "Successful! Link Set!", Toast.LENGTH_SHORT).show();
                                    Log.d("BubbleUp","PUT Request Successful");
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("BubbleUp",error.toString());
                            Toast.makeText(UserSettings.this, "Communication Error!", Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            String httpPostBody="{\"profile_image\": \"" + newLink + "\"}";
                            return httpPostBody.getBytes();
                        }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap();
                            params.put("Content-Type","application/json");
                            params.put("Authorization","JWT " + saved_token);
                            return params;
                        }
                    };

                    Log.d("BubbleUp","Requesting Link Set");
                    queue.add(loginRequest);
                }
            }

        });
    }
}
