package com.brainyapps.footprints.admins;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.adapters.AdminReportyRecyclerAdapter;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Report;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.ArrayList;

public class AdminReportedUsersActivity extends AppCompatActivity implements View.OnClickListener, AdminReportyRecyclerAdapter.OnClickItemListener{

    private ArrayList<Report> reportList = new ArrayList<>();
    private RecyclerView recyclerView;
    private AdminReportyRecyclerAdapter adminReportRecyclerAdapter;
    private DatabaseReference report_info;

    private ImageView btn_back;
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
        setContentView(R.layout.activity_admin_reported_users);

        btn_back = (ImageView)findViewById(R.id.admin_reported_users_btn_back);
        btn_back.setOnClickListener(this);

        adminReportRecyclerAdapter = new AdminReportyRecyclerAdapter(reportList);
        recyclerView = (RecyclerView) findViewById(R.id.admin_reported_user_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adminReportRecyclerAdapter);
        adminReportRecyclerAdapter.setOnClickItemListener(this);
    }

    private ValueEventListener getReportInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            TextView no_result = (TextView) findViewById(R.id.admin_report_no_user);
            if(dataSnapshot.exists()){
                no_result.setVisibility(View.GONE);
                for (DataSnapshot reportInfo : dataSnapshot.getChildren()) {
                    Report report = reportInfo.getValue(Report.class);
                    reportList.add(report);
                }
                adminReportRecyclerAdapter.notifyDataSetChanged();
                hideProgressHUD();
            }
            else {
                no_result.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_reported_users_btn_back:
                super.onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelectProfile(int index, String reportId) {
        Intent process_report_intent = new Intent(this, AdminProcessReportActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.REPORT_ID, reportId);
        process_report_intent.putExtras(bundle);
        startActivity(process_report_intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        adminReportRecyclerAdapter.clear();
        report_info = FirebaseDatabase.getInstance().getReference().child(DBInfo.TBL_REPORT);
        report_info.addValueEventListener(getReportInfoListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        report_info.removeEventListener(getReportInfoListener);
    }
}
