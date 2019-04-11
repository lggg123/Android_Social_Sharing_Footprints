package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brainyapps.footprints.CommentActivity;
import com.brainyapps.footprints.EditpostActivity;
import com.brainyapps.footprints.FollowerActivity;
import com.brainyapps.footprints.LikedByActivity;
import com.brainyapps.footprints.MainActivity;
import com.brainyapps.footprints.OthersprofileActivity;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.adapters.ProfileFollowRecyclerAdapter;
import com.brainyapps.footprints.adapters.ProfilePostRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Comment;
import com.brainyapps.footprints.models.EditPost;
import com.brainyapps.footprints.models.Follower;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;
import static java.lang.Long.parseLong;

public class MainProfileFragment extends android.app.Fragment implements View.OnClickListener, ProfileFollowRecyclerAdapter.OnClickItemListener, ProfilePostRecyclerAdapter.OnClickItemListener{

    public static final String TAG = MainProfileFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_main_profile_fragment_tag";

    private static Context mContext;
    private static String my_id;

    private TextView number_of_post;
    private TextView number_of_follower;
    private TextView number_of_following;

    private TextView location;
    private TextView brif;
    private TextView no_follower;
    private TextView no_post;

    private ArrayList<Follower> followList = new ArrayList<>();
    private RecyclerView followRecyclerView;
    private ProfileFollowRecyclerAdapter followRecyclerAdapter;

    private ArrayList<EditPost> postList = new ArrayList<>();
    private RecyclerView postRecyclerView;
    private ProfilePostRecyclerAdapter postRecyclerAdapter;

    private LinearLayout btn_followers;
    private LinearLayout btn_followings;

    String postingNumbers = "0";
    String followerNumbers = "0";
    String followingNumbers = "0";

