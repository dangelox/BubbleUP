package com.example.bubbleup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ComposerDialogFragment extends DialogFragment {

    String snippet = "";

    String url_tasks ="https://bubbleup-api.herokuapp.com/posts";

    LatLng postLatLng;

    int type = 0;

    public void setLatLng(LatLng latlng){
        postLatLng = latlng;
    }

    View previousButton;

    ImageView selectedTopic;

    boolean didIPost;

    View.OnClickListener typeButtonLister = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.category_button_music:
                    type = 1;
                    break;
                case R.id.category_button_photo:
                    type = 2;
                    break;
                case R.id.category_button_video:
                    type = 3;
                    break;
                case R.id.category_button_animals:
                    type = 4;
                    break;
                case R.id.category_button_cloud:
                    type = 5;
                    break;
                case R.id.category_button_economy:
                    type = 6;
                    break;
                case R.id.category_button_shopping:
                    type = 7;
                    break;
                case R.id.category_button_question:
                    type = 8;
                    break;
                case R.id.category_button_umbrella:
                    type = 9;
                default:
                    type = 0;
                    break;
            }
            previousButton = view;
            selectedTopic.setBackground(((ImageButton) view).getDrawable());
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        didIPost = false;
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View fragmentView = inflater.inflate(R.layout.fragment_post_composer, null);

        builder.setView(fragmentView);

        final Activity myActivity = getActivity();

        selectedTopic = (ImageView) fragmentView.findViewById(R.id.category_selected);

        //Setting button listeners
        ImageButton pickerButton0 = (ImageButton) fragmentView.findViewById(R.id.category_button_post);
        pickerButton0.setOnClickListener(typeButtonLister);
        ImageButton pickerButton1 = (ImageButton) fragmentView.findViewById(R.id.category_button_music);
        pickerButton1.setOnClickListener(typeButtonLister);
        ImageButton pickerButton2 = (ImageButton) fragmentView.findViewById(R.id.category_button_photo);
        pickerButton2.setOnClickListener(typeButtonLister);
        ImageButton pickerButton3 = (ImageButton) fragmentView.findViewById(R.id.category_button_video);
        pickerButton3.setOnClickListener(typeButtonLister);
        ImageButton pickerButton4 = (ImageButton) fragmentView.findViewById(R.id.category_button_animals);
        pickerButton4.setOnClickListener(typeButtonLister);
        ImageButton pickerButton5 = (ImageButton) fragmentView.findViewById(R.id.category_button_cloud);
        pickerButton5.setOnClickListener(typeButtonLister);
        ImageButton pickerButton6 = (ImageButton) fragmentView.findViewById(R.id.category_button_economy);
        pickerButton6.setOnClickListener(typeButtonLister);
        ImageButton pickerButton7 = (ImageButton) fragmentView.findViewById(R.id.category_button_shopping);
        pickerButton7.setOnClickListener(typeButtonLister);
        ImageButton pickerButton8 = (ImageButton) fragmentView.findViewById(R.id.category_button_question);
        pickerButton8.setOnClickListener(typeButtonLister);
        ImageButton pickerButton9 = (ImageButton) fragmentView.findViewById(R.id.category_button_umbrella);
        pickerButton9.setOnClickListener(typeButtonLister);

        //this is for the color bar to change the icon color
        /*SeekBar colorPicker = (SeekBar) fragmentView.findViewById(R.id.seekBar);
        colorPicker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //range of progress should be 0 t 24 bit
                selectedTopic.setBackgroundColor(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ComposerDialogFragment.this.getDialog().cancel();
            }
        });

        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                EditText composer = (EditText) fragmentView.findViewById(R.id.composer_edit);

                snippet = composer.getText().toString();

                if(snippet.equals("")){
                    Toast.makeText(getContext(), "Empty Post", Toast.LENGTH_SHORT).show();
                } else {
                    didIPost = true;
                    //Toast.makeText(getContext(), snippet, Toast.LENGTH_SHORT).show();

                    StringRequest bubblePostRequest = new StringRequest(Request.Method.POST, url_tasks,
                            new Response.Listener<String>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        Log.d("BubbleUp", "Post Taks Response:\n" + response.toString());
                                        JSONObject json_response = new JSONObject(response.toString());

                                        ((MapsActivity) myActivity).jsonToBubbleMarker(json_response, ((MapsActivity) myActivity).myBubbles,false);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("BubbleUp","Post Task Error: " + error.toString());
                            Log.d("BubbleUp","Post Task Error: " + error.getMessage());
                        }
                    }){
                        @Override
                        public Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("body", "\"" + snippet + "\"");
                            params.put("visible", "1");
                            params.put("lat", Double.toString( postLatLng.latitude));
                            params.put("lng", Double.toString( postLatLng.longitude));
                            return params;
                        }
                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            String httpPostBody="{\"body\": \""+snippet+"\", " +
                                    "\"visible\": 1," +
                                    " \"lat\": " + Double.toString( postLatLng.latitude) + "," +
                                    " \"lng\": " + Double.toString( postLatLng.longitude) + "," +
                                    " \"content_type\": " + Integer.toString(type) + "}";
                            Log.d("BubbleUp", "HTTPPOST BODY:\n" + httpPostBody);
                            return httpPostBody.getBytes();
                        }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap();
                            Log.d("BubbleUp",((MapsActivity) getActivity()).token);
                            params.put("Authorization","JWT " + ((MapsActivity) getActivity()).token);
                            params.put("Content-Type","application/json");
                            return params;
                        }
                    };

                    ((MapsActivity) getActivity()).queue.add(bubblePostRequest);
                }
            }
        });

        return builder.create();
    }
}
