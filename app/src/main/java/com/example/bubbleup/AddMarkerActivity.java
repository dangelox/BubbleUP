package com.example.bubbleup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



//This Class should also upload the composed bubble to the database.
public class AddMarkerActivity extends AppCompatActivity {

    String snippet = "";

    String token;

    String url_tasks ="https://bubbleup-api.herokuapp.com/posts";

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        queue = Volley.newRequestQueue(getApplicationContext());

        //no longer a feature
        //final SeekBar colorBar = (SeekBar) findViewById(R.id.seekBar);

        final LatLng latlng = (LatLng) getIntent().getParcelableExtra("location");

        token =  getIntent().getStringExtra("myToken");

        //getting user input title
        final EditText userTitle = (EditText) findViewById(R.id.editTitle);

        final EditText userSnippet = (EditText) findViewById(R.id.editSnippet);

        Button markerButton = (Button) findViewById(R.id.createMarkerButton);
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String tittle = "";

                BubbleMarker myBubble = new BubbleMarker(latlng, "", "","", 320, 320, getApplicationContext());//Draws a bubble near lawrence

                if(userSnippet.getText() != null && userSnippet.getText().length() > 0){
                    myBubble.bubbleMarkerOption.snippet(userSnippet.getText().toString());
                    myBubble.msg = userSnippet.getText().toString();
                    snippet = userSnippet.getText().toString();
                }else{
                    Toast.makeText(getApplicationContext(), "Empty post.", Toast.LENGTH_LONG).show();
                    return;
                }
                //set the title to the input from user
                if (userTitle.getText() != null) {
                    myBubble.bubbleMarkerOption.title(userTitle.getText().toString());
                    myBubble.tittle = userTitle.getText().toString();
                    tittle = userTitle.getText().toString();
                }

                //Adding content to the database.
                StringRequest bubblePostRequest = new StringRequest(Request.Method.POST, url_tasks,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    Log.d("BubbleUp", "Post Taks Response:\n" + response.toString());
                                    JSONObject json_response = new JSONObject(response.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("BubbleUp","Post Task Error: " + error.toString());
                        Log.d("BubbleUp","Post Task Error: " + error.getMessage());
                        //Log.d("BubbleUp","With Token: " + token);
                    }
                }){
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("body", "\"" + snippet + "\"");
                        params.put("visible", "1");
                        params.put("lat", Double.toString( latlng.latitude));
                        params.put("lng", Double.toString( latlng.longitude));
                        return params;
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String httpPostBody="{\"body\": \""+snippet+"\", " +
                                "\"visible\": 1," +
                                " \"lat\": " + Double.toString( latlng.latitude) + "," +
                                " \"lng\": " + Double.toString( latlng.longitude) + "}";
                        Log.d("BubbleUp", "HTTPPOST BODY:\n" + httpPostBody);
                        return httpPostBody.getBytes();
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap();
                        Log.d("BubbleUp",token);
                        params.put("Authorization","JWT " + token);
                        params.put("Content-Type","application/json");
                        return params;
                    }
                };
                queue.add(bubblePostRequest);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("latlng",latlng);
                resultIntent.putExtra("snipet", snippet);
                resultIntent.putExtra("tittle", tittle);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
