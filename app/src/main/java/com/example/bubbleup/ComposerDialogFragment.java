package com.example.bubbleup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

    public void setLatLng(LatLng latlng){
        postLatLng = latlng;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View fragmentView = inflater.inflate(R.layout.fragment_post_composer, null);

        builder.setView(fragmentView);

        final Activity myActivity = getActivity();

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ComposerDialogFragment.this.getDialog().cancel();
                    }
                })
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        EditText composer = (EditText) fragmentView.findViewById(R.id.composer_edit);

                        snippet = composer.getText().toString();

                        if(snippet.equals("")){
                            Toast.makeText(getContext(), "Empty Post", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(getContext(), snippet, Toast.LENGTH_SHORT).show();

                            StringRequest bubblePostRequest = new StringRequest(Request.Method.POST, url_tasks,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                Log.d("BubbleUp", "Post Taks Response:\n" + response.toString());
                                                JSONObject json_response = new JSONObject(response.toString());

                                                ((MapsActivity) myActivity).jsonToBubbleMarker(json_response, false);
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
                                            " \"lng\": " + Double.toString( postLatLng.longitude) + "}";
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
