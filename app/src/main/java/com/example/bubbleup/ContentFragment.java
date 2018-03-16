package com.example.bubbleup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
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

import java.util.Collections;
import java.util.Comparator;
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
    String url_comments ="https://bubbleup-api.herokuapp.com/posts_comments/";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String fragmentToken;

    private OnFragmentInteractionListener mListener;

    View myView;

    LayoutInflater myInflater;

    int currentUserId;

    SharedPreferences saved_settings;

    public ContentFragment() {
        // Required empty public constructor
    }

    //Comparators for sorting
    public class BubbleComparatorSize implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) right.myHeight).compareTo((Integer) left.myHeight);
        }
    }

    public class BubbleComparatorAgeMins implements Comparator<BubbleMarker> {
        public int compare(BubbleMarker left, BubbleMarker right) {
            return ((Integer) right.myAgeMins).compareTo((Integer) left.myAgeMins);
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
    public void sendToFragment(List<BubbleMarker> bubbleList, LatLngBounds bounds){
        final LinearLayout myList = (LinearLayout) myView.findViewById(R.id.linear_view);

        myList.setBackgroundColor(Color.parseColor(saved_settings.getString("backGround_Color","#f2f2f2")));

        //Sorting by size
        Collections.sort(bubbleList, new BubbleComparatorSize());

        //Creating a new view for each bubble
        for (final BubbleMarker currentBubble : bubbleList) {
            if(bounds == null || bounds.contains(currentBubble.bubbleMarker.getPosition())){
                Log.d("BubbleUp_Fragment",currentBubble.msg);

                final View container = myInflater.inflate(R.layout.fragment_post_container, myList, false);

                final LinearLayout body = (LinearLayout) container.findViewById(R.id.fragment_body_linear);

                final TextView myCommentCounter = (TextView) container.findViewById(R.id.comment_counter);
                myCommentCounter.setText(Integer.toString(currentBubble.myCommentCount));

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
                likeCounter.setText(Integer.toString(currentBubble.myLikes));

                final int myPost_id = currentBubble.myPost_id;

                //final BubbleMarker bubble = currentBubble;

                TextView userNameText = (TextView) container.findViewById(R.id.textViewUserName);
                userNameText.setText(currentBubble.username);

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
                else{
                    ageOfPostText.setText(String.valueOf(currentBubble.myAgeDays).concat("d"));
                }

                String userName = currentBubble.bubbleMarkerOption.getTitle().substring(0, Math.min(currentBubble.bubbleMarkerOption.getTitle().length(), 6));

                ImageButton userImage = (ImageButton) container.findViewById(R.id.imageButton);
                userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_intent = new Intent(getActivity(), UserSettings.class);
                        profile_intent.putExtra("myId", ((MapsActivity) getActivity()).myId);
                        profile_intent.putExtra("userId", currentBubble.myUser_id);
                        startActivity(profile_intent);
                    }
                });

                Button deleteButton = (Button) container.findViewById(R.id.deleteButton);

                //check if the bubble is the current user's bubble and should show the deletion button
                if(getActivity() instanceof UserSettings){
                    currentUserId = ((UserSettings) getActivity()).myId;
                } else if (getActivity() instanceof MapsActivity){
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
                                                        
                                                        if(getActivity() instanceof UserSettings){
                                                            ((UserSettings) getActivity()).myBubbles.remove(currentBubble);
                                                        } else if (getActivity() instanceof MapsActivity){
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
                                                        if(getActivity() instanceof UserSettings){
                                                            currentUserId = ((UserSettings) getActivity()).myId;
                                                        } else if (getActivity() instanceof MapsActivity){
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

                //User Reaction Values (Bound to change)
                //1 = Like
                //2 = Dislike
                //3 = ??
                if(currentBubble.userReaction == 1){
                    like_button.toggle();
                    like_button.setBackground(getResources().getDrawable(R.drawable.ic_action_like_on));
                }

                like_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Button Animation
                        buttonView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.reaction_button_animation));
                        if (isChecked) {
                            ToggleButton otherButton = (ToggleButton) container.findViewById(R.id.toggleButton_dislike);
                            if(otherButton.isChecked())
                                otherButton.toggle();

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

                ToggleButton dislike_button = (ToggleButton) container.findViewById(R.id.toggleButton_dislike);

                if(currentBubble.userReaction == 2){
                    dislike_button.toggle();
                    dislike_button.setBackground(getResources().getDrawable(R.drawable.ic_action_dislike_on));
                }

                dislike_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            ToggleButton otherButton = (ToggleButton) container.findViewById(R.id.toggleButton_like);
                            if(otherButton.isChecked())
                                otherButton.toggle();
                            buttonView.setBackground(getResources().getDrawable(R.drawable.ic_action_dislike_on));

                        } else {
                            buttonView.setBackground(getResources().getDrawable(R.drawable.ic_action_dislike));
                        }
                    }
                });


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

        if(getActivity() instanceof UserSettings){
            fragmentToken = ((UserSettings) getActivity()).saved_token;
        } else if (getActivity() instanceof MapsActivity) {
            fragmentToken = ((MapsActivity) getActivity()).token;
        }
        saved_settings = ((MapsActivity) getActivity()).getSharedPreferences(SAVEDLOCATION_PREF, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myInflater = inflater;
        myView = inflater.inflate(R.layout.bubble_data, container, false);

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
}
