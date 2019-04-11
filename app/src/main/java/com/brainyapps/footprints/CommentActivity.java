package com.brainyapps.footprints;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainyapps.footprints.adapters.CommentRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Comment;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.User;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener, CommentRecyclerAdapter.OnClickItemListener{
    private ArrayList<Comment> commentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CommentRecyclerAdapter commentRecyclerAdapter;
    final String myUserId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String post_key;

    private ImageView btn_send;
    private EditText edit_comment;
    private TextView no_comment;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private User myInfo;

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
        setContentView(R.layout.activity_comment);

        ImageView btn_back = (ImageView) findViewById(R.id.comment_btn_back);
        btn_back.setOnClickListener(this);
        btn_send = (ImageView) findViewById(R.id.comment_btn_send);
        btn_send.setOnClickListener(this);
        edit_comment = (EditText) findViewById(R.id.comment_edit);
        no_comment = (TextView) findViewById(R.id.comment_no_result);

        commentRecyclerAdapter = new CommentRecyclerAdapter(commentList);
        recyclerView = (RecyclerView) findViewById(R.id.comment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(commentRecyclerAdapter);
        commentRecyclerAdapter.setOnClickItemListener(this);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                post_key = bundle.getString(IntentExtra.POST_ID);
                getComments();
            }
        }

        Query query = mDatabase.child(DBInfo.TBL_USER).child(myUserId);
        showProgressHUD("");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myInfo = dataSnapshot.getValue(User.class);
                hideProgressHUD();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getComments(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_POST).child(post_key);
        showProgressHUD("");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getInfoResult((Map<String,Object>) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
            }
        });
    }

    private void getInfoResult(Map<String,Object> post) {
        commentRecyclerAdapter.clear();

        if(post.containsKey("comments")){
            no_comment.setVisibility(View.GONE);
            Map<String, Object> comments = (HashMap<String, Object>)post.get("comments");
            for(Map.Entry<String,Object> single: comments.entrySet()){
                Map<String, String> comment_list  = (Map<String,String>) single.getValue();
                Comment putComment = new Comment();
                putComment.userId = comment_list.get("userId");
                putComment.comment = comment_list.get("comment");
                putComment.commentTime = comment_list.get("commentTime");
                commentList.add(putComment);
            }
            Collections.sort(commentList);
            commentRecyclerAdapter.notifyDataSetChanged();
        }else {
            no_comment.setVisibility(View.VISIBLE);
            no_comment.setTextColor(Color.parseColor("#adadad"));
        }
        hideProgressHUD();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.comment_btn_send:
                if(!edit_comment.getText().toString().replace(" ","").isEmpty()){
                    sendComment();
                }
                edit_comment.setText("");
                break;
            case R.id.comment_btn_back:
                goback();
                break;
            default:
                break;
        }
    }

    public void goback(){
        super.onBackPressed();
    }

    public void sendComment(){
        Comment new_comment = new Comment();
        new_comment.userId = myUserId;
        new_comment.comment = edit_comment.getText().toString();
        new_comment.commentTime = String.valueOf(System.currentTimeMillis());
        String commentId = mDatabase.child(Post.TABLE_NAME).child(post_key).child(DBInfo.POST_COMMENTS).push().getKey();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + Post.TABLE_NAME + "/" + post_key + "/" + DBInfo.POST_COMMENTS + "/" + commentId, new_comment);
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getComments();
            }
        });
        edit_comment.setText("");

        Query query = mDatabase.child(DBInfo.TBL_POST).child(post_key);
        showProgressHUD("");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if(!myUserId.equals(post.userId)){
                    String notify_id = mDatabase.child(Notification.TBL_NAME).child(post.userId).push().getKey();
                    Notification notification = new Notification();
                    notification.type = Notification.NotifyType.COMMENT;
                    notification.notifyFrom = myUserId;
                    notification.notifyTo = post_key;
                    notification.notificationId = notify_id;
                    if(myInfo!=null){
                        notification.content = myInfo.getName() + " commented on your post.";
                    }
                    else {
                        showProgressHUD("");
                    }
                    notification.time = String.valueOf(System.currentTimeMillis());
                    Map<String, Object> sendNotification = new HashMap<>();
                    sendNotification.put("/" + Notification.TBL_NAME + "/" + post.userId + "/" + notify_id,notification);
                    mDatabase.updateChildren(sendNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideProgressHUD();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