    private DatabaseReference userInfo;

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressHUD.show(mContext, text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private ValueEventListener getProfileInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                User user = dataSnapshot.getValue(User.class);
                followRecyclerAdapter.clear();
                postRecyclerAdapter.clear();
                if(user.address.equals("")){
                    location.setText("(This person did not input address.)");
                    location.setTextColor(Color.parseColor("#adadad"));
                }else
                    location.setText(user.address);
                if(user.brif.equals("")) {
                    brif.setText("(This person did not input content.)");
                    brif.setTextColor(Color.parseColor("#adadad"));
                }else
                    brif.setText(user.brif);

                if(user.posts.isEmpty()){
                    postingNumbers = "0";
                    no_post.setVisibility(View.VISIBLE);
                    no_post.setTextColor(Color.parseColor("#adadad"));
                }else {
                    no_post.setVisibility(View.GONE);
                    postingNumbers = String.valueOf(user.posts.size());
                    List<String> post_list = new ArrayList<>();
                    for(Map.Entry<String,String> single: user.posts.entrySet()){
                        post_list.add(single.getKey());
                    }
                    addTopostRecyclerView(post_list);
                }

                if(user.followers.isEmpty()){
                    followerNumbers = "0";
                    no_follower.setVisibility(View.VISIBLE);
                    no_follower.setTextColor(Color.parseColor("#adadad"));
                }else {
                    followingNumbers = "0";
                    no_follower.setVisibility(View.GONE);
                    followerNumbers = String.valueOf(user.followers.size());
                    List<String> follower_list = new ArrayList<>();
                    for(Map.Entry<String,String> single: user.followers.entrySet()){
                        follower_list.add(single.getKey());
                    }
                    addTofolloeRecyclerView(follower_list);
                }

                if(!user.followings.isEmpty()){
                    followingNumbers = String.valueOf(user.followings.size());
                }
                number_of_post.setText(postingNumbers);
                number_of_follower.setText(followerNumbers);
                number_of_following.setText(followingNumbers);
            }
            hideProgressHUD();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            hideProgressHUD();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        userInfo = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER).child(my_id);
        userInfo.addValueEventListener(getProfileInfoListener);
    }

    public MainProfileFragment() {
        // Required empty public constructor
    }

    public static android.app.Fragment newInstance(Context context ,String user_id) {
        mContext = context;
        my_id = user_id;
        android.app.Fragment f = new MainProfileFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        showProgressHUD("");

        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_profile, container, false);

        final CircleImageView myprofile_image = (CircleImageView)rootView.findViewById(R.id.myprofile_image);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(my_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
                    Utils.setAvatarImage(user, mContext, myprofile_image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        followRecyclerAdapter = new ProfileFollowRecyclerAdapter(followList);
        followRecyclerView = (RecyclerView)rootView.findViewById(R.id.myprofile_follow_recyclerview);
        followRecyclerView.setLayoutManager(layoutManager);
        followRecyclerView.setAdapter(followRecyclerAdapter);
        followRecyclerAdapter.setOnClickItemListener(this);

        postRecyclerAdapter = new ProfilePostRecyclerAdapter(postList);
        postRecyclerView = (RecyclerView)rootView.findViewById(R.id.profile_post_recyclerview);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        postRecyclerView.setAdapter(postRecyclerAdapter);
        postRecyclerAdapter.setOnClickItemListener(this);

        number_of_post = (TextView)rootView.findViewById(R.id.myprofile_number_post);
        number_of_follower = (TextView)rootView.findViewById(R.id.myprofile_number_follower);
        number_of_following = (TextView)rootView.findViewById(R.id.myprofile_number_following);
        no_follower = (TextView)rootView.findViewById(R.id.profile_follow_no_result);
        no_post = (TextView)rootView.findViewById(R.id.myprofile_post_no_result);

        location = (TextView)rootView.findViewById(R.id.myprofile_location_text);
        brif = (TextView)rootView.findViewById(R.id.myprofile_brif_text);

        btn_followers = (LinearLayout)rootView.findViewById(R.id.myprofile_followers);
        btn_followings = (LinearLayout)rootView.findViewById(R.id.myprofile_followings);
        btn_followings.setOnClickListener(this);
        btn_followers.setOnClickListener(this);

        return  rootView;
    }

    public void addTofolloeRecyclerView(final List<String> list){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    followList.clear();
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
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void addTopostRecyclerView(final List<String> list){
        postRecyclerAdapter.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_POST).orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    postList.clear();
                    Map<String,Object> result = (Map<String,Object>)dataSnapshot.getValue();
                    for (Map.Entry<String, Object> entry : result.entrySet()){
                        Map singlePost = (Map) entry.getValue();
                        for(int i = 0; i < list.size(); i++){
                            if(list.get(i).toString().equals(entry.getKey())){
                                final EditPost new_result = new EditPost();
                                new_result.postId = entry.getKey();
                                new_result.postTitle = singlePost.get("postTitle").toString();
                                new_result.postDescription = singlePost.get("postDescription").toString();
                                new_result.postType = singlePost.get("mediaType").toString();
                                new_result.postMediaUrl = singlePost.get("mediaUrl").toString();
                                new_result.postedTime = parseLong(singlePost.get("postedTime").toString());
                                if(singlePost.containsKey("likes")){
                                    Map<String, String> posts = (HashMap<String, String>)singlePost.get("likes");
                                    new_result.liked = posts.size();
                                }else
                                    new_result.liked = 0;
                                if(singlePost.containsKey("comments")){
                                    Map<String, String> posts = (HashMap<String, String>)singlePost.get("comments");
                                    new_result.comments = posts.size();
                                }else
                                    new_result.comments = 0;
                                postList.add(new_result);
                            }
                        }
                    }
                    Collections.sort(postList);
                    postRecyclerAdapter.notifyDataSetChanged();
                }
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void getFollowers(String userId){
        Intent follower_page_intent = new Intent(getActivity(), FollowerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.FOLLOWER_USER_ID, userId);
        follower_page_intent.putExtras(bundle);
        startActivity(follower_page_intent);
    }

    public void getFollowings(String userId){
        Intent follower_page_intent = new Intent(getActivity(), FollowerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.FOLLOWING_USER_ID, userId);
        follower_page_intent.putExtras(bundle);
        startActivity(follower_page_intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.myprofile_followers:
                getFollowers(my_id);
                break;
            case R.id.myprofile_followings:
                getFollowings(my_id);
                break;
        }
    }

    @Override
    public void onSelectProfile(int index, String userId) {
        Intent other_user_page_intent = new Intent(getActivity(), OthersprofileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, userId);
        other_user_page_intent.putExtras(bundle);
        startActivity(other_user_page_intent);
    }

    @Override
    public void onEditPost(int index, String postId) {
        Intent edit_post_intent = new Intent(getActivity(), EditpostActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, postId);
        edit_post_intent.putExtras(bundle);
        startActivity(edit_post_intent);
    }

    @Override
    public void onCheckLiked(int index, String postId) {
        Intent liked_by_intent = new Intent(getActivity(), LikedByActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, postId);
        liked_by_intent.putExtras(bundle);
        startActivity(liked_by_intent);
    }

    @Override
    public void onCheckComments(int index, String postId) {
        Intent comment_by_intent = new Intent(getActivity(), CommentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, postId);
        comment_by_intent.putExtras(bundle);
        startActivity(comment_by_intent);
    }

    @Override
    public void media_video_play_icon(int index, String postId) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        userInfo.removeEventListener(getProfileInfoListener);
    }
}
