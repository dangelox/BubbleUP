package com.example.bubbleup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.text.emoji.EmojiCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLngBounds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.bubbleup.MapsActivity.SAVEDLOCATION_PREF;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String SAVEDLOCATION_PREF = "previous_location";

    String url_like ="https://bubbleup-api.herokuapp.com/likes/";
    String url_posts ="https://bubbleup-api.herokuapp.com/posts/";
    String url_users_posts ="https://bubbleup-api.herokuapp.com/posts/user/";
    String url_users_likes ="https://bubbleup-api.herokuapp.com/posts/ilike/";
    String url_comments ="https://bubbleup-api.herokuapp.com/posts_comments/";
    String url_bio = "https://bubbleup-api.herokuapp.com/user/user_bio";
    String url_set_name = "https://bubbleup-api.herokuapp.com/user/name";
    String url_set_bio = "https://bubbleup-api.herokuapp.com/user/user_bio";
    String url_set_profile_pic = "https://bubbleup-api.herokuapp.com/user/image";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String fragmentToken;

    private OnFragmentInteractionListener mListener;

    View myView;
    ViewGroup myViewGroupContainer;

    //Profile Viewers
    View profileView;
    LinearLayout profileContainer;

    LayoutInflater myInflater;

    int currentUserId;

    SharedPreferences saved_settings;

    ArrayList<View> emptyUserData;

    //Edit Variables
    boolean edit_settings_clicked = true;
    EditText textEdit;

    public ContentFragment() {
        // Required empty public constructor
    }

    //Comparators for sorting
    public class BubbleComparatorSize implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) right.myHeight).compareTo((Integer) left.myHeight);
        }
    }

    public class BubbleComparatorAgeMinsNewest implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) left.myAgeMins).compareTo((Integer) right.myAgeMins);
        }
    }

    public class BubbleComparatorAgeMinsOldest implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) right.myAgeMins).compareTo((Integer) left.myAgeMins);
        }
    }

    public class BubbleComparatorSentimentGood implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) right.sentiment).compareTo((Integer) left.sentiment);
        }
    }

    public class BubbleComparatorSentimentBad implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) left.sentiment).compareTo((Integer) right.sentiment);
        }
    }

    public static String getYoutubeVideoIdFromUrl(String inUrl) {
        if (inUrl.toLowerCase().contains("youtu.be")) {
            return inUrl.substring(inUrl.lastIndexOf("/") + 1);
        }
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(inUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendToFragment(List<BubbleMarker> bubbleList, LatLngBounds bounds, boolean erase, int sortingChoice){
        final LinearLayout myList = (LinearLayout) myView.findViewById(R.id.linear_view);

        emptyUserData = new ArrayList<>();

        myList.setBackgroundColor(Color.parseColor(saved_settings.getString("backGround_Color","#f2f2f2")));

        if(erase){
            myList.removeAllViews();
        }

        //sorting options
        switch (sortingChoice){
            case 0:
                Collections.sort(bubbleList, new BubbleComparatorSize());
                break;
            case 1:
                Collections.sort(bubbleList, new BubbleComparatorAgeMinsNewest());
                break;
            case 2:
                Collections.sort(bubbleList, new BubbleComparatorAgeMinsOldest());
                break;
            case 3:
                Collections.sort(bubbleList, new BubbleComparatorSentimentGood());
                break;
            case 4:
                Collections.sort(bubbleList, new BubbleComparatorSentimentBad());
                break;
        }

        //Creating a new view for each bubble
        int countBubble = 0;
        for (final BubbleMarker currentBubble : bubbleList) {
            if(bounds == null || bounds.contains(currentBubble.bubbleMarker.getPosition())){
                Log.d("BubbleUp_Fragment",currentBubble.msg);

                final View container = myInflater.inflate(R.layout.fragment_post_container, myList, false);



                final LinearLayout body = (LinearLayout) container.findViewById(R.id.fragment_body_linear);

                final TextView myCommentCounter = (TextView) container.findViewById(R.id.comment_counter);
                if(currentBubble.mySource == 1) {
                    myCommentCounter.setVisibility(View.INVISIBLE);
                } else {
                    myCommentCounter.setText(Integer.toString(currentBubble.myCommentCount));
                }

                if(!currentBubble.myUrl.equals("")){
                    Log.d("BubbleUp_URL","Loading from: " + currentBubble.myUrl);
                    ImageView thumbnail = new ImageView(getContext());
                    thumbnail.setPadding(8,8,8,8);
                    body.addView(thumbnail);

                    if(getYoutubeVideoIdFromUrl(currentBubble.myUrl) != null){
                        String url = "https://img.youtube.com/vi/" + getYoutubeVideoIdFromUrl(currentBubble.myUrl) + "/0.jpg";
                        Picasso.get().load(url).into(thumbnail);
                    } else {
                        Picasso.get().load(currentBubble.myUrl).into(thumbnail);
                    }
                    thumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) currentBubble.bubbleMarker.getTag()));
                            startActivity(browserIntent);
                        }
                    });
                }

                TextView text = (TextView) container.findViewById(R.id.fragment_body_textView);
                text.setText(currentBubble.msg);

                /*
                //Not necessary?
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if( currentBubble.bubbleMarker.getTag() != null && Patterns.WEB_URL.matcher((String) currentBubble.bubbleMarker.getTag()).matches()){
                            //Toast.makeText(this, "Checking for links.", Toast.LENGTH_SHORT).show();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) currentBubble.bubbleMarker.getTag()));
                            startActivity(browserIntent);
                        } else {
                            Toast.makeText(getContext(), "No URL on post.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                */

                final TextView likeCounter = (TextView) container.findViewById(R.id.text_like_counter);
                if(currentBubble.mySource == 1){
                    likeCounter.setVisibility(View.INVISIBLE);
                } else {
                    likeCounter.setText(Integer.toString(currentBubble.myLikes));
                }

                final int myPost_id = currentBubble.myPost_id;

                //final BubbleMarker bubble = currentBubble;
                ImageView sentimentBubble = (ImageView) container.findViewById(R.id.sentBubbleImageView);
                switch (currentBubble.sentiment){
                    case (0):
                        if(currentBubble.analyzed){
                            sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_0));
                        } else {
                            if (currentBubble.mySource == 1) {
                                sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_twitter));
                            } else {
                                sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_not));
                            }
                        }
                        break;
                    case (1):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_1));
                        break;
                    case (2):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_2));
                        break;
                    case (3):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_3));
                        break;
                    case (4):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_4));
                        break;
                    case (5):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_5));
                        break;
                    case (6):
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_6));
                        break;
                    default:
                        sentimentBubble.setBackground(getResources().getDrawable(R.drawable.ic_sentiment_not));
                        break;
                }

                TextView userNameText = (TextView) container.findViewById(R.id.textViewUserName);
                userNameText.setText(currentBubble.username);
                userNameText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MapsActivity) getActivity()).profile_display = true;
                        ((MapsActivity) getActivity()).sorting_spinner.setVisibility(View.GONE);
                        showProfile( ((MapsActivity) getActivity()).myId, currentBubble.myUser_id, true, true);
                    }
                });

                TextView ageOfPostText = (TextView) container.findViewById(R.id.textViewAgeOfPost);
                if(currentBubble.myAgeMins == 0){
                    ageOfPostText.setText("Just now");
                }
                else if(currentBubble.myAgeMins <= 59) {
                    ageOfPostText.setText(String.valueOf(currentBubble.myAgeMins).concat("m"));
                }
                else if(currentBubble.myAgeHours <= 23){
                    ageOfPostText.setText(String.valueOf(currentBubble.myAgeHours).concat("h"));
                }
                else if(currentBubble.myAgeDays <= 7){
                    ageOfPostText.setText(String.valueOf(currentBubble.myAgeDays).concat("d"));
                }
                else if(currentBubble.myAgeDays <= 364) {
                    ageOfPostText.setText("" + currentBubble.myDayOfMonth + " " + getMonthString(currentBubble.myMonthOfYear));
                }
                else{

                    ageOfPostText.setText("" + currentBubble.myDayOfMonth + " " + getMonthString(currentBubble.myMonthOfYear) + " " + currentBubble.myYearOfPost);
                }

                TextView emojisText = (TextView) container.findViewById(R.id.textViewEmojis);
                String emojiString = "";
                for(int i = 0; i < currentBubble.emoji_num; i++){
                    emojiString += EmojiCompat.get().process(DeepEmoji.emojiArray[currentBubble.emojis[i]]);
                }
                emojisText.setText(emojiString);

                String userName = currentBubble.bubbleMarkerOption.getTitle().substring(0, Math.min(currentBubble.bubbleMarkerOption.getTitle().length(), 6));

                ImageButton userImage = (ImageButton) container.findViewById(R.id.imageButton);
                userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ((MapsActivity) getActivity()).profile_display = true;
                        ((MapsActivity) getActivity()).sorting_spinner.setVisibility(View.GONE);
                        showProfile( ((MapsActivity) getActivity()).myId, currentBubble.myUser_id, true, true);
                    }
                });

                Button deleteButton = (Button) container.findViewById(R.id.deleteButton);

                //check if the bubble is the current user's bubble and should show the deletion button
                if (getActivity() instanceof MapsActivity){
                    currentUserId = ((MapsActivity) getActivity()).myId;
                }
                if(currentBubble.myUser_id == currentUserId) {

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            Log.d("BubbleUp","Long Clicked Detected");
                            if(currentBubble.myUser_id == currentUserId){
                                Log.d("BubbleUp","Same ID");

                                //Build an alert dialog to prompt deletion
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                                LinearLayout layout = new LinearLayout(getContext());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setLayoutParams(params);

                                layout.setGravity(Gravity.CLIP_VERTICAL);
                                layout.setPadding(2, 2, 2, 2);

                                alertDialogBuilder.setView(layout);
                                alertDialogBuilder.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));
                                alertDialogBuilder.setTitle("Delete this post?");

                                alertDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        StringRequest deletePostRequest = new StringRequest(Request.Method.DELETE, url_posts + currentBubble.myPost_id,
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        Toast.makeText(getActivity(), "Deletion Success", Toast.LENGTH_SHORT).show();
                                                        ViewGroup vg = myList;
                                                        vg.removeView(container);

                                                        if (getActivity() instanceof MapsActivity){
                                                            ((MapsActivity) getActivity()).myBubbles.remove(currentBubble);
                                                            currentBubble.bubbleMarker.remove();
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
                                                params.put("Authorization", "JWT " + fragmentToken);
                                                return params;
                                            }
                                        };

                                        ((MapsActivity) getActivity()).queue.add(deletePostRequest);
                                    }
                                });

                                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();

                                try {
                                    alertDialog.show();
                                    Log.d("BubbleUp","Dialog Success");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d("BubbleUp","Dialog Fail");
                                }
                            } else {
                                return;
                            }
                            return;
                        }
                    });
                }
                else{
                    container.findViewById(R.id.deleteButton).setVisibility(View.GONE);
                }

                Button findBubbleButton = (Button) container.findViewById(R.id.findBubbleButton);

                findBubbleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MapsActivity) getActivity()).moveCamera(currentBubble.bubbleMarker.getPosition());
                    }
                });

                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Expand post on new window?
                    }
                });

                //Comment Related Section
                //We create an object for the vertical layout of the card
                final LinearLayout postCardVertical = (LinearLayout) container.findViewById(R.id.post_card_vertical);

                //We inflate the comment section
                final LinearLayout commentSection = (LinearLayout) myInflater.inflate(R.layout.post_comment_section, postCardVertical, false);

                //We make an object for the list of comments
                final LinearLayout commentSectionList = (LinearLayout) commentSection.findViewById(R.id.comment_section_list);

                //Comment button creates a new dialog pop up
                Button addCommentButton = (Button) commentSection.findViewById(R.id.add_comment_button);
                addCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Text Edit field
                        final EditText commentEdit = new EditText(getContext());
                        commentEdit.setHint("Write a comment...");

                        //Setting the dialog box
                        AlertDialog.Builder alertCommentDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertCommentDialogBuilder.setIcon(R.drawable.ic_comment);

                        LinearLayout commentDialogLayout = new LinearLayout(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        commentDialogLayout.setOrientation(LinearLayout.VERTICAL);
                        commentDialogLayout.setLayoutParams(params);

                        commentDialogLayout.setGravity(Gravity.CLIP_VERTICAL);
                        commentDialogLayout.setPadding(2, 2, 2, 2);

                        commentDialogLayout.addView(commentEdit);

                        alertCommentDialogBuilder.setView(commentDialogLayout);
                        alertCommentDialogBuilder.setTitle("Comment:");

                        //Posting the dialog box contents
                        alertCommentDialogBuilder.setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(commentEdit.getText().equals("")){
                                    //Comment is empty, don't post
                                    Toast.makeText(getContext(), "Empty Comment", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Making post request
                                    StringRequest postComment = new StringRequest(Request.Method.POST, url_comments + currentBubble.myPost_id,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        final JSONObject commentJSON = new JSONObject(response);

                                                        final LinearLayout comment = (LinearLayout) myInflater.inflate(R.layout.post_comment, commentSectionList, false);
                                                        TextView commentBody = (TextView) comment.findViewById(R.id.comment_body);
                                                        commentBody.setText(commentJSON.getString("comment"));

                                                        currentBubble.myCommentCount++;
                                                        myCommentCounter.setText(Integer.toString(currentBubble.myCommentCount));

                                                        TextView commentUser = (TextView) comment.findViewById(R.id.comment_username);
                                                        String username = ((MapsActivity) getActivity()).profileNameStorage.get(commentJSON.getInt("user_id"));
                                                        if(username != null){
                                                            commentUser.setText(username);
                                                        } else {
                                                            commentUser.setText( "User #" + commentJSON.getInt("user_id"));
                                                        }

                                                        Button deleteCommentButton = (Button) comment.findViewById(R.id.delete_comment_button);
                                                        deleteCommentButton.setOnClickListener(new View.OnClickListener() {
                                                            public void onClick(View v) {
                                                                try {
                                                                    StringRequest deleteCommentRequest = new StringRequest(Request.Method.DELETE, url_comments + commentJSON.getInt("id"),
                                                                            new Response.Listener<String>() {
                                                                                @Override
                                                                                public void onResponse(String response) {
                                                                                    Toast.makeText(getActivity(), "Deletion Success", Toast.LENGTH_SHORT).show();
                                                                                    ViewGroup vg = commentSectionList;
                                                                                    vg.removeView(comment);
                                                                                    currentBubble.myCommentCount--;
                                                                                    myCommentCounter.setText(Integer.toString(currentBubble.myCommentCount));
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
                                                                            params.put("Authorization", "JWT " + fragmentToken);
                                                                            return params;
                                                                        }
                                                                    };

                                                                    ((MapsActivity) getActivity()).queue.add(deleteCommentRequest);
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });

                                                        commentSectionList.addView(comment);

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d("BubbleUp", " : Bubble Comments Response Error! " + error.getMessage());
                                        }
                                    }) {
                                        @Override
                                        public Map<String, String> getParams() throws AuthFailureError {
                                            HashMap<String, String> params = new HashMap<>();
                                            params.put("comment", "\"" + commentEdit.getText() + "\"");
                                            params.put("lat", "0.0");
                                            params.put("lng", "0.0");
                                            return params;
                                        }
                                        @Override
                                        public byte[] getBody() throws AuthFailureError {
                                            String httpPostBody="{\"comment\": \"" + commentEdit.getText() + "\"" + "," +
                                                    "\"lat\": " + "0.0" + "," +
                                                    "\"lng\": " + "0.0" + "}";
                                            // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
                                            return httpPostBody.getBytes();
                                        }
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            Map<String, String> params = new HashMap();
                                            params.put("Authorization", "JWT " + fragmentToken);
                                            params.put("Content-Type","application/json");
                                            return params;
                                        }
                                    };

                                    ((MapsActivity) getActivity()).queue.add(postComment);
                                }
                            }
                        });

                        alertCommentDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        AlertDialog alertCommentDialog = alertCommentDialogBuilder.create();

                        try {
                            alertCommentDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("BubbleUp","Dialog Comment Fail");
                        }
                    }
                });

                final ToggleButton toggleComments = (ToggleButton) container.findViewById(R.id.comment_toggle_button);

                if(currentBubble.mySource == 1){
                    toggleComments.setVisibility(View.INVISIBLE);
                } else {
                    toggleComments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            if (isChecked) {
                                postCardVertical.addView(commentSection);

                                StringRequest getComments = new StringRequest(Request.Method.GET, url_comments + currentBubble.myPost_id,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {

                                                    commentSectionList.removeAllViews();

                                                    final JSONArray commentsArr = new JSONArray(response);

                                                    if(commentsArr.length() == 0){
                                                        TextView emptyPostMessage = new TextView(getContext());
                                                        emptyPostMessage.setText("No comments.");
                                                        commentSectionList.addView(emptyPostMessage);
                                                    } else {
                                                        for (int i = 0; i < commentsArr.length(); i++) {
                                                            final JSONObject commentJSON = (JSONObject) commentsArr.get(i);

                                                            final LinearLayout comment = (LinearLayout) myInflater.inflate(R.layout.post_comment, commentSectionList, false);
                                                            TextView commentBody = (TextView) comment.findViewById(R.id.comment_body);
                                                            commentBody.setText(commentJSON.getString("comment"));

                                                            TextView commentUser = (TextView) comment.findViewById(R.id.comment_username);
                                                            String username = ((MapsActivity) getActivity()).profileNameStorage.get(commentJSON.getInt("user_id"));
                                                            if (username != null) {
                                                                commentUser.setText(username);
                                                            } else {
                                                                commentUser.setText("User #" + commentJSON.getInt("user_id"));
                                                            }

                                                            //check if post is current user's comment, if so keep the delete button
                                                            if (getActivity() instanceof MapsActivity){
                                                                currentUserId = ((MapsActivity) getActivity()).myId;
                                                            }
                                                            if(commentJSON.getInt("user_id") == currentUserId){
                                                                Button deleteCommentButton = (Button) comment.findViewById(R.id.delete_comment_button);
                                                                deleteCommentButton.setOnClickListener(new View.OnClickListener() {
                                                                    public void onClick(View v) {
                                                                        try {
                                                                            StringRequest deleteCommentRequest = new StringRequest(Request.Method.DELETE, url_comments + commentJSON.getInt("id"),
                                                                                    new Response.Listener<String>() {
                                                                                        @Override
                                                                                        public void onResponse(String response) {
                                                                                            Toast.makeText(getActivity(), "Deletion Success", Toast.LENGTH_SHORT).show();
                                                                                            ViewGroup vg = commentSectionList;
                                                                                            vg.removeView(comment);
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
                                                                                    params.put("Authorization", "JWT " + fragmentToken);
                                                                                    return params;
                                                                                }
                                                                            };

                                                                            ((MapsActivity) getActivity()).queue.add(deleteCommentRequest);
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        return;
                                                                    }
                                                                });
                                                            }//get rid of delete button
                                                            else{
                                                                comment.findViewById(R.id.delete_comment_button).setVisibility(View.GONE);
                                                            }
                                                            commentSectionList.addView(comment);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("BubbleUp", " : Bubble Comments Response Error! " + error.getMessage());
                                    }
                                }) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> params = new HashMap();
                                        params.put("Authorization", "JWT " + fragmentToken);
                                        return params;
                                    }
                                };

                                ((MapsActivity) getActivity()).queue.add(getComments);
                            } else {
                                postCardVertical.removeView(commentSection);
                            }
                        }
                    });
                }


                Button hideCommentsButton = (Button) commentSection.findViewById(R.id.hide_comment_button);
                hideCommentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postCardVertical.removeView(commentSection);

                        toggleComments.setChecked(false);

                    }
                });

                /*
                container.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });
                */

                //Like / Dislike Buttons
                ToggleButton like_button = (ToggleButton) container.findViewById(R.id.toggleButton_like);

                if(currentBubble.mySource == 1) {
                    like_button.setVisibility(View.INVISIBLE);
                } else {
                    if(currentBubble.userReaction == 1){
                        like_button.toggle();
                        like_button.setBackground(getResources().getDrawable(R.drawable.ic_action_like_on));
                    }

                    like_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //Button Animation
                            buttonView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.reaction_button_animation));
                            if (isChecked) {
                                //Like request
                                StringRequest registerRequest = new StringRequest(Request.Method.POST, url_like + myPost_id,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                //Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
                                                currentBubble.userReaction = 1;
                                                currentBubble.myLikes++;
                                                likeCounter.setText(Integer.toString(currentBubble.myLikes));
                                                container.findViewById(R.id.toggleButton_like).setBackground(getResources().getDrawable(R.drawable.ic_action_like_on));
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(), "Response Error", Toast.LENGTH_SHORT).show();
                                    }
                                }){
                                    @Override
                                    public byte[] getBody() throws AuthFailureError {
                                        String httpPostBody="{\"posts_id\": \"" + myPost_id + "\"}";
                                        return httpPostBody.getBytes();
                                    }
                                    @Override
                                    public Map<String,String> getParams(){
                                        Map<String, String> params = new HashMap();
                                        params.put("posts_id","\"" + myPost_id + "\"");
                                        return params;
                                    }
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String,String> params = new HashMap();
                                        params.put("Content-Type","application/json");
                                        params.put("Authorization", "JWT " + fragmentToken);
                                        return params;
                                    }
                                };

                                ((MapsActivity) getActivity()).queue.add(registerRequest);
                            } else {
                                //Like request
                                StringRequest registerRequest = new StringRequest(Request.Method.DELETE, url_like + myPost_id,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                //Toast.makeText(getContext(), "unliked", Toast.LENGTH_SHORT).show();
                                                currentBubble.userReaction = 0;
                                                currentBubble.myLikes--;
                                                likeCounter.setText(Integer.toString(currentBubble.myLikes));
                                                container.findViewById(R.id.toggleButton_like).setBackground(getResources().getDrawable(R.drawable.ic_action_like));
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(), "Response Error", Toast.LENGTH_SHORT).show();
                                    }
                                }){
                                    @Override
                                    public byte[] getBody() throws AuthFailureError {
                                        String httpPostBody="{\"posts_id\": \"" + myPost_id + "\"}";
                                        return httpPostBody.getBytes();
                                    }
                                    @Override
                                    public Map<String,String> getParams(){
                                        Map<String, String> params = new HashMap();
                                        params.put("posts_id","\"" + myPost_id + "\"");
                                        return params;
                                    }
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String,String> params = new HashMap();
                                        params.put("Content-Type","application/json");
                                        params.put("Authorization", "JWT " + fragmentToken);
                                        return params;
                                    }
                                };

                                ((MapsActivity) getActivity()).queue.add(registerRequest);
                            }
                        }
                    });
                }

                if(currentBubble.profile_image != null) {
                    userImage.setImageBitmap(currentBubble.profile_image);
                    userImage.setBackgroundResource(0);
                }else {
                    //TODO: Move bubble src to the foreground, and make image the src.
                    switch (userName) {
                        case "User#1":
                            userImage.setColorFilter(Color.parseColor("#ff9555"));
                            ;//setBackgroundColor(Color.parseColor("#ff9555"));
                            break;
                        case "User#2":
                            userImage.setColorFilter(Color.parseColor("#9044D3"));//setBackgroundColor(Color.parseColor("#9044D3"));
                            break;
                        case "User#9":
                            userImage.setColorFilter(Color.parseColor("#EA2F7E"));//setBackgroundColor(Color.parseColor("#EA2F7E"));
                            break;
                        default:
                            userImage.setColorFilter(Color.parseColor("#28E1D3"));//setBackgroundColor(Color.parseColor("#28E1D3"));
                            break;
                    }
                }

                myList.addView(container);
                container.setTag(currentBubble.myUser_id);
                if(currentBubble.profile_image == null || currentBubble.username == null){
                    emptyUserData.add(container);
                }
                countBubble++;
            }
        }

        int density = (int) this.getResources().getDisplayMetrics().density;
        Space endOfList = new Space(getActivity());
        endOfList.setMinimumHeight(50*density);
        myList.addView(endOfList);
        //Toast.makeText(getActivity(), "Bubbles " + countBubble, Toast.LENGTH_SHORT).show();
    }

    public void updateUserCard(){
        if(emptyUserData != null){
            for(View userCard : new ArrayList<View>(emptyUserData)){
                MapsActivity mA = (MapsActivity) getActivity();
                Bitmap profileImage = mA.profilePictureStorageBitmap.get( (Integer) userCard.getTag() );
                String profileUserName = mA.profileNameStorage.get( (Integer) userCard.getTag() );
                Log.d("BubbleUP","Attempting to update container with tag: " + userCard.getTag());

                if(profileImage != null && profileUserName != null){
                    emptyUserData.remove(userCard);
                }
                if(profileImage != null){
                    ((ImageButton) userCard.findViewById(R.id.imageButton)).setBackgroundResource(0);
                    ((ImageButton) userCard.findViewById(R.id.imageButton)).setColorFilter(0);
                    ((ImageButton) userCard.findViewById(R.id.imageButton)).setImageBitmap(profileImage);
                }
                if(profileUserName != null){
                    ((TextView) userCard.findViewById(R.id.textViewUserName)).setText(profileUserName);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showProfile(final int myUserId, final int queryUserId, final boolean display, final boolean showUserPosts){
        if(display){ //If true we display, if false we eliminate the view
            profileView = myInflater.inflate(R.layout.activity_user_settings, myViewGroupContainer, false);

            profileContainer = (LinearLayout) myView.findViewById(R.id.linear_view);

            if(profileContainer.getChildCount() > 0){
                profileContainer.removeAllViews();
            }

            profileContainer.addView(profileView, 0);

            ///////////////////////////
            //Code From User Settings//
            ///////////////////////////
            final Button edit_profile = (Button) profileContainer.findViewById(R.id.button_set_username);
            int myId = myUserId;
            final int userId = queryUserId;

            //checking to see if the profile is that of the current user's, if not then we hide the edit button
            if(currentUserId != userId){
                edit_profile.setVisibility(View.GONE);
            }

            final TextView display_username = (TextView) profileContainer.findViewById((R.id.textView));

            final EditText display_username_edit = (EditText) profileContainer.findViewById(R.id.textViewEditable);
            display_username_edit.setVisibility(View.GONE);

            final TextView display_bio = (TextView) profileContainer.findViewById(R.id.textViewBio);

            final EditText display_bio_edit = (EditText) profileContainer.findViewById(R.id.textViewBioEditable);
            display_bio_edit.setVisibility(View.GONE);

            final ImageButton profpic = (ImageButton) profileContainer.findViewById(R.id.imageButton2);

            final Button showUserPostsButton = (Button) profileContainer.findViewById(R.id.toggleButtonUserPosts);

            final Button showUserLikesButton = (Button) profileContainer.findViewById(R.id.toggleButtonUserLikes);

            if(myId == userId){
                //Getting user BIO
                //TODO: Tell dilesh to make a more general get method
                StringRequest user_bio_link_request = new StringRequest(Request.Method.GET, url_bio,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    //We get back an array with data for the requested IDs
                                    JSONObject myJson = new JSONObject(response);
                                    String bio = myJson.getString("user_bio");
                                    if(bio!=""){
                                        display_bio.setText(bio);
                                    }else{
                                        display_bio.setText("<Empty Bio>");
                                    }

                                } catch (JSONException e) {
                                    Log.d("BubbleUp", "JSON IDs GET problem!");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("BubbleUp", "ID get JSOn Response Error! " + error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap();
                        headers.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                        return headers;
                    }
                };

                ((MapsActivity) getActivity()).queue.add(user_bio_link_request);

                ///////////////////
                //Profile Editing//
                ///////////////////

                final int finalMyId = myId;
                edit_profile.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(View v) {
                        if(edit_settings_clicked){
                            edit_profile.setBackgroundResource(android.R.drawable.ic_menu_save);
                            edit_settings_clicked = false;
                            //Toast.makeText(getApplicationContext(), "Click on what you wish to edit", Toast.LENGTH_SHORT).show();

                            display_username.setVisibility(View.GONE);
                            display_username_edit.setText(display_username.getText().toString());
                            display_username_edit.setVisibility(View.VISIBLE);

                            display_bio.setVisibility(View.GONE);
                            display_bio_edit.setText(display_bio.getText().toString());
                            display_bio_edit.setVisibility(View.VISIBLE);

                            //change profile picture listener
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                profpic.setForeground(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_action_edit_profpic));
                                profpic.setForegroundGravity(Gravity.CENTER);
                            }
                            profpic.setBackgroundResource(R.drawable.edit_background);
                            profpic.setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onClick(View v){
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                                    LinearLayout layout = new LinearLayout(getActivity());
                                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    layout.setLayoutParams(parms);

                                    layout.setGravity(Gravity.CLIP_VERTICAL);
                                    layout.setPadding(2, 2, 2, 2);

                                    textEdit = new EditText(getActivity());

                                    layout.addView(textEdit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                                    alertDialogBuilder.setView(layout);
                                    alertDialogBuilder.setIcon(getResources().getDrawable(R.drawable.ic_action_edit_name));
                                    alertDialogBuilder.setTitle("Input your new profile pic link!");

                                    // alertDialogBuilder.setMessage(message);
                                    alertDialogBuilder.setCancelable(false);

                                    // Setting Negative "Cancel" Button
                                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    });

                                    // Setting Positive "OK" Button
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {


                                            final String newLink = textEdit.getText().toString();

                                            if (newLink == null || newLink.equals("")) {
                                                Toast.makeText(getActivity(), "Empty Link!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_profile_pic,
                                                        new Response.Listener<String>() {
                                                            @Override
                                                            public void onResponse(String response) {

                                                                //settings.edit().putString("profile_link", newLink).commit();

                                                                class fetchProfImageAsync extends AsyncTask<String, Void, Bitmap> {
                                                                    @Override
                                                                    protected Bitmap doInBackground(String... params) {
                                                                        String url = params[0];

                                                                        Bitmap profile_image;
                                                                        try {
                                                                            Log.d("BubbleUp", "Trying profile picture fetch. From: " + url);
                                                                            profile_image = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                                                                            Log.d("BubbleUp", "Success profile picture fetch. From: " + url);
                                                                        } catch (Exception e) {
                                                                            Log.d("BubbleUp", "Profile picture fetch failed. " + e.toString());
                                                                            profile_image = null;
                                                                        }

                                                                        return profile_image;
                                                                    }

                                                                    @Override
                                                                    protected void onPostExecute(Bitmap usr_image) {
                                                                        if(usr_image != null) {
                                                                            //If an image has been fetched successfully then we store it on a table using the user ID as the key.
                                                                            profpic.setImageBitmap(usr_image);
                                                                            profpic.setBackgroundResource(0);
                                                                            ((MapsActivity) getActivity()).profilePictureStorageBitmap.put(finalMyId,usr_image);
                                                                            ((MapsActivity) getActivity()).reload_button.performClick();
                                                                        }
                                                                    }
                                                                }

                                                                fetchProfImageAsync fetcher_reload = new fetchProfImageAsync();
                                                                fetcher_reload.execute(newLink);

                                                                Log.d("BubbleUp", "Got profile link");
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        Log.d("BubbleUp", error.toString());
                                                        Toast.makeText(getActivity(), "Communication Error!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }) {
                                                    @Override
                                                    public byte[] getBody() throws AuthFailureError {
                                                        String httpPostBody = "{\"profile_image\": \"" + newLink + "\"}";
                                                        return httpPostBody.getBytes();
                                                    }

                                                    @Override
                                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                                        Map<String, String> params = new HashMap();
                                                        params.put("Content-Type", "application/json");
                                                        params.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                                                        return params;
                                                    }
                                                };

                                                Log.d("BubbleUp", "Requesting Link Set");
                                                ((MapsActivity) getActivity()).queue.add(loginRequest);
                                            }


                                        }
                                    });

                                    AlertDialog alertDialog = alertDialogBuilder.create();

                                    try {
                                        alertDialog.show();
                                        Log.d("BubbleUp","Dialog Success");
                                    } catch (Exception e) {
                                        // WindowManager$BadTokenException will be caught and the app would
                                        // not display the 'Force Close' message
                                        e.printStackTrace();
                                        Log.d("BubbleUp","Dialog Fail");
                                        Log.d("BubbleUp",e.toString());
                                        Log.d("BubbleUp",e.getLocalizedMessage());
                                    }
                                    edit_settings_clicked = true;
                                    edit_profile.setBackgroundResource(R.drawable.ic_action_edit_name);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        profpic.setForeground(null);
                                    }
                                    display_username.setVisibility(View.VISIBLE);
                                    display_username_edit.setVisibility(View.GONE);
                                    display_bio.setVisibility(View.VISIBLE);
                                    display_bio_edit.setVisibility(View.GONE);
                                    profpic.setBackground(null);
                                    profpic.setOnClickListener(null);

                                }
                            });

                        }
                        else{
                            edit_settings_clicked = true;
                            edit_profile.setBackgroundResource(R.drawable.ic_action_edit_name);
                            //Toast.makeText(getApplicationContext(), "Edit canceled", Toast.LENGTH_SHORT).show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                profpic.setForeground(null);
                            }

                            //checking for valid username change
                            if(!display_username_edit.getText().toString().equals("") && !display_username_edit.getText().toString().equals(display_username.getText().toString())) {

                                StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_name,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast.makeText(getActivity(), "Changed Name to: " + display_username_edit.getText().toString(), Toast.LENGTH_SHORT).show();

                                                //settings.edit().putString("saved_username", newUserName).commit();

                                                display_username.setText(display_username_edit.getText().toString());

                                                Log.d("BubbleUp", "Success Username Change: " + response);
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("BubbleUp", "Unsuccessful change");
                                        Toast.makeText(getActivity(), "Unsuccessful Name Change", Toast.LENGTH_SHORT).show();

                                    }
                                }) {
                                    @Override
                                    public byte[] getBody() throws AuthFailureError {
                                        String httpPostBody = "{\"name\": \"" + display_username_edit.getText().toString() + "\"}";
                                        return httpPostBody.getBytes();
                                    }

                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> params = new HashMap();
                                        params.put("Content-Type", "application/json");
                                        params.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                                        return params;
                                    }
                                };

                                ((MapsActivity) getActivity()).queue.add(loginRequest);
                            } else {
                                if(display_username_edit.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Empty Name", Toast.LENGTH_SHORT).show();
                                }
                            }

                            //checking for valid bio change
                            if(!display_bio_edit.getText().toString().equals("") && !display_bio_edit.getText().toString().equals(display_bio.getText().toString())) {

                                StringRequest loginRequest = new StringRequest(Request.Method.PUT, url_set_bio,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                Toast.makeText(getActivity(), "Changed Bio to: " + display_bio_edit.getText().toString(), Toast.LENGTH_SHORT).show();

                                                //TODO: add saved_bio to settings
                                                //settings.edit().putString("saved_bio", newUserBio).commit();

                                                display_bio.setText(display_bio_edit.getText().toString());

                                                Log.d("BubbleUp", "Success Username Change: " + response);
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("BubbleUp", "Unsuccessful change");
                                        Toast.makeText(getActivity(), "Unsuccessful Bio Change", Toast.LENGTH_SHORT).show();

                                    }
                                }) {
                                    @Override
                                    public Map<String, String> getParams() throws AuthFailureError {
                                        HashMap<String, String> params = new HashMap<>();
                                        params.put("user_bio", "\"" + display_bio_edit.getText().toString() + "\"");
                                        return params;
                                    }

                                    @Override
                                    public byte[] getBody() throws AuthFailureError {
                                        String httpPostBody = "{\"user_bio\": \"" + display_bio_edit.getText().toString() + "\"}";
                                        return httpPostBody.getBytes();
                                    }

                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> params = new HashMap();
                                        params.put("Content-Type", "application/json");
                                        params.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                                        return params;
                                    }
                                };

                                ((MapsActivity) getActivity()).queue.add(loginRequest);
                            } else {
                                if(display_bio_edit.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Empty Bio", Toast.LENGTH_SHORT).show();
                                }
                            }

                            display_username.setVisibility(View.VISIBLE);
                            display_username_edit.setVisibility(View.GONE);
                            display_bio.setVisibility(View.VISIBLE);
                            display_bio_edit.setVisibility(View.GONE);
                            profpic.setBackground(null);
                            profpic.setOnClickListener(null);
                        }
                    }
                });
            } else {
                //request for id posts and info
                myId = queryUserId;

                StringRequest user_bio_link_request = new StringRequest(Request.Method.GET, url_bio + "/" + myId,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    //We get back an array with data for the requested IDs
                                    JSONObject myJson = new JSONObject(response);
                                    String bio = myJson.getString("user_bio");
                                    if(bio!=""){
                                        display_bio.setText(bio);
                                    }else{
                                        display_bio.setText("<Empty Bio>");
                                    }

                                } catch (JSONException e) {
                                    Log.d("BubbleUp", "JSON IDs GET problem!");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("BubbleUp", "ID get JSOn Response Error! " + error.toString());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap();
                        headers.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                        return headers;
                    }
                };

                ((MapsActivity) getActivity()).queue.add(user_bio_link_request);
            }

            final int reqId = myId;

            if( ((MapsActivity) getActivity()).profileNameStorage.get(reqId) != null){
                display_username.setText(((MapsActivity) getActivity()).profileNameStorage.get(myId));
            }

            if( ((MapsActivity) getActivity()).profilePictureStorageBitmap.get(reqId) != null ){
                profpic.setImageBitmap(((MapsActivity) getActivity()).profilePictureStorageBitmap.get(myId));
            }

            if(showUserPosts){
                StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_users_posts + reqId,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("BubbleUp", "JSOn Post Get Response Successful for id = " + reqId);
                                Log.d("BubbleUp", response);

                                try {
                                    //We convert the response into an JSONArray object so as to iterate through the posts.
                                    JSONArray json_response = new JSONArray(response.toString());

                                    List<BubbleMarker> myBubbles = new ArrayList<>();
                                    //myBubbles.clear();//empty the array first.

                                    //Iterating through the JSON object array.
                                    for (int i = 0; i < json_response.length(); i++)
                                        ((MapsActivity) getActivity()).jsonToBubbleMarker((JSONObject) json_response.get(i), myBubbles, true);

                                    sendToFragment(myBubbles, null, false, 1);

                                } catch (JSONException e) {
                                    Log.d("BubbleUp", "JSON object problem!");
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
                        params.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                        return params;
                    }
                };
                ((MapsActivity) getActivity()).queue.add(tokenRequest);

                //Buttons to show Likes or Posts
                showUserPostsButton.setOnClickListener(null);
                showUserPostsButton.setBackground(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_light_normal_background));

                showUserLikesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showProfile(myUserId, queryUserId, display, false);
                    }
                });
            }
            else{
                StringRequest tokenRequest = new StringRequest(Request.Method.GET, url_users_likes + reqId,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("BubbleUp", "JSOn Post Get Response Successful for id = " + reqId);
                                Log.d("BubbleUp", response);

                                try {
                                    //We convert the response into an JSONArray object so as to iterate through the posts.
                                    JSONArray json_response = new JSONArray(response.toString());

                                    List<BubbleMarker> myBubbles = new ArrayList<>();
                                    //myBubbles.clear();//empty the array first.

                                    //Iterating through the JSON object array.
                                    for (int i = 0; i < json_response.length(); i++)
                                        ((MapsActivity) getActivity()).jsonToBubbleMarker((JSONObject) json_response.get(i), myBubbles, true);

                                    sendToFragment(myBubbles, null, false, 1);

                                } catch (JSONException e) {
                                    Log.d("BubbleUp", "JSON object problem!");
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
                    public Map<String,String> getParams(){
                        Map<String, String> params = new HashMap();
                        params.put("id","\"" + reqId + "\"");
                        return params;
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        String httpPostBody="{\"id\": \"" + reqId + "\"}";
                        // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
                        return httpPostBody.getBytes();
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap();
                        params.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                        return params;
                    }
                };
                ((MapsActivity) getActivity()).queue.add(tokenRequest);

                //Buttons to show Likes or Posts
                showUserPostsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showProfile(myUserId, queryUserId, display, true);
                    }
                });

                showUserLikesButton.setOnClickListener(null);
                showUserLikesButton.setBackground(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_light_normal_background));
            }

        } else { //If false we eliminate the view
            profileContainer.removeView(profileView);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showUserOptions(final int myUserId, final int[] queryUserIds, String s){
        profileContainer = (LinearLayout) myView.findViewById(R.id.linear_view);

        profileContainer.removeAllViews();


        View header = myInflater.inflate(R.layout.search_results_header, profileContainer, false);

        profileContainer.addView(header, 0);

        TextView userSearch = (TextView) profileContainer.findViewById(R.id.headerTextViewSearch);
        TextView before = (TextView) profileContainer.findViewById(R.id.headerTextViewBefore);
        TextView after = (TextView) profileContainer.findViewById(R.id.headerTextViewAfter);
        if(queryUserIds[0] != -1){
            before.setText("Users with ");
            userSearch.setText(s);
            after.setText(" in their name");
            userSearch.setTextColor(getResources().getColor(R.color.main_color));
        }
        else{
            before.setText("No users found with ");
            userSearch.setText(s);
            after.setText(" in their name");
            userSearch.setTextColor(getResources().getColor(R.color.main_color));
        }

        for(int i = 0; i < queryUserIds.length; i++) {

            if(queryUserIds[i] == -1){
                i = queryUserIds.length;
            }
            else{
                profileView = myInflater.inflate(R.layout.search_query_user_option, profileContainer, false);

                profileContainer.addView(profileView, 1);

                ///////////////////////////
                //Code From User Settings//
                ///////////////////////////
                int myId = myUserId;
                final int userId = queryUserIds[i];

                final TextView display_username = (TextView) profileContainer.findViewById((R.id.textView));
                display_username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MapsActivity) getActivity()).profile_display = true;
                        ((MapsActivity) getActivity()).sorting_spinner.setVisibility(View.GONE);
                        showProfile( ((MapsActivity) getActivity()).myId, userId, true, true);
                    }
                });

                final TextView display_bio = (TextView) profileContainer.findViewById(R.id.textViewBio);

                final ImageButton profpic = (ImageButton) profileContainer.findViewById(R.id.imageButton2);
                profpic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MapsActivity) getActivity()).profile_display = true;
                        ((MapsActivity) getActivity()).sorting_spinner.setVisibility(View.GONE);
                        showProfile( ((MapsActivity) getActivity()).myId, userId, true, true);
                    }
                });

                if (myId == userId) {
                    //Getting user BIO
                    //TODO: Tell dilesh to make a more general get method
                    StringRequest user_bio_link_request = new StringRequest(Request.Method.GET, url_bio,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        //We get back an array with data for the requested IDs
                                        JSONObject myJson = new JSONObject(response);
                                        String bio = myJson.getString("user_bio");
                                        if (bio != "") {
                                            display_bio.setText(bio);
                                        } else {
                                            display_bio.setText("<Empty Bio>");
                                        }

                                    } catch (JSONException e) {
                                        Log.d("BubbleUp", "JSON IDs GET problem!");
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("BubbleUp", "ID get JSOn Response Error! " + error.toString());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap();
                            headers.put("Authorization", "JWT " + ((MapsActivity) getActivity()).token);
                            return headers;
                        }
                    };

                    ((MapsActivity) getActivity()).queue.add(user_bio_link_request);

                    ///////////////////
                    //Profile Editing//
                    ///////////////////


                } else {
                    //request for id posts and info
                    myId = queryUserIds[i];
                }

                final int reqId = myId;

                if (((MapsActivity) getActivity()).profileNameStorage.get(reqId) != null) {
                    display_username.setText(((MapsActivity) getActivity()).profileNameStorage.get(myId));
                }

                if (((MapsActivity) getActivity()).profilePictureStorageBitmap.get(reqId) != null) {
                    profpic.setImageBitmap(((MapsActivity) getActivity()).profilePictureStorageBitmap.get(myId));
                }
            }
        }

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContentFragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (getActivity() instanceof MapsActivity) {
            fragmentToken = ((MapsActivity) getActivity()).token;
        }
        saved_settings = ((MapsActivity) getActivity()).getSharedPreferences(SAVEDLOCATION_PREF, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myInflater = inflater;
        myView = inflater.inflate(R.layout.bubble_data, container, false);

        final Button scrollUpButton = (Button) myView.findViewById(R.id.scrollUp);
        scrollUpButton.setVisibility(View.GONE);

        final ScrollView scrollStuff = (ScrollView) myView.findViewById(R.id.scrollView2);

        //TODO:get rid of this error, not sure if it should be annotated or surrounded by if statement
        scrollStuff.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if(i1 != 0){
                    scrollUpButton.setVisibility(View.VISIBLE);
                }else {
                    scrollUpButton.setVisibility(View.GONE);
                }
            }
        });

        scrollUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollStuff.smoothScrollTo(0,0);
            }
        });

        return myView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static String getMonthString(int monthNum){
        switch (monthNum){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sept";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "this should not happen";
        }
    }
}
