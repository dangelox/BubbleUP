package com.example.bubbleup;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/*
A class to store user data.
 */
public class UserData implements Serializable{
    private String token = "";
    private String username = "";
    private LatLng current_location;
    private LatLng previous_location;
    private int previous_location_zoom;
    private boolean check = false;


    public UserData(String success_token, String success_username){
        token = success_token;
        username = success_username;
        check = true;
    }

    public String getUserName(){
        return username;
    }

    public String getToken(){
        return token;
    }

    //Checks if User has been created correctly
    public boolean check(){
        return check;
    }
}
