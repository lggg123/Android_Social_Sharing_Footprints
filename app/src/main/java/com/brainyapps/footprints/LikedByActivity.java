package com.brainyapps.footprints;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.adapters.SearchRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.Search;
import com.bumptech.glide.Glide;
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

public class LikedByActivity extends AppCompatActivity implements View.OnClickListener, SearchRecyclerAdapter.OnClickItemListener{

    private String postID;
    private ArrayList<Search> likedList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearchRecyclerAdapter likedRecyclerAdapter;
    private TextView no_result;

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
        setContentView(R.layout.activity_liked_by);

        likedRecyclerAdapter = new SearchRecyclerAdapter(likedList);
        recyclerView = (RecyclerView) findViewById(R.id.liked_by_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(likedRecyclerAdapter);
        likedRecyclerAdapter.setOnClickItemListener(this);
        no_result = (TextView)findViewById(R.id.liked_no_result);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                postID = bundle.getString(IntentExtra.POST_ID);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_POST).child(postID);
                showProgressHUD("");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
//                            Post post = dataSnapshot.getValue(Post.class);
                            getInfoResult((Map<String,Object>) dataSnapshot.getValue());
                        }else{
                            hideProgressHUD();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgressHUD();
                    }
                });
            }
        }
    }


    private void getInfoResult(Map<String,Object> users) {
        if(users.containsKey("likes")){
            no_result.setVisibility(View.GONE);
            Map<String, String> posts = (HashMap<String, String>)users.get("likes");
            List<String> liked_list = new ArrayList<>();
            for(Map.Entry<String,String> single: posts.entrySet()){
                liked_list.add(single.getKey());
            }
            addToLikedRecyclerView(liked_list);
        }else {
            no_result.setVisibility(View.VISIBLE);
            no_result.setTextColor(Color.parseColor("#adadad"));
            hideProgressHUD();
        }
    }

    public void addToLikedRecyclerView(final List<String> list){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    likedRecyclerAdapter.clear();
                    likedList.clear();
                    Map<String,Object> result = (Map<String,Object>)dataSnapshot.getValue();
                    for (Map.Entry<String, Object> entry : result.entrySet()){
                        Map singleUser = (Map) entry.getValue();
                        for(int i = 0; i < list.size(); i++){
                            if(list.get(i).toString().equals(entry.getKey())){
                                final Search new_result = new Search();
                                new_result.userId = singleUser.get("userId").toString();
                                new_result.firstName = singleUser.get("firstName").toString();
                                new_result.lastName = singleUser.get("lastName").toString();
                                new_result.photoUrl = singleUser.get("photoUrl").toString();
                                likedList.add(new_result);
                            }
                        }
                    }
                    likedRecyclerAdapter.notifyDataSetChanged();
                }
                hideProgressHUD();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    public void liked_by_goto_backpage(View view){
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSelectProfile(int index, String userId) {
        Intent other_user_page_intent = new Intent(this, OthersprofileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, userId);
        other_user_page_intent.putExtras(bundle);
        startActivity(other_user_page_intent);
    }
}
