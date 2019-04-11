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

public class Comment implements Comparable<Comment>{
    public String userId = "";
    public String comment = "";
    public String commentTime = "";

    public Comment(){
        userId = "";
        comment = "";
        commentTime = "";
    }

    @Override
    public int compareTo(@NonNull Comment comment) {
        if (Long.parseLong(comment.commentTime) > Long.parseLong(commentTime))
            return 1;
        else
            return -1;
    }
}
