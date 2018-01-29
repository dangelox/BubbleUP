package com.example.bubbleup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Button bt_free_view;
    Button bt_login;
    Button bt_register;
    TextView mTextView;

    StringRequest login_request;
    StringRequest register_request;

    JSONObject jsonLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text_greet);

        bt_free_view = (Button) findViewById(R.id.button_freeview);
        bt_login =  (Button) findViewById(R.id.button_login);
        bt_register = (Button) findViewById(R.id.button_register);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        //String url ="https://bubbleup-api.herokuapp.com/token";

        String url ="http://www.google.com";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.substring(0,200));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

        queue.add(stringRequest);

        bt_free_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(myIntent);
            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = ((TextView) findViewById(R.id.text_username)).getText().toString();
                String pass = ((TextView) findViewById(R.id.text_password)).getText().toString();

                Log.d("BubbleUp","Login Attempt.");

                if (!checkFields(user, pass))
                    return;

            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = ((TextView) findViewById(R.id.text_username)).getText().toString();
                String pass = ((TextView) findViewById(R.id.text_password)).getText().toString();

                Log.d("BubbleUp","Register Attempt.");


                if (!checkFields(user, pass))
                    return;
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
}
