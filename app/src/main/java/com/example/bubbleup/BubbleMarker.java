package com.example.bubbleup;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BubbleMarker implements Serializable{

    public String msg;
    public String tittle;

    public int zoomUpBound;
    public int zoomDownBound;

    public int myUser_id;
    public int myPost_id;

    public String username = "";

    public MarkerOptions bubbleMarkerOption;
    public Marker bubbleMarker;

    private double wobbler1 = 0;
    private double wobbler2 = 0;

    public Bitmap profile_image;
    public Bitmap overlay;

    public int myWidth;
    public int myHeight;

    Uri myUri;

    String myUrl;

    public BubbleMarker(LatLng mCoor, int user_id,String text, String poster, String tittle, int width, int height, Context myContext, Bitmap image){
        bubbleMarkerOption = new MarkerOptions().position(mCoor);

        profile_image = image;

        myUser_id = user_id;

        myWidth = width;
        myHeight = height;

        myUri = Uri.parse(text);

        //Patterns.WEB_URL.matcher(text).matches();

        String[] splitText = text.split("\\s+");

        for(int i = 0; i < splitText.length; i++){
            if(Patterns.WEB_URL.matcher(splitText[i]).matches()){
                myUrl = splitText[i];
            }
        }

        bubbleMarkerOption.snippet(text);
        bubbleMarkerOption.title(poster);

        msg = text;//Text message to be displayed

        //creates a bitMap from our crystal bubble image
        overlay = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.crystal_bubble);

        //scales the bitmap to a predetermined size
        overlay = Bitmap.createScaledBitmap(overlay, width,height, true);

        //TODO: Might want to edit this?
        if(image != null) {
            RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(Resources.getSystem(), image);
            img.setCircular(true);
            overlay = overlay(overlay, Bitmap.createScaledBitmap(img.getBitmap(),myWidth,myHeight,true));
        }

        //adds the scaled bitmap to our marker icon
        bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(overlay));

        wobbler1 = mCoor.latitude * mCoor.latitude;
        wobbler2 = mCoor.longitude * mCoor.longitude;
    }

    public Marker addMarker(GoogleMap mMap){
        bubbleMarker = mMap.addMarker(bubbleMarkerOption);
        if(myUrl != null){
            bubbleMarker.setTag(myUrl);
        } else {
            bubbleMarker.setTag("");
        }
        return bubbleMarker;
    }

    public void wobble(){
        if(bubbleMarker != null && bubbleMarker.isVisible()){
            wobbler1 += .01;
            wobbler2 += .02;
            double moveY = bubbleMarker.getPosition().latitude + Math.cos(wobbler1)/185000.0 + Math.cos(wobbler2)/187000.0;
            double moveX = bubbleMarker.getPosition().longitude + Math.sin(wobbler1)/185000.0 + Math.cos(wobbler2)/187000.0;

            bubbleMarker.setPosition(new LatLng(moveY, moveX));//changes the bubble position.
            bubbleMarker.setVisible(true);//Re-draws the bubble in its new position.

        }
    }

    public void updateImage(Bitmap image){
        profile_image = image;
        RoundedBitmapDrawable img = RoundedBitmapDrawableFactory.create(Resources.getSystem(), image);
        img.setCircular(true);
        Bitmap result = overlay(Bitmap.createScaledBitmap(img.getBitmap(),myWidth,myHeight,true), overlay);
        if(bubbleMarker != null){
            bubbleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(result));
        }
    }

    private Bitmap overlay(Bitmap image1, Bitmap image2) {
        Bitmap bmOverlay = Bitmap.createBitmap(image1.getWidth(), image1.getHeight(), image1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(image1, new Matrix(), null);
        canvas.drawBitmap(image2, new Matrix(), null);
        return bmOverlay;
    }

    public Bitmap getProfileImage(){
        return profile_image;
    }
}

/*
//Draws a bubble, and return the ground overlay object.
public GroundOverlay bubbleMake(LatLng location, float width, float height){
    GroundOverlayOptions bubbleMake = new GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.crystal_bubble))
            .position(location, width, height);//position(location, width, height)
    return mMap.addGroundOverlay(bubbleMake);
}

 */
