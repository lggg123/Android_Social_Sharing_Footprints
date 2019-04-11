package com.brainyapps.footprints.models;

/**
 * Created by SuperMan on 4/23/2018.
 */

public class Follower {
    public String userId = "";
    public String firstName = "";
    public String lastName = "";
    public String photoUrl = "";

    public Follower(){
        userId = "";
        firstName = "";
        lastName = "";
        photoUrl = "";
    }

    public String getName(){
        return firstName+" "+lastName;
    }
}
