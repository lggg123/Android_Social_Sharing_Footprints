package com.brainyapps.footprints.admins;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.adapters.AdminUserRecyclerAdaper;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Report;
import com.brainyapps.footprints.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;

public class AdminUserActivity extends AppCompatActivity implements View.OnClickListener, AdminUserRecyclerAdaper.OnClickItemListener{
    private ImageView btn_back;
    private RelativeLayout btn_all_user;
    private RelativeLayout btn_banned_user;

    private LinearLayout all_user_field;
    private LinearLayout banned_user_field;
    private TextView all_user_text;
    private TextView banned_user_text;

    private RelativeLayout all_user_bottom;
    private RelativeLayout banned_user_bottom;


    private ArrayList<User> allUserList = new ArrayList<>();
    private ArrayList<User> bannedUserList = new ArrayList<>();
    private RecyclerView allUserRecyclerView;
    private RecyclerView bannedUserRecyclerView;
    private AdminUserRecyclerAdaper adminUserRecyclerAdaper;
    private AdminUserRecyclerAdaper adminBannedUserRecyclerAdaper;
    private DatabaseReference user_info;

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
        setContentView(R.layout.activity_admin_user);

        btn_back = (ImageView) findViewById(R.id.admin_user_btn_back);
        btn_back.setOnClickListener(this);
        btn_all_user = (RelativeLayout) findViewById(R.id.admin_user_tab_all_users);
        btn_all_user.setOnClickListener(this);
        btn_banned_user = (RelativeLayout) findViewById(R.id.admin_user_tab_banned_users);
        btn_banned_user.setOnClickListener(this);

        all_user_field = (LinearLayout) findViewById(R.id.admin_user_field);
        banned_user_field = (LinearLayout) findViewById(R.id.admin_banned_user_field);
        all_user_text = (TextView) findViewById(R.id.admin_user_tab_all_users_text);
        banned_user_text = (TextView) findViewById(R.id.admin_user_tab_banned_users_text);

        all_user_bottom = (RelativeLayout) findViewById(R.id.admin_user_tab_bottom_all_users);
        banned_user_bottom = (RelativeLayout) findViewById(R.id.admin_user_tab_bottom_banned_users);

        adminUserRecyclerAdaper = new AdminUserRecyclerAdaper(allUserList);
        allUserRecyclerView = (RecyclerView) findViewById(R.id.admin_all_user_list_view);
        allUserRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        allUserRecyclerView.setAdapter(adminUserRecyclerAdaper);
        adminUserRecyclerAdaper.setOnClickItemListener(this);

        adminBannedUserRecyclerAdaper = new AdminUserRecyclerAdaper(bannedUserList);
        bannedUserRecyclerView = (RecyclerView) findViewById(R.id.admin_banned_user_list_view);
        bannedUserRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        bannedUserRecyclerView.setAdapter(adminBannedUserRecyclerAdaper);
        adminBannedUserRecyclerAdaper.setOnClickItemListener(this);

        showAllUserTab();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_user_btn_back:
                super.onBackPressed();
                break;
            case R.id.admin_user_tab_all_users:
                showAllUserTab();
                break;
            case R.id.admin_user_tab_banned_users:
                showBannedUserTab();
                break;
            default:
                break;
        }
    }

    private void showAllUserTab(){
        all_user_field.setVisibility(View.VISIBLE);
        banned_user_field.setVisibility(View.GONE);
        banned_user_text.setTextColor(Color.parseColor("#2f779d"));
        banned_user_bottom.setBackgroundColor(Color.parseColor("#52B0E3"));

        all_user_text.setTextColor(Color.parseColor("#FFFFFF"));
        all_user_bottom.setBackgroundColor(Color.parseColor("#FFFFFF"));


    }

    private void showBannedUserTab(){
        all_user_field.setVisibility(View.GONE);
        banned_user_field.setVisibility(View.VISIBLE);
        all_user_text.setTextColor(Color.parseColor("#2f779d"));
        all_user_bottom.setBackgroundColor(Color.parseColor("#52B0E3"));

        banned_user_text.setTextColor(Color.parseColor("#FFFFFF"));
        banned_user_bottom.setBackgroundColor(Color.parseColor("#FFFFFF"));
    }

    @Override
    public void onSelectProfile(int index, String userId) {
        Intent ban_user_intent = new Intent(this, AdminBanUsersActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, userId);
        ban_user_intent.putExtras(bundle);
        startActivity(ban_user_intent);
    }

    private ValueEventListener getUserInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                TextView no_result = (TextView) findViewById(R.id.admin_user_no_result);
                if(dataSnapshot.exists()){
                    no_result.setVisibility(View.GONE);
                    for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                        User user = userInfo.getValue(User.class);
                        if(user.banned == 1){
                            bannedUserList.add(user);
                        }else {
                            allUserList.add(user);
                        }
                    }
                    adminUserRecyclerAdaper.notifyDataSetChanged();
                    adminBannedUserRecyclerAdaper.notifyDataSetChanged();
                }
                else {
                    no_result.setVisibility(View.VISIBLE);
                }
            }
            hideProgressHUD();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    public void onResume() {
        super.onResume();
        adminUserRecyclerAdaper.clear();
        adminBannedUserRecyclerAdaper.clear();
        user_info = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_USER);
        showProgressHUD("");
        user_info.addValueEventListener(getUserInfoListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        user_info.removeEventListener(getUserInfoListener);
    }
}
