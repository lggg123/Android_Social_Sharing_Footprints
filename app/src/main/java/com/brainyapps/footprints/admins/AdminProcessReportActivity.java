package com.brainyapps.footprints.admins;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.Report;
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

public class AdminProcessReportActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView btn_back;
    private TextView title;
    private CircleImageView report_avatar;
    private TextView content;
    private TextView reported_by;
    private RelativeLayout ban_user;
    private RelativeLayout remove_report;

    private DatabaseReference mDatabase;
    private String reportID;
    private String reportedUser;
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
        setContentView(R.layout.activity_admin_process_report);

        btn_back = (ImageView)findViewById(R.id.admin_process_report_btn_back);
        btn_back.setOnClickListener(this);
        title = (TextView)findViewById(R.id.admin_process_report_title);
        report_avatar = (CircleImageView)findViewById(R.id.admin_reported_user_image);
        content = (TextView) findViewById(R.id.admin_process_report_reason_content);
        reported_by = (TextView) findViewById(R.id.admin_process_report_reason_reporter);
        ban_user = (RelativeLayout) findViewById(R.id.admin_process_report_ban_btn);
        ban_user.setOnClickListener(this);
        remove_report = (RelativeLayout) findViewById(R.id.admin_process_report_delete_report_btn);
        remove_report.setOnClickListener(this);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference();
                reportID = bundle.getString(IntentExtra.REPORT_ID);
                if(TextUtils.isEmpty(reportID)){
                    return;
                }
                DatabaseReference ref = mDatabase.child(DBInfo.TBL_REPORT).child(reportID);
                showProgressHUD("");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Report report = dataSnapshot.getValue(Report.class);
                            content.setText(report.reportContent);
                            reportedUser = report.reportedId;

                            DatabaseReference ref_reported_user = mDatabase.child(DBInfo.TBL_USER).child(report.reportedId);
                            ref_reported_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        User reported_user = dataSnapshot.getValue(User.class);
                                        title.setText(reported_user.getName());
                                        Utils.setAvatarImage(reported_user,getApplicationContext(),report_avatar);
                                    }
                                    hideProgressHUD();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    hideProgressHUD();
                                }
                            });

                            DatabaseReference ref_reporter_user = mDatabase.child(DBInfo.TBL_USER).child(report.reporterId);
                            ref_reporter_user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        User reporter = dataSnapshot.getValue(User.class);
                                        reported_by.setText("REPORTED BY "+reporter.getName());
                                    }
                                    hideProgressHUD();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    hideProgressHUD();
                                }
                            });
                        } else {
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_process_report_btn_back:
                super.onBackPressed();
                break;
            case R.id.admin_process_report_ban_btn:
                banUser();
                break;
            case R.id.admin_process_report_delete_report_btn:
                deleteReport();
                break;
            default:
                break;
        }
    }

    public void banUser(){
        AlertFactory.showAlert(this, "Ban User", "Are you sure want to ban this?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(final AlertDialog dialog) {
                mDatabase.child(DBInfo.TBL_REPORT).child(reportID).setValue(null);
                mDatabase.child(DBInfo.TBL_USER).child(reportedUser).child("banned").setValue(1);
                alertMessage("","The user successfully banned.");
                callback();
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

    public void deleteReport(){
        AlertFactory.showAlert(this, "Delete Report", "Are you sure want to delete this report?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(final AlertDialog dialog) {
                mDatabase.child(DBInfo.TBL_REPORT).child(reportID).setValue(null);
                alertMessage("","The report successfully deleted.");
                callback();
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

    private void callback(){
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
