package com.brainyapps.footprints.admins;

import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminBanUsersActivity extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference mDatabase;
    private String selectedUserId;

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
        setContentView(R.layout.activity_admin_ban_users);

        final ImageView btn_back = (ImageView) findViewById(R.id.admin_ban_user_btn_back);
        btn_back.setOnClickListener(this);
        final TextView title = (TextView) findViewById(R.id.admin_user_ban_title);
        final CircleImageView avatar = (CircleImageView) findViewById(R.id.admin_selected_ban_user_image);
        final TextView email_addr = (TextView) findViewById(R.id.admin_ban_user_email);
        final TextView password =(TextView) findViewById(R.id.admin_ban_user_password);
        final RelativeLayout btn_ban = (RelativeLayout) findViewById(R.id.admin_ban_user_btn);
        btn_ban.setOnClickListener(this);
        final RelativeLayout btn_unban = (RelativeLayout) findViewById(R.id.admin_unban_user_btn);
        btn_unban.setOnClickListener(this);
        final TextView btn_ban_title = (TextView) findViewById(R.id.admin_ban_user_btn_title);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference();
                selectedUserId = bundle.getString(IntentExtra.USER_ID);
                if(TextUtils.isEmpty(selectedUserId)){
                    return;
                }
                DatabaseReference ref = mDatabase.child(DBInfo.TBL_USER).child(selectedUserId);
                showProgressHUD("");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            User user = dataSnapshot.getValue(User.class);
                            title.setText(user.getName());
                            email_addr.setText(user.userEmail);
                            password.setText(user.userPassword);
                            if(user.banned == 0){
                                btn_ban.setVisibility(View.VISIBLE);
                                btn_unban.setVisibility(View.GONE);
                            }else {
                                btn_ban.setVisibility(View.GONE);
                                btn_unban.setVisibility(View.VISIBLE);
                            }
                            Utils.setAvatarImage(user,getApplicationContext(),avatar);
                        }
                        hideProgressHUD();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgressHUD();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_ban_user_btn_back:
                callBackActivity();
                break;
            case R.id.admin_ban_user_btn:
                banUser();
                break;
            case R.id.admin_unban_user_btn:
                unbanUser();
                break;
            default:
                break;
        }
    }

    public void banUser(){
        AlertFactory.showAlert(this, "Ban User", "Are you sure want to ban this user?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(final AlertDialog dialog) {
                mDatabase.child(DBInfo.TBL_USER).child(selectedUserId).child("banned").setValue(1);
                callBackActivity();
            }

            @Override
            public void onClickNo(AlertDialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onClickDone(AlertDialog dialog) {
                dialog.dismiss();
            }
        });
    }

    public void unbanUser(){
        AlertFactory.showAlert(this, "Unban User", "Are you sure want to unban this user?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(final AlertDialog dialog) {
                mDatabase.child(DBInfo.TBL_USER).child(selectedUserId).child("banned").setValue(0);
                callBackActivity();
            }

            @Override
            public void onClickNo(AlertDialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onClickDone(AlertDialog dialog) {
                dialog.dismiss();
            }
        });
    }

    private void callBackActivity(){
        super.onBackPressed();
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
}
