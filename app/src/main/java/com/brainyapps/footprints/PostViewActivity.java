package com.brainyapps.footprints;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.brainyapps.footprints.adapters.PostRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.Report;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.DownloadTask;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewActivity extends AppCompatActivity implements View.OnClickListener, PostRecyclerAdapter.OnClickItemListener, DownloadTask.OnDownloadStatusListener{
    final Context context = this;
    private ImageView btn_back;
    private CircleImageView img_avatar;
    private TextView text_title;
    private String selected_post_id;

    private TextView btn_submit;
    private EditText content_report;

    private ArrayList<Post> postList = new ArrayList<>();
    private RecyclerView postRecyclerView;
    private PostRecyclerAdapter postRecyclerAdapter;

    private DatabaseReference postInfo;
    private DatabaseReference mDatabase;
    private User myInfo;

    private final String my_userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private static final int PERMISSION_REQUEST_CODE = 70;

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
        setContentView(R.layout.activity_post_view);

        btn_back = (ImageView) findViewById(R.id.otherspost_back);
        btn_back.setOnClickListener(this);
        img_avatar = (CircleImageView) findViewById(R.id.view_post_avatar);
        text_title = (TextView) findViewById(R.id.view_post_title);

        postRecyclerAdapter = new PostRecyclerAdapter(postList);
        postRecyclerView = (RecyclerView) findViewById(R.id.post_recyclerview);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        postRecyclerView.setAdapter(postRecyclerAdapter);
        postRecyclerAdapter.setOnClickItemListener(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child(DBInfo.TBL_USER).child(my_userId);
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
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                selected_post_id = bundle.getString(IntentExtra.POST_ID);
                postInfo = mDatabase.child(DBInfo.TBL_POST).child(selected_post_id);
                postInfo.addValueEventListener(getPostInfoListener);
                showProgressHUD("");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.otherspost_back:
                super.onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLikeClick(int index, Boolean isLiked, final String postId) {
        showProgressHUD("");
        if(!isLiked){
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("/" + DBInfo.TBL_POST + "/" + postId + "/" + DBInfo.POST_LIKES + "/" + my_userId, my_userId);
            mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });

            Query query = mDatabase.child(DBInfo.TBL_POST).child(postId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Post post = dataSnapshot.getValue(Post.class);
                    String notify_id = mDatabase.child(Notification.TBL_NAME).child(post.userId).push().getKey();
                    Notification notification = new Notification();
                    notification.type = Notification.NotifyType.LIKEED;
                    notification.notifyFrom = my_userId;
                    notification.notifyTo = postId;
                    notification.content = myInfo.getName() + " liked your post.";
                    notification.notificationId = notify_id;
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

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressHUD();
                }
            });
        }else {
            mDatabase.child(DBInfo.TBL_POST).child(postId).child(DBInfo.POST_LIKES).child(my_userId).setValue(null);
            hideProgressHUD();
        }
    }

    @Override
    public void onCheckLiked(int index, String postId) {
        Intent liked_by_intent = new Intent(this, LikedByActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, postId);
        liked_by_intent.putExtras(bundle);
        startActivity(liked_by_intent);
    }

    @Override
    public void onCheckComments(int index, String postId) {
        Intent comment_by_intent = new Intent(this, CommentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.POST_ID, postId);
        comment_by_intent.putExtras(bundle);
        startActivity(comment_by_intent);
    }

    @Override
    public void media_video_play_icon(int index, String postId) {

    }

    @Override
    public void showReportDlg(int index, final String postId) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.report_popup);
        dialog.show();
        btn_submit = (TextView) dialog.findViewById(R.id.otherprofile_followed_report_submit);
        content_report = (EditText) dialog.findViewById(R.id.otherprofile_followed_report_edit);
        // if button is clicked, close the custom dialog
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(content_report.getText().toString().replace(" ","").equals("")){
                    alertMessage("","Please input report and try again.");
                    content_report.setText("");
                }else {
                    Query query = mDatabase.child(DBInfo.TBL_POST).child(postId);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Post post = dataSnapshot.getValue(Post.class);
                                String reportId = mDatabase.child(Report.TABLE_NAME).push().getKey();

                                Report new_report = new Report();
                                new_report.reportId = reportId;
                                new_report.reporterId = my_userId;
                                new_report.reportedId = post.userId;
                                new_report.reportContent = content_report.getText().toString();

                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put("/" + Report.TABLE_NAME + "/" + reportId, new_report);
                                mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideProgressHUD();
                                        alertMessage("","Thanks for your report.");
                                    }
                                });
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void downloadMedia(int index, String postId) {
        Query query = mDatabase.child(DBInfo.TBL_POST).child(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if(isConnectingToInternet()){
                    showProgressHUD("Downloading...");
                    DownloadTask dt = new DownloadTask(context, post.mediaType, post.mediaUrl);
                    dt.setOnDownloadStatusListener(new DownloadTask.OnDownloadStatusListener() {
                        @Override
                        public void onDownloadCompleted() {
                            hideProgressHUD();
                            Toast.makeText(PostViewActivity.this, "Download Completed! Check directory named 'Footprints' on external storage.",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onDownloadFailed() {
                            hideProgressHUD();
                            Toast.makeText(PostViewActivity.this, "Download Failed!",Toast.LENGTH_LONG).show();
                        }
                    });
                }else {
                    Toast.makeText(context, "Oops!! There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private ValueEventListener getPostInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            postRecyclerAdapter.clear();
            if(dataSnapshot.exists()){
                postList.clear();
                Post post = dataSnapshot.getValue(Post.class);
                postList.add(post);
                Query getUser = mDatabase.child(DBInfo.TBL_USER).child(post.userId);
                getUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        text_title.setText(user.getName());
                        Utils.setAvatarImage(user,getApplicationContext(),img_avatar);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Collections.sort(postList);
                postRecyclerAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postInfo.removeEventListener(getPostInfoListener);
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

    @Override
    public void onDownloadCompleted() {
        hideProgressHUD();
    }

    @Override
    public void onDownloadFailed() {
        hideProgressHUD();
    }
}
