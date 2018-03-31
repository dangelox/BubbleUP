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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.EmojiMetadata;
import android.support.text.emoji.EmojiSpan;
import android.support.text.emoji.TypefaceEmojiSpan;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.text.emoji.widget.EmojiTextView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
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
import java.util.HashMap;
import java.util.Map;
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
    public int myAgeHours;
    public int myAgeDays;
    public int myDayOfMonth;
    public int myMonthOfYear;
    public int myYearOfPost;

    int userReaction;

    int myCommentCount;

    int myType = 0;
    int emoji_num;
    int [] emojis;
    int sentiment;
    boolean analyzed = false;

    Uri myUri;

    String myUrl = "";

    String [] emojiArray;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public BubbleMarker(LatLng mCoor, int user_id, int reaction, int like_count, int comment_count, int type, int post_id, String text, String poster, String tittle, int width, int height, int age_minutes, int age_hours, int age_days, int  day_of_month, int month_of_year, int year_of_post, Context myContext, Bitmap image, boolean showHeatMap){
        bubbleMarkerOption = new MarkerOptions().position(mCoor);
        bubbleMarkerOption.anchor(0.5f, 0.5f);

        emojis = new int[3];

        myPost_id = post_id;

        myCommentCount = comment_count;

        myAgeMins = age_minutes;
        myAgeHours = age_hours;
        myAgeDays = age_days;
        myDayOfMonth = day_of_month;
        myMonthOfYear = month_of_year;
        myYearOfPost = year_of_post;

        userReaction = reaction;

        myType = type;
        //How is the weather here? 623247363
        //to be or not to be? 671088643
        //type = 544374848;

        /////////////
        //BIT MAGIC//
        /////////////
        myType = (type & DeepEmoji.CONTENT_TYPE_MASK) >> DeepEmoji.CONTENT_TYPE_SHIFT;
        emoji_num = (type & DeepEmoji.EMOJI_NUM_MASK) >> DeepEmoji.EMOJI_NUM_SHIFT;
        Log.d("BubbleMarker", "Number of emojis = " + emoji_num);
        emojis[0] = (type & DeepEmoji.EMOJI_1_MASK) >> DeepEmoji.EMOJI_1_SHIFT;
        emojis[1] = (type & DeepEmoji.EMOJI_2_MASK) >> DeepEmoji.EMOJI_2_SHIFT;
        emojis[2] = (type & DeepEmoji.EMOJI_3_MASK) >> DeepEmoji.EMOJI_3_SHIFT;
        sentiment = type & DeepEmoji.SENTIMENT_MASK;
        analyzed = (type & DeepEmoji.ANALYZED_MASK) >> DeepEmoji.ANALYZED_SHIFT == 1;
        Log.d("BubbleMarker", "Sentiment = " + sentiment + ", emoji[0] = " + emojis[0] + ", emoji[1] = " + emojis[1] + ", emoji[2] = " + emojis[2]);

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
            case 9:
                icon = R.drawable.ic_beach_umbrella;
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
        if(!showHeatMap){
            bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(overlay));
        }
        else{
            switch (sentiment){
                case 0:
                    if(analyzed){
                        bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_0)));
                    }
                    else{
                        bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_not)));
                    }
                    break;
                case 1:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_1)));
                    break;
                case 2:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_2)));
                    break;
                case 3:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_3)));
                    break;
                case 4:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_4)));
                    break;
                case 5:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_5)));
                    break;
                case 6:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_6)));
                    break;
                default:
                    bubbleMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_sentiment_not)));
            }
        }

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

    public void updateImage(Bitmap image, boolean showHeatMap){
        if(!showHeatMap){
            profile_image = image;

            Bitmap result = overlay(Bitmap.createScaledBitmap(getclip(image), myWidth, myHeight,true), overlay);

            if(bubbleMarker != null){
                bubbleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(result));
            }
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

        //Painting sentiment
        Paint sentimentCircle = new Paint();
        switch (sentiment){
            case (0):
                if(analyzed){
                    sentimentCircle.setColor(Color.parseColor("#ff0000"));
                } else {
                    sentimentCircle.setColor(Color.parseColor("#6b6b6b"));
                }
                break;
            case (1):
                sentimentCircle.setColor(Color.parseColor("#f46100"));
                break;
            case (2):
                sentimentCircle.setColor(Color.parseColor("#f4a200"));
                break;
            case (3):
                sentimentCircle.setColor(Color.parseColor("#f4ef00"));
                break;
            case (4):
                sentimentCircle.setColor(Color.parseColor("#bbf400"));
                break;
            case (5):
                sentimentCircle.setColor(Color.parseColor("#86f400"));
                break;
            case (6):
                sentimentCircle.setColor(Color.parseColor("#00ff00"));
                break;
            default:
                sentimentCircle.setColor(Color.parseColor("#6b6b6b"));
                break;
        }
        canvas.drawCircle(x + image.getWidth() / 5.5f, y +image.getHeight() / 5.5f, image.getWidth() / 5.5f, sentimentCircle);

        //Emoji?
        //String someEmojis = "\uD83C\uDF0E";
        //String someEmojis = "\u0001\uf602";//smiley
        //Character.toChars(0x1F369);
        if (emoji_num > 0){
            double theta = -6*Math.PI/4;
            double increase = 0.8;
            double place = 0.8;
            Log.d("BubbleMarker","Attempting to draw 1");
            canvas.translate(canvas.getWidth()/2,canvas.getHeight()/5.5f);
            canvas.save();
            for(int i = 0; i < emoji_num; i++){
                Canvas copy = canvas;
                Log.d("BubbleMarker","Attempting to draw 2");
                TextPaint paint = new TextPaint();

                switch (emoji_num){
                    case (2):
                        if(i == 0){
                            theta -= increase/2;
                        }
                        break;
                    case (3):
                        if(i == 0){
                            theta -= increase;
                        }
                        break;
                }

                //paint.setTypeface();

                String emoji = DeepEmoji.emojiArray[emojis[i%3]];

                Log.d("BubbleMarker","Drawing emoji, " + emoji);
                paint.setTextSize( (int) (bubbleOverlay.getWidth()/3.5));


                canvas.restore();
                canvas.save();
                canvas.translate((float) (canvas.getWidth()/2.2 * Math.cos(theta)), (float) (canvas.getWidth()/2.2 * Math.sin(theta)));

                StaticLayout lsLayout = new StaticLayout(EmojiCompat.get().process(emoji), paint, 0, Layout.Alignment.ALIGN_CENTER, 1, 1, true);
                lsLayout.draw(canvas);
                //canvas.drawText(emoji,100,100,paint);
                place = place + 0.4;

                theta += increase;
            }
            /*
            TextPaint paint = new TextPaint();
            String emoji = new String(Character.toChars(someEmojis.codePointAt(0)));
            paint.setTextSize( (int) (bubbleOverlay.getWidth()/3.5));
            StaticLayout lsLayout = new StaticLayout(emoji, paint, (int) (bubbleOverlay.getWidth()/3), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
            lsLayout.draw(canvas);
            */
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
