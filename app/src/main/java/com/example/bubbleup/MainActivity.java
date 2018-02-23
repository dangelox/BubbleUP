package com.example.bubbleup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends AppCompatActivity {

    private static final int log_off = 1;

    Button bt_free_view;
    Button bt_login;
    Button bt_register;
    TextView mTextView;

    RequestQueue queue;

    String url_register ="https://bubbleup-api.herokuapp.com/users";
    String url_login ="https://bubbleup-api.herokuapp.com/token";
    String url_token ="https://bubbleup-api.herokuapp.com/user";

    String user;
    String pass;

    public static final String TOKEN_PREF = "user_token";

    private String saved_token = "";
    private String profile_pic_link;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(getApplicationContext());

        mTextView = (TextView) findViewById(R.id.text_greet);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
        saved_token = settings.getString("saved_token", null);

        if(saved_token != null && saved_token != "" && saved_token.length() != 0){
            //There is a token, send it? and wait for response?
            mTextView.setText(saved_token);
            // String Request for new user registration.
            StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_token,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            int id;
                            String user_name = "";
                            String email = "";
                            try {
                                Log.d("BubbleUp","Token Response:" + response);
                                JSONObject json_response = new JSONObject(response.toString());
                                profile_pic_link = (String) json_response.get("profile_image");
                                id = (int) json_response.get("id");
                                user_name = (String) json_response.get("name");
                                email = (String) json_response.get("email");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mTextView.setText("Response: "+ response.toString());

                            SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("saved_username", user_name);
                            editor.commit();

                            Intent token_success = new Intent(MainActivity.this, MapsActivity.class);
                            token_success.putExtra("myToken", saved_token);
                            token_success.putExtra("log_status", true);
                            token_success.putExtra("profile_link", profile_pic_link);
                            token_success.putExtra("myUsernName", user_name);
                            //startActivity(token_success);
                            MainActivity.this.startActivityForResult(token_success,log_off);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("BubbleUp","Response error:" + error.toString());
                    Log.d("BubbleUp","Response error msg:" + error.getMessage());

                    //com.android.volley.TimeoutError

                    mTextView.setText("Token Authentication Failure! Deleting token.");
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
            tokenRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(tokenRequest);
        }

        bt_free_view = (Button) findViewById(R.id.button_freeview);
        bt_login =  (Button) findViewById(R.id.button_login);
        bt_register = (Button) findViewById(R.id.button_register);

        //Setting behaviour for screen buttons
        bt_free_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                myIntent.putExtra("log_status", false);
                startActivity(myIntent);
            }
        });



        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = ((TextView) findViewById(R.id.text_username)).getText().toString();
                pass = ((TextView) findViewById(R.id.text_password)).getText().toString();

                Log.d("BubbleUp","Login Attempt.");

                if (!checkFields(user, pass))
                    return;

                // String Request for new user registration.
                StringRequest loginRequest = new StringRequest(Request.Method.POST, url_login,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                mTextView.setText("Response: "+ response);
                                Log.d("BubbleUp","Login Success: " + response);

                                String user_name = "";

                                //TODO: Convert this string into a JSOn object and then just extract the token.
                                try {
                                    JSONObject json_response = new JSONObject(response);
                                    saved_token = (String) json_response.get("token");
                                    user_name = (String) json_response.get("name");

                                    //Necessary?
                                    if(json_response.has("profile_image")){
                                        profile_pic_link = json_response.getString("profile_image");
                                    } else {
                                        profile_pic_link = "";
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    saved_token = response;
                                }

                                SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("saved_token", saved_token);
                                editor.putString("saved_username", user_name);
                                editor.commit();


                                Intent login_success = new Intent(MainActivity.this, MapsActivity.class);
                                login_success.putExtra("myToken", saved_token);
                                login_success.putExtra("log_status",true);
                                login_success.putExtra("profile_link", profile_pic_link);
                                login_success.putExtra("myUsernName", user_name);
                                MainActivity.this.startActivityForResult(login_success,log_off);//log_off meaning it expects a log_off result at some point
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText("Could not login!");
                    }
                }){
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String httpPostBody="{\"email\": \"" + user + "\", \"password\": \"" + pass + "\"}";
                        // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
                        return httpPostBody.getBytes();
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap();
                        params.put("Content-Type","application/json");
                        return params;
                    }
                };
                queue.add(loginRequest);
            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = ((TextView) findViewById(R.id.text_username)).getText().toString();
                pass = ((TextView) findViewById(R.id.text_password)).getText().toString();

                Log.d("BubbleUp","Register Attempt.");


                if (!checkFields(user, pass))
                    return;

                // String Request for new user registration.
                StringRequest registerRequest = new StringRequest(Request.Method.POST, url_register,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                mTextView.setText("Response: "+ response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText("Could not register!");
                    }
                }){
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String httpPostBody=
                                "{\"name\": \"newuser\", \"email\": \"" + user + "\", \"password\": \"" + pass + "\"}";
                        // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
                        return httpPostBody.getBytes();
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap();
                        params.put("Content-Type","application/json");
                        return params;
                    }
                };
                queue.add(registerRequest);
            }
        });
    }

    private boolean checkFields(String usr, String psw){
        if(usr.length() == 0){
            Log.d("BubbleUp","No username.");
            Toast.makeText(getApplicationContext(), "No Username", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(psw.length() == 0){
            Log.d("BubbleUp","No password.");
            Toast.makeText(getApplicationContext(), "No Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (log_off) : {
                if( resultCode == Activity.RESULT_OK) {
                    saved_token = "";
                    SharedPreferences settings = getSharedPreferences(TOKEN_PREF, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("saved_token", saved_token);
                    editor.commit();

                    Toast.makeText(getApplicationContext(), "Logged off, token deleted", Toast.LENGTH_SHORT).show();

                    mTextView.setText("Good Bye!");
                }
                break;
            }
        }
    }
}
