package com.brainyapps.footprints.admins;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.R;

public class AdminMainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView btn_admin_setting;
    private RelativeLayout btn_admin_users;
    private RelativeLayout btn_admin_reported_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        btn_admin_setting = (ImageView) findViewById(R.id.admin_main_btn_setting);
        btn_admin_setting.setOnClickListener(this);
        btn_admin_users = (RelativeLayout) findViewById(R.id.admin_mian_users_tab);
        btn_admin_users.setOnClickListener(this);
        btn_admin_reported_users = (RelativeLayout) findViewById(R.id.admin_mian_reported_users_tab);
        btn_admin_reported_users.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_main_btn_setting:
                Intent adminSettingIntent = new Intent(this, AdminSettingActivity.class);
                startActivity(adminSettingIntent);
                break;
            case R.id.admin_mian_users_tab:
                Intent adminUserIntent = new Intent(this, AdminUserActivity.class);
                startActivity(adminUserIntent);
                break;
            case R.id.admin_mian_reported_users_tab:
                Intent adminReportIntent = new Intent(this, AdminReportedUsersActivity.class);
                startActivity(adminReportIntent);
                break;
            default:
                break;
        }
    }
}
