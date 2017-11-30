package com.example.bubbleup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.MarkerOptions;

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
                MarkerOptions marker = new MarkerOptions().position(latlng);

                if(userSnippet.getText() != null){
                    marker.snippet(userSnippet.getText().toString());
                }
                //set the title to the input from user
                if (userTitle.getText() != null) {
                    marker.title(userTitle.getText().toString());
                }

                //markers have info window to customize what data we show



                //creates a bitMap from our cyrstal bubble image
                Bitmap bitMap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.crystal_bubble);

                //scales the bitmap to a predetermined size
                Bitmap scaledBitMap = Bitmap.createScaledBitmap(bitMap, 320,320, true);

                //adds the scaled bitmap to our marker icon
                marker.icon(BitmapDescriptorFactory.fromBitmap(scaledBitMap)); //(BitmapDescriptorFactory.fromResource(R.drawable.crystal_bubble));// defaultMarker(colorBar.getProgress()*359/100));

                Intent resultIntent = new Intent();
                resultIntent.putExtra("marker", marker);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
