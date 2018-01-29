package com.example.bubbleup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

//This Class should also upload the composed bubble to the database.
public class AddMarkerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_marker);

        //no longer a feature
        //final SeekBar colorBar = (SeekBar) findViewById(R.id.seekBar);

        final LatLng latlng = (LatLng) getIntent().getParcelableExtra("location");

        //getting user input title
        final EditText userTitle = (EditText) findViewById(R.id.editTitle);

        final EditText userSnippet = (EditText) findViewById(R.id.editSnippet);

        Button markerButton = (Button) findViewById(R.id.createMarkerButton);
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String snipet = "";
                String tittle = "";

                BubbleMarker myBubble = new BubbleMarker(latlng, "", "", 320, 320, getApplicationContext());//Draws a bubble near lawrence

                if(userSnippet.getText() != null){
                    myBubble.bubbleMarkerOption.snippet(userSnippet.getText().toString());
                    myBubble.msg = userSnippet.getText().toString();
                    snipet = userSnippet.getText().toString();
                }
                //set the title to the input from user
                if (userTitle.getText() != null) {
                    myBubble.bubbleMarkerOption.title(userTitle.getText().toString());
                    myBubble.tittle = userTitle.getText().toString();
                    tittle = userTitle.getText().toString();
                }

                Intent resultIntent = new Intent();
                //resultIntent.putExtra("marker", myBubble);//TODO: Figure out if there is a way to send objects beteen activities.
                resultIntent.putExtra("latlng",latlng);
                resultIntent.putExtra("snipet", snipet);
                resultIntent.putExtra("tittle", tittle);

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
