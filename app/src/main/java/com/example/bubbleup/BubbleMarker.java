package com.example.bubbleup;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
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

    public int myLikes;

    public String username = "";

    public MarkerOptions bubbleMarkerOption;
    public Marker bubbleMarker;

    private double wobbler1 = 0;
    private double wobbler2 = 0;

    public Bitmap profile_image;
    public Bitmap overlay;

    public int myWidth;
    public int myHeight;

    public int myAgeMins;

    int userReaction;

    int myType = 0;

    Uri myUri;

    String myUrl;

    public BubbleMarker(LatLng mCoor, int user_id, int reaction, int like_count, int type, int post_id, String text, String poster, String tittle, int width, int height, int age_minutes,Context myContext, Bitmap image){
        bubbleMarkerOption = new MarkerOptions().position(mCoor);

        myPost_id = post_id;

        myAgeMins = age_minutes;

        userReaction = reaction;

        myType = type;

        myLikes = like_count;

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

        int icon;
        switch (myType){
            case 1:
                icon = R.drawable.ic_music;
                break;
            case 2:
                icon = R.drawable.ic_photo;
                break;
            case 3:
                icon = R.drawable.ic_video;
                break;
            case 4:
                icon = R.drawable.ic_animals;
                break;
            case 5:
                icon = R.drawable.ic_cloud;
                break;
            case 6:
                icon = R.drawable.ic_economy;
                break;
            case 7:
                icon = R.drawable.ic_shopping;
                break;
            case 8:
                icon = R.drawable.ic_question;
                break;
            default:
                icon = R.drawable.ic_post;
                break;
        }

        //Extra overlay
        Bitmap typeBitmap = BitmapFactory.decodeResource(myContext.getResources(), icon);
        typeBitmap = Bitmap.createScaledBitmap(typeBitmap, width / 3, height / 3, true);

        /*
        //Extra bubble overlay for the icon, doesn't add clarity, may remove.
        Bitmap bubbleIconOverlay = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.crystal_bubble);
        bubbleIconOverlay = Bitmap.createScaledBitmap(bubbleIconOverlay, width / 3,height / 3, true);

        typeBitmap = overlay(typeBitmap, bubbleIconOverlay);
        */

        overlay = overlayAdd(typeBitmap, overlay, "",0.0f, 0.0f);

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

        Bitmap result = overlay(Bitmap.createScaledBitmap(getclip(image), myWidth, myHeight,true), overlay);

        if(bubbleMarker != null){
            bubbleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(result));
        }
    }

    public static Bitmap getclip(Bitmap image) {
        int min, max;
        if(image.getWidth() < image.getHeight()){
            min = image.getWidth();
            max = image.getHeight();
        }else{
            min = image.getHeight();
            max = image.getWidth();
        }
        Bitmap output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, image.getWidth(), image.getHeight());

        //Edge Smoothing
        paint.setAntiAlias(true);
        //setting transparent canvas
        canvas.drawARGB(0, 0, 0, 0);

        int offset = max/2 - min/2;

        if(max == image.getWidth())
            canvas.translate(0.0f - offset,0.0f);
        else
            canvas.translate(0.0f,0.0f - offset);

        //drawing a circle
        canvas.drawCircle(image.getWidth() / 2, image.getHeight() / 2, min / 2, paint);

        //Some reference: https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //Drawing the bitmap
        canvas.drawBitmap(image, rect, rect, paint);

        return output;
    }

    private Bitmap overlayAdd(Bitmap image, Bitmap bubbleOverlay, String emojiString, float x, float y) {
        Bitmap finalBubble = Bitmap.createBitmap(bubbleOverlay.getWidth(), bubbleOverlay.getHeight(), bubbleOverlay.getConfig());
        Canvas canvas = new Canvas(finalBubble);
        canvas.drawBitmap(bubbleOverlay, new Matrix(), null);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#d3d3d3"));
        circlePaint.setAntiAlias(true);
        canvas.drawCircle(x + image.getWidth() / 2, y +image.getHeight() / 2, image.getWidth() / 2, circlePaint);


        Matrix imageMat = new Matrix();
        imageMat.setTranslate(bubbleOverlay.getWidth()*x,bubbleOverlay.getHeight()*y);
        canvas.drawBitmap(image, imageMat, null);

        //Emoji?
        String someEmojis = "\uD83C\uDDE7\uD83C\uDDF4";
        //Character.toChars(0x1F369);
        if (false){
            TextPaint paint = new TextPaint();
            String emoji = new String(Character.toChars(someEmojis.codePointAt(0)));
            paint.setTextSize( (int) (bubbleOverlay.getWidth()/3.5));
            StaticLayout lsLayout = new StaticLayout(emoji, paint, (int) (bubbleOverlay.getWidth()/3), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
            lsLayout.draw(canvas);
        }

        return finalBubble;
    }

    private Bitmap overlay(Bitmap image, Bitmap bubbleOverlay) {
        Bitmap finalBubble = Bitmap.createBitmap(bubbleOverlay.getWidth(), bubbleOverlay.getHeight(), bubbleOverlay.getConfig());
        Canvas canvas = new Canvas(finalBubble);
        canvas.drawBitmap(image, new Matrix(), null);
        canvas.drawBitmap(bubbleOverlay, new Matrix(), null);
        return finalBubble;
    }

    public Bitmap getProfileImage(){
        return profile_image;
    }
}
