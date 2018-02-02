package com.example.bubbleup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

public class BubbleMarker implements Serializable{

    public String msg;
    public String tittle;

    public int zoomUpBound;
    public int zoomDownBound;

    public int id;

    public MarkerOptions bubbleMarkerOption;
    public Marker bubbleMarker;

    private double wobbler1 = 0;
    private double wobbler2 = 0;

    public BubbleMarker(LatLng mCoor, String text, String poster, String tittle, int width, int height, Context myContext){
        bubbleMarkerOption = new MarkerOptions().position(mCoor);

        bubbleMarkerOption.snippet(text);
        bubbleMarkerOption.title(poster);

        msg = text;//Text message to be displayed

        //creates a bitMap from our cyrstal bubble image
        Bitmap bitMap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.crystal_bubble);

        //scales the bitmap to a predetermined size
        Bitmap scaledBitMap = Bitmap.createScaledBitmap(bitMap, width,height, true);

        //adds the scaled bitmap to our marker icon
        bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(scaledBitMap));

        wobbler1 = mCoor.latitude * mCoor.latitude;
        wobbler2 = mCoor.longitude * mCoor.longitude;

        //bubbleMarker = mMap.addMarker(bubble);
    }

    public Marker addMarker(GoogleMap mMap){
        bubbleMarker = mMap.addMarker(bubbleMarkerOption);
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

            //Log.d("BubbleUp", "Y = " + wobbler1 + "X = " + wobbler2);

            /*
            if(mMap.getCameraPosition().zoom > 8){
                currentBubble.bubble.visible(true);
            }else{
                currentBubble.bubble.visible(false);
            }
            */
        }
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
