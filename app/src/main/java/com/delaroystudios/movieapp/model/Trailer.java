package com.delaroystudios.movieapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by delaroy on 5/24/17.
 */
public class Trailer {

    @SerializedName("key")
    private String key;
    @SerializedName("name")
    private String name;

    public Trailer(String key, String name){
        this.key = key;
        this.name = name;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
