package com.brainyapps.footprints.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.brainyapps.footprints.constants.DBInfo;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SuperMan on 4/16/2018.
 */

public class User implements Parcelable {

    public static final String TABLE_NAME = "users";
    public String userId = "";
    public String userEmail = "";
    public String userPassword = "";
    public String signupType = "";
    public String facebookId = "";
    public String firstName = "";
    public String lastName = "";
    public String photoUrl = "";
    public String brif = "";
    public String address = "";
    public String privacy = "On";
    public Integer banned = 0;

    public Map<String, String> posts = new HashMap<>();
    public Map<String, String> followers = new HashMap<>();
    public Map<String, String> followings = new HashMap<>();
    public Map<String, String> pending = new HashMap<>();

    public User(){
        userId = "";
        userEmail = "";
        userPassword = "";
        signupType = "";
        facebookId = "";
        firstName = "";
        lastName = "";
        photoUrl = "";
        brif = "";
        address = "";
        privacy = "Off";
        banned = 0;

        posts = new HashMap<>();
        followers = new HashMap<>();
        followings = new HashMap<>();
        pending = new HashMap<>();
    }
    protected User(Parcel in) {
        userId = in.readString();
        userEmail = in.readString();
        userPassword = in.readString();
        signupType = in.readString();
        facebookId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        photoUrl = in.readString();
        privacy = in.readString();
        banned = in.readInt();
        brif = in.readString();
        address = in.readString();

        in.readMap(posts, Map.class.getClassLoader());
        in.readMap(followers, Map.class.getClassLoader());
        in.readMap(followings, Map.class.getClassLoader());
        in.readMap(pending, Map.class.getClassLoader());
    }

    public String getName(){
        return firstName+" "+lastName;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBInfo.USER_ID, userId);
        result.put(DBInfo.USER_EMAIL, userEmail);
        result.put(DBInfo.USER_PASSWORD, userPassword);
        result.put(DBInfo.USER_FIRSTNAME, firstName);
        result.put(DBInfo.USER_LASTNAME, lastName);
        result.put(DBInfo.USER_FACEBOOKID, facebookId);
        result.put(DBInfo.USER_PHOTO, photoUrl);
        result.put(DBInfo.USER_PRIVACY, privacy);
        result.put(DBInfo.USER_SIGNUP_TYPE, signupType);
        result.put(DBInfo.USER_BANNED, banned);
        result.put(DBInfo.USER_BRIF, brif);
        result.put(DBInfo.USER_ADDRESS, address);

        result.put(DBInfo.USER_POSTS, posts);
        result.put(DBInfo.USER_FOLLOWERS, followers);
        result.put(DBInfo.USER_FOLLOWINGS, followings);
        result.put(DBInfo.USER_PENDING, pending);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(userEmail);
        parcel.writeString(userPassword);
        parcel.writeString(signupType);
        parcel.writeString(facebookId);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(photoUrl);
        parcel.writeString(privacy);
        parcel.writeInt(banned);
        parcel.writeString(brif);
        parcel.writeString(address);

        if (posts == null)
            posts = new HashMap<>();
        parcel.writeMap(posts);

        if (followers == null)
            followers = new HashMap<>();
        parcel.writeMap(followers);

        if (followings == null)
            followings = new HashMap<>();
        parcel.writeMap(followings);

        if (pending == null)
            pending = new HashMap<>();
        parcel.writeMap(pending);
    }
}
