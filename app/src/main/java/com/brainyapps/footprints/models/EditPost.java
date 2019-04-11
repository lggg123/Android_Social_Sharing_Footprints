package com.brainyapps.footprints.models;

import android.support.annotation.NonNull;

/**
 * Created by SuperMan on 4/23/2018.
 */

public class EditPost implements Comparable<EditPost>{
    public String postId = "";
    public String postTitle = "";
    public String postDescription = "";
    public String postType = "";
    public String postMediaUrl = "";
    public Long postedTime = 0L;
    public int liked = 0;
    public int comments = 0;

    public EditPost(){
        postId = "";
        postTitle = "";
        postType = "Image";
        postDescription = "";
        postMediaUrl = "";
        liked = 0;
        comments = 0;
        postedTime = 0L;
    }

    @Override
    public int compareTo(@NonNull EditPost editPost) {
        if (editPost.postedTime > postedTime)
            return 1;
        else
            return -1;
    }
}
