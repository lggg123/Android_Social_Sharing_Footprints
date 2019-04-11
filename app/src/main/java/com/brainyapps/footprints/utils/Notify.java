package com.brainyapps.footprints.utils;

import android.support.annotation.NonNull;

import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SuperMan on 5/4/2018.
 */

public class Notify {
    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static void notifyFollow(String type, String notifyFrom, final String notifyTo){
        final Notification notification = new Notification();
        notification.type = Notification.NotifyType.FOLLOWED;
        notification.notifyFrom = notifyFrom;
        notification.notifyTo = notifyTo;
        notification.time = String.valueOf(System.currentTimeMillis());

        Query query = mDatabase.child(DBInfo.TBL_USER).child(notifyFrom);
        if(type.equals("follow")){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    notification.content = user.getName()+" followed you.";
                    String notify_id = FirebaseDatabase.getInstance().getReference().child(Notification.TBL_NAME).child(notifyTo).push().getKey();
                    notification.notificationId = notify_id;
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + notifyTo + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else if(type.equals("unfollow")){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    notification.content = user.getName()+" unfollowed you.";
                    String notify_id = FirebaseDatabase.getInstance().getReference().child(Notification.TBL_NAME).child(notifyTo).push().getKey();
                    notification.notificationId = notify_id;
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + notifyTo + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static void notifyRequest(String type, String notifyFrom, final String notifyTo){
        final Notification notification = new Notification();
        notification.notifyFrom = notifyFrom;
        notification.notifyTo = notifyTo;
        notification.time = String.valueOf(System.currentTimeMillis());

        Query query = mDatabase.child(DBInfo.TBL_USER).child(notifyFrom);
        if(type.equals(Notification.NotifyType.ACCEPTED)){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    notification.content = user.getName()+" accepted your following request.";
                    String notify_id = FirebaseDatabase.getInstance().getReference().child(Notification.TBL_NAME).child(notifyTo).push().getKey();
                    notification.notificationId = notify_id;
                    notification.type = Notification.NotifyType.ACCEPTED;
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + notifyTo + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else if(type.equals(Notification.NotifyType.DECLINED)){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    notification.content = user.getName()+" declined your following request.";
                    String notify_id = FirebaseDatabase.getInstance().getReference().child(Notification.TBL_NAME).child(notifyTo).push().getKey();
                    notification.notificationId = notify_id;
                    notification.type = Notification.NotifyType.DECLINED;
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + notifyTo + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if(type.equals(Notification.NotifyType.REQUESTED)){
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    notification.content = user.getName()+" sent a follow request.";
                    String notify_id = FirebaseDatabase.getInstance().getReference().child(Notification.TBL_NAME).child(notifyTo).push().getKey();
                    notification.notificationId = notify_id;
                    notification.type = Notification.NotifyType.REQUESTED;
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + notifyTo + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
