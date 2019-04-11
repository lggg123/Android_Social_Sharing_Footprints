package com.brainyapps.footprints.models;

import android.support.annotation.NonNull;

/**
 * Created by SuperMan on 5/3/2018.
 */

public class Notification implements Comparable<Notification>{
    public static final String TBL_NAME = "notifications";

    @Override
    public int compareTo(@NonNull Notification notification) {
        if (Long.parseLong(notification.time) > Long.parseLong(time))
            return 1;
        else
            return -1;
    }

    public class NotifyType{
        public static final String LIKEED ="Liked";
        public static final String COMMENT = "Comment";
        public static final String FOLLOWED = "Followed";
        public static final String ACCEPTED = "Accepted";
        public static final String DECLINED = "Declined";
        public static final String REQUESTED = "Request_follow";
        public static final String POSTED = "Posted";
    }

    public String notificationId = "";
    public String type = NotifyType.LIKEED;
    public String content = "";
    public String time = "";
    public String notifyFrom = "";
    public String notifyTo = "";
    public Integer isRead = 0;
//    public Integer isHide = 0;
    public Notification(){
        notificationId = "";
        type = NotifyType.LIKEED;
        content = "";
        time = "";
        notifyFrom = "";
        notifyTo = "";
        isRead = 0;
//        isHide = 0;
    }
}