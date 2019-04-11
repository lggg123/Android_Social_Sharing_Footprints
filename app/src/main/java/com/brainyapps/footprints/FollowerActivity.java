package com.brainyapps.footprints;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainyapps.footprints.adapters.UserFollowerRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.controller.SwipeController;
import com.brainyapps.footprints.controller.SwipeControllerActions;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Notify;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FollowerActivity extends AppCompatActivity implements View.OnClickListener, UserFollowerRecyclerAdapter.OnClickItemListener{
    private ArrayList<User> followerList = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserFollowerRecyclerAdapter followerRecyclerAdapter;
    private String selected_user_id;
    final String my_user_id =  FirebaseAuth.getInstance().getCurrentUser().getUid();

    private TextView page_title;
    private TextView no_result;

    SwipeController swipeController = null;

    private DatabaseReference mDatabase;
    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(this, text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);
        ImageView btn_back = (ImageView)findViewById(R.id.follower_btn_back);
        btn_back.setOnClickListener(this);

        page_title = (TextView) findViewById(R.id.follower_title);

        followerRecyclerAdapter = new UserFollowerRecyclerAdapter(followerList);
        recyclerView = (RecyclerView) findViewById(R.id.follower_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(followerRecyclerAdapter);
        followerRecyclerAdapter.setOnClickItemListener(this);

        no_result = (TextView) findViewById(R.id.follower_no_result);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                super.onRightClicked(position);
                unfollowUser(followerRecyclerAdapter.followerList.get(position));
                followerRecyclerAdapter.followerList.remove(position);
                followerRecyclerAdapter.notifyItemRemoved(position);
                followerRecyclerAdapter.notifyItemRangeRemoved(position, followerRecyclerAdapter.getItemCount());
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference();
                if(bundle.getString(IntentExtra.FOLLOWER_USER_ID)!=null){
                    selected_user_id = bundle.getString(IntentExtra.FOLLOWER_USER_ID);
                    viewFollowerAction();
                    page_title.setText("Followers");
                }else if(bundle.getString(IntentExtra.FOLLOWING_USER_ID)!=null){
                    selected_user_id = bundle.getString(IntentExtra.FOLLOWING_USER_ID);
                    if(selected_user_id.equals(my_user_id)){
                        itemTouchhelper.attachToRecyclerView(recyclerView);
                        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                            @Override
                            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                                swipeController.onDraw(c);
                            }
                        });
                    }
                    viewFollowingAction();
                    page_title.setText("Followings");
                }
            }
        }
    }

    public void viewFollowerAction(){
        showProgressHUD("");
        followerRecyclerAdapter.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(selected_user_id).child("followers");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    getFollowerResult((Map<String,String>) dataSnapshot.getValue());
                else{
                    no_result.setVisibility(View.VISIBLE);
                    no_result.setText("No Followers yet.");
                    hideProgressHUD();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    private void getFollowerResult(final Map<String,String> users) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                        User user = userInfo.getValue(User.class);
                        for (Map.Entry<String, String> entry : users.entrySet()){
                            String singleUser_id = (String) entry.getKey();
                            if(singleUser_id.equals(user.userId)){
                                followerList.add(user);
                            }
                        }
                    }
                    followerRecyclerAdapter.notifyDataSetChanged();
                }
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    private void viewFollowingAction(){
        showProgressHUD("");
        followerRecyclerAdapter.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(selected_user_id).child("followings");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    getFollowingResult((Map<String,String>) dataSnapshot.getValue());
                }else {
                    no_result.setVisibility(View.VISIBLE);
                    no_result.setText("No Followings yet.");
                    hideProgressHUD();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    private void getFollowingResult(final Map<String,String> users) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                        User user = userInfo.getValue(User.class);
                        for (Map.Entry<String, String> entry : users.entrySet()){
                            String singleUser_id = (String) entry.getKey();
                            if(singleUser_id.equals(user.userId)){
                                followerList.add(user);
                            }
                        }
                    }
                    followerRecyclerAdapter.notifyDataSetChanged();
                }
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void goBackpage(){
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.follower_btn_back:
                goBackpage();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelectProfile(int index, String userId) {
        Intent other_user_page_intent = new Intent(this, OthersprofileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, userId);
        other_user_page_intent.putExtras(bundle);
        startActivity(other_user_page_intent);
    }

    @Override
    public void onFollowUser(int index, String userId) {
        showProgressHUD("");
        Long tsLong = System.currentTimeMillis();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + my_user_id + "/" + "followings" + "/" + userId, String.valueOf(tsLong));
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + userId + "/" + "followers" + "/" + my_user_id, String.valueOf(tsLong));
        FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressHUD();
            }
        });
        Notify.notifyFollow("follow",my_user_id,userId);
    }

    private void unfollowUser(User selected_user){
        String otherUserID = selected_user.userId;
        mDatabase.child(DBInfo.TBL_USER).child(my_user_id).child("followings").child(otherUserID).setValue(null);
        mDatabase.child(DBInfo.TBL_USER).child(otherUserID).child("followers").child(my_user_id).setValue(null);
        Notify.notifyFollow("unfollow",my_user_id,otherUserID);
    }
}
