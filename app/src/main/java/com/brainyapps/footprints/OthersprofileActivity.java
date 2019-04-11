package com.brainyapps.footprints;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.adapters.ProfileFollowRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Follower;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.Report;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Notify;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OthersprofileActivity extends AppCompatActivity implements View.OnClickListener, ProfileFollowRecyclerAdapter.OnClickItemListener{

    final Context context = this;
    private TextView btn_submit;
    private EditText content_report;
    private ImageView goBack;
    private ImageView reportFlag;

    private CircleImageView profileImage;
    private TextView postNumber;
    private TextView followerNumber;
    private TextView followingNumber;
    private TextView address;
    private TextView brif;
    private TextView title;

    private RelativeLayout btn_follow;
    private RelativeLayout btn_unfollow;
    private RelativeLayout btn_pending;
    private RelativeLayout infoTable;
    private RelativeLayout followedByFiled;
    private RelativeLayout header_filed;

    private LinearLayout user_followers;
    private LinearLayout user_followings;

    private LinearLayout followed_brif_filed;

    String posting_numbers = "0";
    String follower_numbers = "0";
    String following_numbers = "0";
    String otherUserID = "";
    String curret_status = "unfollow";

    boolean isPrivate = false;
    final String myUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ArrayList<Follower> followList = new ArrayList<>();
    private RecyclerView followRecyclerView;
    private ProfileFollowRecyclerAdapter followRecyclerAdapter;

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
        setContentView(R.layout.activity_othersprofile);
        goBack = (ImageView) findViewById(R.id.otherprofile_followed_back);
        goBack.setOnClickListener(this);
        reportFlag = (ImageView) findViewById(R.id.otherprofile_followed_flag);
        reportFlag.setOnClickListener(this);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        followRecyclerAdapter = new ProfileFollowRecyclerAdapter(followList);
        followRecyclerView = (RecyclerView) findViewById(R.id.profile_follow_recyclerview);
        followRecyclerView.setLayoutManager(layoutManager);
        followRecyclerView.setAdapter(followRecyclerAdapter);
        followRecyclerAdapter.setOnClickItemListener(this);

        profileImage = (CircleImageView) findViewById(R.id.otherprofile_image);
        postNumber = (TextView) findViewById(R.id.other_profile_post_number);
        followerNumber = (TextView) findViewById(R.id.other_profile_follower_number);
        followingNumber = (TextView) findViewById(R.id.other_profile_following_number);
        address = (TextView) findViewById(R.id.otherprofile_followed_location_text);
        brif = (TextView) findViewById(R.id.otherprofile_followed_brif_text);
        title = (TextView) findViewById(R.id.other_profile_title);

        btn_follow = (RelativeLayout) findViewById(R.id.btn_follow);
        btn_follow.setOnClickListener(this);
        btn_unfollow = (RelativeLayout) findViewById(R.id.btn_unfollow);
        btn_unfollow.setOnClickListener(this);
        btn_pending = (RelativeLayout) findViewById(R.id.btn_pendding);

        infoTable = (RelativeLayout) findViewById(R.id.other_profile_options);
        followedByFiled = (RelativeLayout)findViewById(R.id.otherprofile_followed_followed_content);
        followed_brif_filed = (LinearLayout) findViewById(R.id.otherprofile_followed_brif);
        header_filed = (RelativeLayout) findViewById(R.id.otherprofile_followed_title_field);

        user_followers = (LinearLayout)findViewById(R.id.other_profile_followed_users);
        user_followers.setOnClickListener(this);
        user_followings = (LinearLayout)findViewById(R.id.other_profile_following_users);
        user_followings.setOnClickListener(this);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference();
                otherUserID = bundle.getString(IntentExtra.USER_ID);
            }
        }
    }

    private ValueEventListener getMyStatus = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                curret_status = "unfollow";
                User user = dataSnapshot.getValue(User.class);
                if(user.pending.isEmpty()){
                    if(!user.followings.isEmpty()){
                        for(Map.Entry<String,String> single: user.followings.entrySet()){
                            if(single.getKey().equals(otherUserID)){
                                curret_status = "following";
                            }
                        }
                    }
                }else{
                    for(Map.Entry<String,String> single: user.pending.entrySet()){
                        if(single.getKey().equals(otherUserID)){
                            curret_status = "pending";
                        }
                    }
                    if(!user.followings.isEmpty()){
                        for(Map.Entry<String,String> single: user.followings.entrySet()){
                            if(single.getKey().equals(otherUserID)){
                                curret_status = "following";
                            }
                        }
                    }
                }
                refresh();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            hideProgressHUD();
        }
    };

    public void refresh(){
        if(!isFinishing()){showProgressHUD("");}
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER).child(otherUserID);
        followRecyclerAdapter.clear();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.address.equals("")){
                    address.setText("(This person did not input address.)");
                    address.setTextColor(Color.parseColor("#adadad"));
                }else {
                    address.setText(user.address);
                }
                if(user.brif.equals("")) {
                    brif.setText("(This person did not input content.)");
                    brif.setTextColor(Color.parseColor("#adadad"));
                }else
                    brif.setText(user.brif);
                title.setText(user.firstName+" "+user.lastName);
                if(user.privacy.equals("On")){
                    isPrivate = true;
                }
                if(user.photoUrl.equals("")){
                    char first_letter = user.firstName.charAt(0);
                    char last_letter = user.lastName.charAt(0);
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .fontSize(30)
                            .bold()
                            .width(100)  // width in px
                            .height(100) // height in px
                            .endConfig()
                            .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10,127,181));
                    profileImage.setImageDrawable(drawable);
                }else {
                    Glide.with(getApplicationContext()).load(user.photoUrl).into(profileImage);
                }

                if(user.posts.isEmpty()){
                    posting_numbers = "0";
                }else {
                    posting_numbers = String.valueOf(user.posts.size());
                }

                if(user.followers.isEmpty()){
                    follower_numbers = "0";
                }else {
                    follower_numbers = String.valueOf(user.followers.size());
                    List<String> follower_list = new ArrayList<>();
                    for(Map.Entry<String,String> single: user.followers.entrySet()){
                        follower_list.add(single.getKey());
                    }
                    addTofollowRecyclerView(follower_list);
                }
                if(user.followings.isEmpty()){
                    following_numbers = "0";
                }else {
                    following_numbers = String.valueOf(user.posts.size());
                }

                postNumber.setText(posting_numbers);
                followerNumber.setText(follower_numbers);
                followingNumber.setText(following_numbers);
                setStartView(curret_status);
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void addTofollowRecyclerView(final List<String> list){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followRecyclerAdapter.clear();
                Map<String,Object> result = (Map<String,Object>)dataSnapshot.getValue();
                for (Map.Entry<String, Object> entry : result.entrySet()){
                    Map singleUser = (Map) entry.getValue();
                    for(int i = 0; i < list.size(); i++){
                        if(list.get(i).toString().equals(entry.getKey())){
                            final Follower new_result = new Follower();
                            new_result.userId = singleUser.get("userId").toString();
                            new_result.firstName = singleUser.get("firstName").toString();
                            new_result.lastName = singleUser.get("lastName").toString();
                            new_result.photoUrl = singleUser.get("photoUrl").toString();
                            followList.add(new_result);
                        }
                    }
                }
                followRecyclerAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setStartView(String status){
        if(status.equals("following")){
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (310 * scale + 0.5f);
            header_filed.getLayoutParams().height = pixels;
            btn_unfollow.setVisibility(View.VISIBLE);
            btn_follow.setVisibility(View.GONE);
            btn_pending.setVisibility(View.GONE);
            reportFlag.setVisibility(View.VISIBLE);
            infoTable.setVisibility(View.VISIBLE);
            followedByFiled.setVisibility(View.VISIBLE);
            followed_brif_filed.setVisibility(View.VISIBLE);
        }else if(status.equals("unfollow")){
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (260 * scale + 0.5f);
            header_filed.getLayoutParams().height = pixels;
            btn_unfollow.setVisibility(View.GONE);
            btn_follow.setVisibility(View.VISIBLE);
            btn_pending.setVisibility(View.GONE);
            reportFlag.setVisibility(View.VISIBLE);
            infoTable.setVisibility(View.GONE);
            followedByFiled.setVisibility(View.GONE);
            if(isPrivate){
                followed_brif_filed.setVisibility(View.GONE);
            }
        }else if(status.equals("pending")){
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (260 * scale + 0.5f);
            header_filed.getLayoutParams().height = pixels;
            btn_unfollow.setVisibility(View.GONE);
            btn_follow.setVisibility(View.GONE);
            btn_pending.setVisibility(View.VISIBLE);
            reportFlag.setVisibility(View.VISIBLE);
            infoTable.setVisibility(View.GONE);
            followedByFiled.setVisibility(View.GONE);
            if(isPrivate){
                followed_brif_filed.setVisibility(View.GONE);
            }
        }
        if(myUserID.equals(otherUserID)){
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (280 * scale + 0.5f);
            header_filed.getLayoutParams().height = pixels;
            btn_unfollow.setVisibility(View.GONE);
            btn_follow.setVisibility(View.GONE);
            btn_pending.setVisibility(View.GONE);
            reportFlag.setVisibility(View.GONE);
            infoTable.setVisibility(View.VISIBLE);
            followedByFiled.setVisibility(View.VISIBLE);
            followed_brif_filed.setVisibility(View.VISIBLE);
        }
    }

    public void goBackPage(){
        super.onBackPressed();
    }

    public void otherProfileFollow(View view){
    }

    public void viewReportPopup(){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.report_popup);

        btn_submit = (TextView) dialog.findViewById(R.id.otherprofile_followed_report_submit);
        content_report = (EditText) dialog.findViewById(R.id.otherprofile_followed_report_edit);
        // if button is clicked, close the custom dialog
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = content_report.getText().toString();
                content = content.replace(" ","");
                if(TextUtils.isEmpty(content)){
                    content_report.setText("");
                    alertMessage("","Please input report and try again.");
                }else {
                    submitReport(content_report.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.otherprofile_followed_back:
                goBackPage();
                break;
            case R.id.otherprofile_followed_flag:
                viewReportPopup();
                break;
            case R.id.btn_follow:
                if(isPrivate){
                    sendFollowRequest();
                }else
                    followThisUser();
                break;
            case R.id.btn_unfollow:
                unfollowThisUser();
                break;
            case R.id.other_profile_followed_users:
                getFollowedUser(otherUserID);
                break;
            case R.id.other_profile_following_users:
                getFollowingUser(otherUserID);
            default:
                break;
        }
    }
    public void sendFollowRequest(){
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + myUserID + "/" + "pending" + "/" + otherUserID, otherUserID);
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        Notify.notifyRequest(Notification.NotifyType.REQUESTED,myUserID,otherUserID);
    }

    public void getFollowedUser(String userId){
        Intent follower_page_intent = new Intent(this, FollowerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.FOLLOWER_USER_ID, userId);
        follower_page_intent.putExtras(bundle);
        startActivity(follower_page_intent);
    }

    public void getFollowingUser(String userId){
        Intent follower_page_intent = new Intent(this, FollowerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.FOLLOWING_USER_ID, userId);
        follower_page_intent.putExtras(bundle);
        startActivity(follower_page_intent);
    }

    public void submitReport(String content){

        String reportId = mDatabase.child(Report.TABLE_NAME).push().getKey();

        Report new_report = new Report();
        new_report.reportId = reportId;
        new_report.reporterId = myUserID;
        new_report.reportedId = otherUserID;
        new_report.reportContent = content;

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + Report.TABLE_NAME + "/" + reportId, new_report);
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressHUD();
                alertMessage("","Thanks for your report.");
            }
        });
    }

    public void alertMessage(String title, String message){
        AlertFactory.showAlert(this, title, message, "OKAY", "", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(AlertDialog dialog) {

            }
            @Override
            public void onClickNo(AlertDialog dialog) {

            }
            @Override
            public void onClickDone(AlertDialog dialog) {
                dialog.dismiss();
            }
        });
    }

    public void followThisUser(){
        Long tsLong = System.currentTimeMillis();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + myUserID + "/" + "followings" + "/" + otherUserID, String.valueOf(tsLong));
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + otherUserID + "/" + "followers" + "/" + myUserID, String.valueOf(tsLong));
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        Notify.notifyFollow("follow",myUserID,otherUserID);
    }

    private void unfollowThisUser(){
        mDatabase.child(DBInfo.TBL_USER).child(myUserID).child("followings").child(otherUserID).setValue(null);
        mDatabase.child(DBInfo.TBL_USER).child(otherUserID).child("followers").child(myUserID).setValue(null);
        Notify.notifyFollow("unfollow",myUserID,otherUserID);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER).child(myUserID);
        mDatabase.addValueEventListener(getMyStatus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(getMyStatus);
    }
}
