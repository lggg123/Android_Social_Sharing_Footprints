package com.brainyapps.footprints.models;

/**
 * Created by SuperMan on 4/22/2018.
 */

public class Search {
    public String userId = "";
    public String firstName = "";
    public String lastName = "";
    public String photoUrl = "";

    public Search(){
        userId = "";
        firstName = "";
        lastName = "";
        photoUrl = "";
    }

    public String getName(){
        return firstName+" "+lastName;
    }
}
