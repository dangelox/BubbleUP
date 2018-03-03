package com.example.bubbleup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.StrictMath.abs;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter ,OnMapReadyCallback, ContentFragment.OnFragmentInteractionListener {

    public static final String SAVEDLOCATION_PREF = "previous_location";

    int logout = 0;
    boolean log_status = false;

    String url_posts ="https://bubbleup-api.herokuapp.com/posts";
    String url_links ="https://bubbleup-api.herokuapp.com/user/image/";


    String token;

    private GoogleMap mMap;

    Bitmap profile_picture;
    String myUserName;

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
    public ContentFragment myFragment = new ContentFragment();
    boolean fragment_display = false;

    //Map Fragment, make local or global? Check Transitions guides.
    SupportMapFragment mapFragment;

    //Integer to hold theme choice
    int curTheme = R.raw.standard_mode; //0 = standard mode, 1 = night mode, 2 = silver mode, 3 = night2 mode, 4 = retro mode, 5 = dark mode

    //Buttons
    ImageButton content_button;
    ImageButton theme_button;
    ImageButton profile_button;
    ImageButton reload_button;


    //Colors
    String backGroundColor;
    String buttonColor;
    String buttonTextColor;

    double saved_lat;
    double saved_lng;
    int saved_zoom;

    RequestQueue queue;

    private String profile_pic_link = "";
    ArrayList<Integer> user_id_list;
    HashMap<Integer, Bitmap> profilePictureStorageBitmap;
    HashMap<Integer, String> profilePictureStorageLink;
    HashMap<Integer, String> profileNameStorage;

    SharedPreferences saved_settings;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saved_settings = getSharedPreferences(SAVEDLOCATION_PREF, 0);
        saved_lat = Double.longBitsToDouble(saved_settings.getLong("saved_lat",0));
        saved_lng = Double.longBitsToDouble(saved_settings.getLong("saved_lng",0));
        saved_zoom = saved_settings.getInt("saved_zoom",0);
        curTheme = saved_settings.getInt("myTheme", R.raw.standard_mode);
        backGroundColor = saved_settings.getString("backGround_Color",null);

        log_status = getIntent().getBooleanExtra("log_status",false);
        myUserName = getIntent().getStringExtra("myUserName");
        profile_pic_link = getIntent().getStringExtra("profile_link");

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

        mHandler = new Handler();//Create handler

        //Buttons
        content_button = (ImageButton) findViewById(R.id.content_show);

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

                    LatLngBounds currentBound = mMap.getProjection().getVisibleRegion().latLngBounds;
                    fragmentTransaction.commitNow();
                    myFragment.sendToFragment(myBubbles, currentBound);
                    fragment_display = true;
                    Log.d("BubbleUp","Showing content.");
                }
            }
        });

        theme_button = (ImageButton) findViewById(R.id.button_theme_change);

        theme_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (curTheme) {
                    case R.raw.standard_mode:
                        curTheme = R.raw.night_mode;
                        backGroundColor = "#4e6d70";//water labels.text.fill
                        buttonColor = "#515c6d";
                        break;
                    case R.raw.night_mode:
                        curTheme = R.raw.night2_mode;
                        backGroundColor = "#17263c";
                        buttonColor = "#515c6d";
                        break;
                    case R.raw.night2_mode:
                        curTheme = R.raw.retro_mode;
                        backGroundColor = "#92998d";
                        buttonColor = "#515c6d";
                        break;
                    case R.raw.retro_mode:
                        curTheme = R.raw.dark_mode;
                        backGroundColor = "#3d3d3d";
                        buttonColor = "#515c6d";
                        break;
                    case R.raw.dark_mode:
                        curTheme = R.raw.standard_mode;
                        backGroundColor = "#f2f2f2";
                        buttonColor = "#515c6d";
                        break;
                }
                if(backGroundColor != null && buttonColor != null) {
                    findViewById(R.id.dashboard).setBackgroundColor(Color.parseColor(backGroundColor));
                    findViewById(R.id.zone).setBackgroundColor(Color.parseColor(backGroundColor));
                    saved_settings.edit().putString("backGround_Color",backGroundColor).apply();
                }
                //theme_button.setBackgroundColor();
                //theme_button.setTextColor();

                //content_button.setBackgroundColor();
                //content_button.setTextColor();

                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getApplicationContext(), curTheme));
                    if (!success) {
                        Log.e("BubbleUp", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("BubbleUP", "Can't find style. Error: ", e);
                }

            }
        });

        profile_button = (ImageButton) findViewById(R.id.button_profile);

        //Use this if we have a user profile image?
        //Bitmap bitMap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_avatar);
        //Bitmap scaledBitMap = Bitmap.createScaledBitmap(bitMap, 90,90, true);
        //profile_button.setImageBitmap(scaledBitMap);

        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile_intent = new Intent(MapsActivity.this, UserSettings.class);
                startActivity(profile_intent);
            }
        });

        reload_button = (ImageButton) findViewById(R.id.button_reload);

        reload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bubbleLoader();
                mMap.clear();
            }
        });

        //Set dashboard background color
        if(backGroundColor != null) {
            findViewById(R.id.dashboard).setBackgroundColor(Color.parseColor(backGroundColor));
            saved_settings.edit().putString("backGround_Color",backGroundColor).apply();
            findViewById(R.id.zone).setBackgroundColor(Color.parseColor(backGroundColor));
        }
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

    public void updateBubbles (List<BubbleMarker> bubbles){
        LatLngBounds currentBound = mMap.getProjection().getVisibleRegion().latLngBounds;//Efficiency?
        //iterate over the list, uses for each syntax, nice Java 8 feature. Can parallelize?
        for (BubbleMarker currentBubble : bubbles) {
            //Checking if the bubbles are visible within the current camera position
            if(currentBound.contains(currentBubble.bubbleMarker.getPosition())) {
                //If visible then we animate the bubble.
                currentBubble.wobble();

                //We check if an image has been loaded for the current bubble.
                if(currentBubble.getProfileImage() == null && profilePictureStorageBitmap.containsKey(currentBubble.myUser_id)){
                    //Updating the bubble image.
                    currentBubble.updateImage(profilePictureStorageBitmap.get(currentBubble.myUser_id));
                }

                //We check if the username has been loaded for the current bubble.
                if(currentBubble.username.equals("") && profileNameStorage.containsKey(currentBubble.myUser_id)){
                    //Updating the bubble username variable.
                    currentBubble.username = profileNameStorage.get(currentBubble.myUser_id);
                    currentBubble.bubbleMarker.setTitle(currentBubble.username + currentBubble.bubbleMarker.getTitle());
                }
            }
        }
    }

    //this should update all bubbles. All bubbles should be added to some sort of array or data structure.
    Runnable bubbleUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                if(!myBubbles.isEmpty()){
                    updateBubbles(myBubbles);//Changes bubbles coordinates
                }
            } finally {
                // this code always executes even if try is successful.
                mHandler.postDelayed(bubbleUpdater, 50);//setting update delay to 10 milliseconds.
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

        mMap.setInfoWindowAdapter(this);

        profilePictureStorageLink = new HashMap<>();

        profileNameStorage = new HashMap<>();

        profilePictureStorageBitmap = new HashMap<>();

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, curTheme));
            if (!success) {
                Log.e("BubbleUp", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("BubbleUP", "Can't find style. Error: ", e);
            curTheme = R.raw.standard_mode;
        }


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
        //Create a request queue
        queue = Volley.newRequestQueue(getApplicationContext());

        //Array for storing user ID that have been returned from the query
        user_id_list = new ArrayList();

        if(log_status) {
            //If the user is logged in then we make a request for getting all users posts
            StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_posts,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //If response is successful we proceed to create bubbles
                            int user_id;
                            int post_id;
                            String body;
                            boolean visible = false;
                            double lat;
                            double lng;
                            String date;
                            Log.d("BubbleUp", "JSOn Post Get Response Successful");
                            Log.d("BubbleUp", response);

                            try {
                                //We convert the response into an JSONArray object so as to iterate through the posts.
                                JSONArray json_response = new JSONArray(response.toString());

                                myBubbles.clear();//empty the array first.

                                //Iterating through the JSON object array.
                                for (int i = 0; i < json_response.length(); i++) {
                                    JSONObject myJson = (JSONObject) json_response.get(i);

                                    //Getting variables from the JSON object

                                    post_id = Integer.parseInt(myJson.get("id").toString());
                                    user_id = Integer.parseInt(myJson.get("user_id").toString());
                                    body = myJson.get("body").toString();
                                    String date_str = myJson.get("created_at").toString().substring(5,10);
                                    Integer year = Integer.parseInt(myJson.get("created_at").toString().substring(0,4));
                                    Integer month = Integer.parseInt(date_str.substring(0,2));
                                    Integer day = Integer.parseInt(date_str.substring(3,5));
                                    String time_str = myJson.get("created_at").toString().substring(11,16);
                                    Integer hour = Integer.parseInt(time_str.substring(0,2));
                                    Integer minute = Integer.parseInt(time_str.substring(3,5));

                                    //User Like
                                    int reaction = myJson.getInt("i_like");

                                    //We check if the user of the current post is already on our user array.
                                    if(user_id_list != null && !user_id_list.contains(user_id)){
                                        user_id_list.add(user_id);
                                        Log.d("BubbleUp","Added User to List " + user_id);
                                    }

                                    //Calculating Post Times.

                                    DateTime bubbleTime = new DateTime(year,month,day,hour,minute, DateTimeZone.UTC);
                                    DateTime currentTime = new DateTime();//Local Date Time

                                    int dayDiff = Days.daysBetween(bubbleTime, currentTime).getDays();
                                    int minDiff = Minutes.minutesBetween(bubbleTime, currentTime).getMinutes();

                                    double size_calc = 180 * Math.pow(0.65,minDiff/1440.0) + 50;
                                    int size = (int) size_calc;

                                    date = date_str +" "+ time_str;

                                    //Parsing bubble coordinates.

                                    visible = Boolean.parseBoolean(myJson.get("visible").toString());
                                    lat = Double.parseDouble(myJson.get("lat").toString());
                                    lng = Double.parseDouble(myJson.get("lng").toString());

                                    //Creating the bubble marker objects.
                                    BubbleMarker newBubble = new BubbleMarker(new LatLng(lat, lng), user_id, reaction , post_id,body + " #" + post_id, "#"+ user_id +" "+date,"", size, size, getApplicationContext(), null);

                                    //Adding the bubble to the google map fragment.
                                    newBubble.addMarker(mMap);

                                    //Adding the bubble to the array so as to iteratively update their status.
                                    myBubbles.add(newBubble);
                                }

                                //After finishing the post querying we proceed to request the user names and profile pictures link for the user in the user array.

                                Log.d("BubbleUp", "JSON Requesting ID Links");

                                //Iterating through the user array
                                //TODO: There is now a better way for requesting user IDs, should change this at some point
                                for (final Integer id : user_id_list) {

                                    Log.d("BubbleUp", "JSON Request for ID #" + id);

                                    //Creating a request for each user
                                    StringRequest id_profile_link_request = new StringRequest(Request.Method.GET, url_links + id,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    //On successful response we store the user ID and assign them their corresponding data

                                                    try {
                                                        JSONObject json_response = new JSONObject(response);
                                                        String link = json_response.getString("profile_image");
                                                        String username = json_response.getString("name");

                                                        //Storing the ID on a table with his corresponding username
                                                        profileNameStorage.put(id,username);

                                                        //We proceed to use the internet link to try fetch the user's profile picture
                                                        fetchImageAsync imageFetch = new fetchImageAsync();
                                                        imageFetch.execute(Pair.create(id, link));

                                                    } catch (JSONException e) {
                                                        Log.d("BubbleUp", "JSON IDs get problem!");
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d("BubbleUp", " : ID get JSOn Response Error! " + error.toString());
                                        }
                                    }) {
                                        @Override
                                        public Map<String,String> getParams(){
                                            Map<String, String> params = new HashMap();
                                            params.put("id","\"" + id.toString() + "\"");
                                            return params;
                                        }
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> headers = new HashMap();
                                            headers.put("Authorization", "JWT " + token);
                                            headers.put("Content-Type","application/json");
                                            return headers;
                                        }
                                    };

                                    queue.add(id_profile_link_request);
                                }


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

    public void moveCamera(LatLng latlng) {
        LatLng currLatLng = mMap.getCameraPosition().target; //containts current camera latlng

        float currZoom = mMap.getCameraPosition().zoom; //contains current camera zoom level

        if ((Math.abs(currLatLng.latitude - latlng.latitude) < .001) && (Math.abs(currLatLng.longitude - latlng.longitude) < .001) && (currZoom < 20.0)) { //compares current latlng to see if we are already centered over the bubble marker we clicked on and to see if we have hit max zoom already
            //We were already hovering over current bubble and wanted to zoom in to get a cleaner look
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, currZoom + 1));
        } else {
            //camera was elsewhere and need to center over bubble without zooming
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, mMap.getCameraPosition().zoom));
        }
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

        editor.putLong("saved_lat",Double.doubleToRawLongBits(mMap.getCameraPosition().target.latitude));
        editor.putLong("saved_lng",Double.doubleToRawLongBits(mMap.getCameraPosition().target.longitude));
        editor.putInt("saved_zoom",(int) mMap.getCameraPosition().zoom);
        editor.putInt("myTheme", curTheme);

        editor.commit();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if( marker.getTag() != null && Patterns.WEB_URL.matcher((String) marker.getTag()).matches()){
            //Toast.makeText(this, "Checking for links.", Toast.LENGTH_SHORT).show();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) marker.getTag()));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "No URL on post.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View windowView = View.inflate(getApplicationContext(),R.layout.bubble_info_window,null);

        TextView postHeader = (TextView) windowView.findViewById(R.id.bubble_info_textViewUserName);

        postHeader.setText(marker.getTitle());

        TextView postContent = (TextView) windowView.findViewById(R.id.bubble_info_textView);

        postContent.setText(marker.getSnippet());

        return windowView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    //This method tries to fetch a image from the internet given it recives a valid URL.
    private class fetchImageAsync extends AsyncTask<Pair<Integer,String>, Void, Pair<Integer,Bitmap>> {
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

    @Override
    protected void onSaveInstanceState(Bundle outState){
        if(fragment_display) {
            content_button.performClick();
        }

        super.onSaveInstanceState(outState);

    }
}
