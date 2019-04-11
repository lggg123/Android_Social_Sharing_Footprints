package com.brainyapps.footprints.admins;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brainyapps.footprints.EditprofileActivity;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import java.util.HashMap;
import java.util.Map;

public class AdminEditProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView btn_back;
    private TextView btn_save;
    private EditText email;
    private EditText old_pw;
    private EditText new_pw;
    private EditText confirm_pw;

    public static String TAG = "Admin Editprofile Activity";

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
    private final String myUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final String myCurrentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        btn_back = (ImageView) findViewById(R.id.admin_editprofile_btn_back);
        btn_save = (TextView) findViewById(R.id.admin_editpost_btn_save);
        email = (EditText) findViewById(R.id.admin_profile_edit_email);
        old_pw =(EditText)findViewById(R.id.admin_profile_edit_old_password);
        new_pw = (EditText)findViewById(R.id.admin_profile_edit_new_password);
        confirm_pw = (EditText)findViewById(R.id.admin_profile_edit_confirm_password);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_EMAIL).child(myUserID).child("email");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    email.setText(dataSnapshot.getValue().toString());
                else
                    email.setText("");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.admin_editprofile_btn_back:
                super.onBackPressed();
                break;
            case R.id.admin_editpost_btn_save:
                saveProfile();
                break;
        }
    }

    public void saveProfile(){
        if(checkValidation()){
            if(!TextUtils.isEmpty(new_pw.getText().toString())){
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(myCurrentEmail, old_pw.getText().toString());

                showProgressHUD("");
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            user.updatePassword(new_pw.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Map<String, Object> userUpdates = new HashMap<>();
                                                        userUpdates.put("/" + DBInfo.TBL_EMAIL + "/" + myUserID +"/" + "email", email.getText().toString());
                                                        FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                alertMessage("","Your profile successfully changed.");
                                                            }
                                                        });
                                                        Log.d(TAG, "Password updated");
                                                    } else {
                                                        Log.d(TAG, "Error password not updated");
                                                    }
                                                    hideProgressHUD();
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    alertMessage("Error","Old password is incorrect.");
                                    old_pw.requestFocus();
                                    hideProgressHUD();
                                    return;

                                }
                            }
                        });
            }
        }
    }

    public boolean checkValidation(){
        int error = 0;
        if(TextUtils.isEmpty(email.getText().toString())){
            alertMessage("Error","Please input email address.");
            email.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(old_pw.getText().toString())){
            alertMessage("Error","Please input old password.");
            old_pw.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(new_pw.getText().toString())){
            alertMessage("Error","Please input new password.");
            new_pw.requestFocus();
            return false;
        }else {
            String password = new_pw.getText().toString();
            if (!Utils.overLength(password)) {
                error++;
            }
            if (!Utils.containsNumber(password)) {
                error++;
            }

            if (!Utils.containsCharacter(password)) {
                error++;
            }
            if(error > 0){
                alertMessage("Error","Password type is incorrect. Password must contains a letter and a number. And it muct contains at least 6 characters.");
                new_pw.requestFocus();
                return false;
            }else {
                if(TextUtils.isEmpty(confirm_pw.getText().toString())){
                    alertMessage("Error","Please confirm new password.");
                    confirm_pw.requestFocus();
                    return false;
                }else {
                    if(!new_pw.getText().toString().equals(confirm_pw.getText().toString())){
                        alertMessage("Error","Confirm password is incorrect. Please try again.");
                        confirm_pw.requestFocus();
                        return false;
                    }
                }
            }
        }

        return true;
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
