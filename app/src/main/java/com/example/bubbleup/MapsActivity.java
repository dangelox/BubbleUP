package com.example.bubbleup;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, BlankFragment.OnFragmentInteractionListener {

    private GoogleMap mMap;

    //List where we will store all bubbles, may wanna use a different data structure in the future.
    List<GroundOverlay> myBubbles;

    Marker myMarker;

    //Handler
    private Handler mHandler;

    //To pass the intent to switch case later
    private static final int addMarkerIntent = 1;

    //Trajectory control variables, each bubble should have its own, create new class?
    double wobbler1;
    double wobbler2;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

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

    //Draws a bubble, and return the ground overlay object.
    public GroundOverlay bubbleMake(LatLng location, float width, float height){
        GroundOverlayOptions bubbleMake = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.crystal_bubble))
                .position(location, width, height);//position(location, width, height)
        return mMap.addGroundOverlay(bubbleMake);
    }

    //TODO: Might want to move some of this functions to their own java class. Needs more modularity.
    public void wobbleBubbles (List<GroundOverlay> bubbles){
        wobbler1 = wobbler1 + .01;
        wobbler2 = wobbler2 + .02;
        //iterate over the list, uses for each syntax, nice Java 8 feature. Can parallelize?
        for (GroundOverlay currentBubble : bubbles) {
            double moveY = currentBubble.getPosition().latitude + Math.cos(wobbler1) / 12000.0 + Math.cos(wobbler2) / 13000.0;
            double moveX = currentBubble.getPosition().longitude + Math.sin(wobbler1) / 12000.0 + Math.cos(wobbler2) / 13000.0;

            currentBubble.setPosition(new LatLng(moveY, moveX));//changes the bubble position.
            currentBubble.setVisible(true);//Re-draws the bubble in its new position.

            if(mMap.getCameraPosition().zoom > 8){
                currentBubble.setVisible(true);
            }
            else{
                currentBubble.setVisible(false);
            }
        }
    }

    //this should update all bubbles. All bubbles should be added to some sort of array or data structure.
    Runnable bubbleUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                if(!myBubbles.isEmpty()){
                    wobbleBubbles(myBubbles);//Changes bubbles coordinates
                }else{
                    Log.d("BubbleUp", "Bubble list is empty.");
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

        //((ViewGroup) findViewById(R.id.map)).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        myBubbles = new ArrayList<>();

        //Permission Check
        getLocationPermission();
        //If permission is granted then create location button.
        if(mLocationPermissionGranted){
            Log.d("BubbleUp","Permission has already been Granted");
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }

        //TODO: Make bubbles/groundMark interactive?
        //Example Bubble
        GroundOverlay myBubble = bubbleMake(new LatLng(38.97, -95.23), 8600f, 8600f);//Draws a bubble near lawrence
        GroundOverlay myBubble2 = bubbleMake(new LatLng(38.90, -95.10), 8600f, 8600f);//Draws a bubble near lawrence
        GroundOverlay myBubble3 = bubbleMake(new LatLng(39.00, -95.00), 8600f, 8600f);//Draws a bubble near lawrence
        GroundOverlay myBubble4 = bubbleMake(new LatLng(38.75, -95.30), 8600f, 8600f);//Draws a bubble near lawrence
        //Add Bubble to array.
        myBubbles.add(myBubble);
        myBubbles.add(myBubble2);
        myBubbles.add(myBubble3);
        myBubbles.add(myBubble4);

        //TODO: Make a bubble creation activity.

        //Initiate bubble updater
        bubbleUpdater.run();//Run the bubble updater.

        //add a marker in Lawrence
        LatLng lawrence = new LatLng(38.9717, -95.2353);
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(lawrence)
                .title("Lawrence"));

        //set listener to call addMarkerActivity when a click is registerd on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                Intent edit = new Intent(MapsActivity.this, AddMarkerActivity.class);
                edit.putExtra("location", latLng);
                MapsActivity.this.startActivityForResult(edit, addMarkerIntent);
            }
        });


        //experementing with fragment sharing
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {


            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (addMarkerIntent) : {
                if (resultCode == Activity.RESULT_OK) {
                    MarkerOptions markerOptions = data.getParcelableExtra("marker");
                    mMap.addMarker(markerOptions);
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
        //Transaction
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.remove(myFragment);
        //fragmentTransaction.commit();

        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
