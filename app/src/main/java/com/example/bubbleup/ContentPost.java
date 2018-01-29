package com.example.bubbleup;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/*
This class should be used to hold post contents
 */
public class ContentPost implements Serializable{
    int post_id;
    String text;
    String tittle;//By default tittle should be username??
    String poster_username;

    public ContentPost(){

    }
}
