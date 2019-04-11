package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.adapters.RegularNotificationRecyclerAdapter;
import com.brainyapps.footprints.adapters.RequestNotificationRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.utils.Notify;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainNotificationFragment extends android.app.Fragment implements View.OnClickListener, RegularNotificationRecyclerAdapter.OnClickItemListener, RequestNotificationRecyclerAdapter.OnClickItemListener{
    public static final String TAG = MainNotificationFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_main_notifications_fragment_tag";

    private static Context mContext;
    private static String myUserId;

    private ArrayList<Notification> regularNotificationList = new ArrayList<>();
    private RecyclerView regularNotificationRecyclerView;
    private RegularNotificationRecyclerAdapter regularNotificationRecyclerAdapter;

    private ArrayList<Notification> requestNotificationList = new ArrayList<>();
    private RecyclerView requestNotificationRecyclerView;
    private RequestNotificationRecyclerAdapter requestNotificationRecyclerAdapter;

    private TextView no_result;

    private DatabaseReference regularNotifyInfo;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressHUD.show(getActivity(), text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String email = charSequence.toString();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public static android.app.Fragment newInstance(Context context, String user_id) {
        mContext = context;
        myUserId = user_id;

        android.app.Fragment f = new MainNotificationFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_notification, container, false);
        regularNotificationRecyclerAdapter = new RegularNotificationRecyclerAdapter(regularNotificationList);
        regularNotificationRecyclerView = (RecyclerView)rootView.findViewById(R.id.notification_regular_recyclerview);
        regularNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        regularNotificationRecyclerView.setAdapter(regularNotificationRecyclerAdapter);
        regularNotificationRecyclerAdapter.setOnClickItemListener(this);

        no_result = (TextView)rootView.findViewById(R.id.notification_no_result);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                readNotification(regularNotificationRecyclerAdapter.notificationList.get(viewHolder.getAdapterPosition()));
                regularNotificationList.remove(viewHolder.getAdapterPosition());
                regularNotificationRecyclerAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchhelper.attachToRecyclerView(regularNotificationRecyclerView);


        requestNotificationRecyclerAdapter = new RequestNotificationRecyclerAdapter(requestNotificationList);
        requestNotificationRecyclerView = (RecyclerView)rootView.findViewById(R.id.notification_friend_request_recyclerview);
        requestNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        requestNotificationRecyclerView.setAdapter(requestNotificationRecyclerAdapter);
        requestNotificationRecyclerAdapter.setOnClickItemListener(this);


        regularNotifyInfo = mDatabase.child(Notification.TBL_NAME).child(myUserId).orderByChild("time").getRef();
        regularNotifyInfo.addValueEventListener(getRegularNotifyInfoListener);
        showProgressHUD("");
        return rootView;
    }

    public void readNotification(Notification notification){
        String notificationId = notification.notificationId;
        mDatabase.child(Notification.TBL_NAME).child(myUserId).child(notificationId).child("isRead").setValue(1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            default:
                break;
        }
    }

    @Override
    public void onSelectProfile(int index, String notification_type, String notification_from, String notification_to) {

    }

    private ValueEventListener getRegularNotifyInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            regularNotificationRecyclerAdapter.clear();
            if(dataSnapshot.exists()){
                regularNotificationList.clear();
                for (DataSnapshot notifyInfo : dataSnapshot.getChildren()) {
                    Notification notification = notifyInfo.getValue(Notification.class);
                    if(notification.isRead == 0){
                        if(!notification.type.equals(Notification.NotifyType.REQUESTED)){
                            regularNotificationList.add(notification);
                        }else {
                            requestNotificationList.add(notification);
                        }
                    }
                }
                if(regularNotificationList.size() > 0){
                    no_result.setVisibility(View.GONE);
                    Collections.sort(regularNotificationList);
                }else {
                    no_result.setVisibility(View.VISIBLE);
                }
                regularNotificationRecyclerAdapter.notifyDataSetChanged();
                requestNotificationRecyclerAdapter.notifyDataSetChanged();
            }else {
                no_result.setVisibility(View.VISIBLE);
            }
            hideProgressHUD();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            hideProgressHUD();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        regularNotifyInfo.removeEventListener(getRegularNotifyInfoListener);
    }

    @Override
    public void onSelectProfile(int index, String notification_from) {

    }

    @Override
    public void onClickAccept(int index, String notification_from) {
        Long tsLong = System.currentTimeMillis();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + notification_from + "/" + "followings" + "/" + myUserId, String.valueOf(tsLong));
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + myUserId + "/" + "followers" + "/" + notification_from, String.valueOf(tsLong));
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        mDatabase.child(DBInfo.TBL_USER).child(notification_from).child("pending").child(myUserId).setValue(null);

        Notify.notifyRequest(Notification.NotifyType.ACCEPTED, myUserId, notification_from);
        readNotification(requestNotificationRecyclerAdapter.notificationList.get(index));
        requestNotificationList.remove(index);
        requestNotificationRecyclerAdapter.notifyItemRemoved(index);
    }

    @Override
    public void onClickDecline(int index, String notification_from) {
        mDatabase.child(DBInfo.TBL_USER).child(notification_from).child("pending").child(myUserId).setValue(null);

        Notify.notifyRequest(Notification.NotifyType.DECLINED, myUserId, notification_from);
        readNotification(requestNotificationRecyclerAdapter.notificationList.get(index));
        requestNotificationList.remove(index);
        requestNotificationRecyclerAdapter.notifyItemRemoved(index);
    }
}

