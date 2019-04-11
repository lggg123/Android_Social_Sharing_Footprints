package com.brainyapps.footprints.admins;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.SigninActivity;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class AdminSettingActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView btn_back;
    private RelativeLayout edit_profile;
    private RelativeLayout log_out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setting);

        btn_back = (ImageView)findViewById(R.id.admin_setting_back);
        btn_back.setOnClickListener(this);
        edit_profile = (RelativeLayout)findViewById(R.id.admin_setting_edit_profile);
        edit_profile.setOnClickListener(this);
        log_out = (RelativeLayout)findViewById(R.id.admin_setting_logout);
        log_out.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_setting_back:
                super.onBackPressed();
                break;
            case R.id.admin_setting_edit_profile:
                Intent adminProfileEditIntent = new Intent(this, AdminEditProfileActivity.class);
                startActivity(adminProfileEditIntent);
                break;
            case R.id.admin_setting_logout:
                logout();
                break;
        }
    }

    public void logout(){
        if (FirebaseAuth.getInstance() != null) {

            if (FirebaseManager.getInstance().getGoogleApiClient() != null)
                FirebaseManager.getInstance().getGoogleApiClient().disconnect();

            FirebaseManager.getInstance().clear();
            FirebaseAuth.getInstance().signOut();

            Firebase firebase = new Firebase("https://footprints-fef2a.firebaseio.com/");
            firebase.unauth();

            Intent loginintent = new Intent(this, SigninActivity.class);
            startActivity(loginintent);
            finish();
        }
        Intent signinIntent = new Intent(this, SigninActivity.class);
        finish();
        startActivity(signinIntent);
    }
}
