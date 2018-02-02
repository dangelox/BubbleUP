package com.example.bubbleup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, BlankFragment.OnFragmentInteractionListener {

    public static final String SAVEDLOCATION_PREF = "previous_location";

    int logout = 0;
    boolean log_status = false;

    String url_posts ="https://bubbleup-api.herokuapp.com/posts";

    String token;

    private GoogleMap mMap;

    //List where we will store all bubbles, may wanna use a different data structure in the future.
    //List<GroundOverlay> myBubbles;

    List<BubbleMarker> myBubbles;

    Marker myMarker;

    //Handler
    private Handler mHandler;

    //To pass the intent to switch case later
    private static final int addMarkerIntent = 1;

    private boolean mLocationPermissionGranted;

    //Permission key.
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123;

    //Fragment Manager global?
    FragmentManager fragmentManager = getSupportFragmentManager();

    //Content Fragment
    public BlankFragment myFragment = new BlankFragment();
    boolean fragment_display;

    //Map Fragment, make local or global? Check Transitions guides.
    SupportMapFragment mapFragment;

    //Buttons
    Button content_button;

    double saved_lat;
    double saved_lng;
    int saved_zoom;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences saved_settings = getSharedPreferences(SAVEDLOCATION_PREF, 0);
        saved_lat = Double.longBitsToDouble(saved_settings.getLong("saved_lat",0));
        saved_lng = Double.longBitsToDouble(saved_settings.getLong("saved_lng",0));
        saved_zoom = saved_settings.getInt("saved_zoom",0);

        log_status = getIntent().getBooleanExtra("log_status",false);

        if(log_status) {
            Log.d("BubbleUp","log_status = true");
            token = getIntent().getStringExtra("myToken");
            Log.d("BubbleUp","Token = " + token);
        }

        setContentView(R.layout.activity_maps);

        //Call a method that loads bubbles in to a structure.

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        fragment_display = false;

        mHandler = new Handler();//Create handler

        //Buttons
        content_button = (Button) findViewById(R.id.content_show);

        content_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if(fragment_display){
                    fragmentTransaction.remove(myFragment);
                    fragmentTransaction.commit();
                    fragment_display = false;
                    Log.d("BubbleUp","Hiding content.");
                }else{
                    fragmentTransaction.add(R.id.zone, myFragment);
                    fragmentTransaction.commit();
                    fragment_display = true;
                    Log.d("BubbleUp","Showing content.");
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mMap.setMyLocationEnabled(true);//Silenced warning.
                }
            }
        }
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void wobbleBubbles (List<BubbleMarker> bubbles){
        //iterate over the list, uses for each syntax, nice Java 8 feature. Can parallelize?
        for (BubbleMarker currentBubble : bubbles) {
            currentBubble.wobble();
        }
    }

    //this should update all bubbles. All bubbles should be added to some sort of array or data structure.
    Runnable bubbleUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                if(!myBubbles.isEmpty()){
                    wobbleBubbles(myBubbles);//Changes bubbles coordinates
                }
            } finally {
                // this code always executes even if try is successful.
                mHandler.postDelayed(bubbleUpdater, 40);//setting update delay to 10 milliseconds.
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(saved_lat,saved_lng),saved_zoom));

        //((ViewGroup) findViewById(R.id.map)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        myBubbles = new ArrayList<>();

        //Permission Check
        getLocationPermission();
        //If permission is granted then create location button.
        if(mLocationPermissionGranted){
            Log.d("BubbleUp","Internet Permission access has already been Granted");
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }

        //BubbleMarker Maker Example
        /*
        BubbleMarker myBubble1 = new BubbleMarker(new LatLng(38.9717, -95.2353), "Deafult Bubble", "Hello World!", 320, 320, getApplicationContext());//Draws a bubble near lawrence
        myBubble1.addMarker(mMap);
        myBubbles.add(myBubble1);
        */

        //BubbleLoader
        bubbleLoader();

        //TODO: Make a bubble creation activity.

        //Initiate bubble updater
        bubbleUpdater.run();//Run the bubble updater.

        //set listener to call addMarkerActivity when a click is registerd on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
            }
        });


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(log_status) {
                    Intent edit = new Intent(MapsActivity.this, AddMarkerActivity.class);
                    edit.putExtra("location", latLng);
                    edit.putExtra("myToken", token);
                    MapsActivity.this.startActivityForResult(edit, addMarkerIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "Log in to post.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void bubbleLoader(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        if(log_status) {
            StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_posts,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String user_id;
                            String post_id;
                            String body;
                            boolean visible = false;
                            double lat;
                            double lng;
                            String date;
                            Log.d("BubbleUp", "JSOn Response: \n" + response.toString());
                            try {
                                JSONArray json_response = new JSONArray(response.toString());

                                myBubbles.clear();//empty the array.

                                for (int i = 0; i < json_response.length(); i++) {

                                    JSONObject myJson = (JSONObject) json_response.get(i);
                                    post_id = myJson.get("id").toString();
                                    user_id = myJson.get("user_id").toString();
                                    body = myJson.get("body").toString();
                                    String date_str = myJson.get("created_at").toString().substring(5,10);
                                    Integer year = Integer.parseInt(myJson.get("created_at").toString().substring(0,4));
                                    Integer month = Integer.parseInt(date_str.substring(0,2));
                                    Integer day = Integer.parseInt(date_str.substring(3,5));
                                    String time_str = myJson.get("created_at").toString().substring(11,16);
                                    Integer hour = Integer.parseInt(time_str.substring(0,2));
                                    Integer minute = Integer.parseInt(time_str.substring(3,5));

                                    DateTime bubbleTime = new DateTime(year,month,day,hour,minute, DateTimeZone.UTC);
                                    DateTime currentTime = new DateTime();//Local Date Time

                                    int dayDiff = Days.daysBetween(bubbleTime, currentTime).getDays();
                                    int minDiff = Minutes.minutesBetween(bubbleTime, currentTime).getMinutes();

                                    //Log.d("BubbleUp",bubbleTime.toString());
                                    //Log.d("BubbleUp",currentTime.toString());

                                    double size_calc = 250 * Math.pow(0.65,minDiff/1440.0) + 100;
                                    int size = (int) size_calc;

                                    Log.d("BubbleUp","Double = " + Double.toString(Math.pow(0.5,minDiff/1440.0)) + ", wtf = " + Double.toString(Math.pow(0.5,0.9)));
                                    Log.d("BubbleUp","post_id = "+ post_id +", dayDiff = " + Integer.toString(dayDiff) + ", minDiff = " + Integer.toString(minDiff) +", CALCULATED SIZE = " + size);

                                    date = date_str +" "+ time_str;

                                    visible = Boolean.parseBoolean(myJson.get("visible").toString());
                                    lat = Double.parseDouble(myJson.get("lat").toString());
                                    lng = Double.parseDouble(myJson.get("lng").toString());

                                    //Check for id so that you don't duplicate bubbles.

                                    BubbleMarker newBubble = new BubbleMarker(new LatLng(lat, lng), body + " #" + post_id, "User#"+user_id+" "+date,"", size, size, getApplicationContext());
                                    newBubble.addMarker(mMap);
                                    myBubbles.add(newBubble);
                                }
                            } catch (JSONException e) {
                                Log.d("BubbleUp", "JSON object problem!");
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("BubbleUp", "Bubble Loader Error! " + error.toString());
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
        }else{
            //TODO: ADD FREE VIEW ROUTE TO HEROKU?

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (addMarkerIntent) : {
                if (resultCode == Activity.RESULT_OK) {
                    bubbleLoader();
                }
                break;
            }
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //Handle Back Button Press.
    @Override
    public void onBackPressed() {
        if (log_status) {
            logout++;
            if(logout >= 3) {
                log_status = false;//Redundant?
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }else
                Toast.makeText(this, "Press back 2 times to logout", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

        SharedPreferences settings = getSharedPreferences(SAVEDLOCATION_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putLong("saved_lat",Double.doubleToRawLongBits(mMap.getMyLocation().getLatitude()));
        editor.putLong("saved_lng",Double.doubleToRawLongBits(mMap.getMyLocation().getLongitude()));
        editor.putInt("saved_zoom",(int) mMap.getCameraPosition().zoom);

        editor.commit();

    }
}
