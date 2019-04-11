package com.brainyapps.footprints.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.brainyapps.footprints.constants.DBInfo;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SuperMan on 4/20/2018.
 */

public class Post implements Parcelable, Comparable<Post>{

    public static final String TABLE_NAME = "posts";
    public String postId = "";
    public String userId = "";
    public String postTitle = "";
    public String postDescription = "";
    public String mediaType = "";
    public String mediaUrl = "";
    public long postedTime = 0L;
    public int isLocked = 0;
    public GooglePosition googlePosition = new GooglePosition();

    public Map<String, String> likes = new HashMap<>();
    public Map<String, Comment> comments = new HashMap<>();

    public Post(){
        postId = "";
        userId = "";
        postTitle = "";
        postDescription = "";
        mediaType = "";
        mediaUrl = "";
        postedTime = 0L;
        isLocked = 0;
        googlePosition = new GooglePosition();

        likes = new HashMap<>();
        comments = new HashMap<>();
    }
    protected Post(Parcel in) {
        postId = in.readString();
        userId = in.readString();
        postTitle = in.readString();
        postDescription = in.readString();
        mediaType = in.readString();
        mediaUrl = in.readString();
        postedTime = in.readLong();
        isLocked = in.readInt();
        googlePosition = in.readParcelable(GooglePosition.class.getClassLoader());

        in.readMap(likes, Map.class.getClassLoader());
        in.readMap(comments, Map.class.getClassLoader());
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel parcel) {
            return new Post(parcel);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBInfo.POST_ID, postId);
        result.put(DBInfo.USER_ID, userId);
        result.put(DBInfo.POST_TITLE, postTitle);
        result.put(DBInfo.POST_DESCRIPTION, postDescription);
        result.put(DBInfo.MEDIA_TYPE, mediaType);
        result.put(DBInfo.MEDIA_URL, mediaUrl);
        result.put(DBInfo.POST_TIME, postedTime);
        result.put(DBInfo.POST_IS_LOCKED, isLocked);
        result.put(DBInfo.USER_GOOGLEPOSITION, googlePosition.toMap());

        result.put(DBInfo.POST_LIKES, likes);
        result.put(DBInfo.POST_COMMENTS, comments);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(postId);
        parcel.writeString(userId);
        parcel.writeString(postTitle);
        parcel.writeString(postDescription);
        parcel.writeString(mediaType);
        parcel.writeString(mediaUrl);
        parcel.writeLong(postedTime);
        parcel.writeInt(isLocked);
        parcel.writeParcelable(googlePosition, i);
    }

    @Override
    public int compareTo(@NonNull Post post) {
        if (post.postedTime > postedTime)
            return 1;
        else
            return -1;
    }
}