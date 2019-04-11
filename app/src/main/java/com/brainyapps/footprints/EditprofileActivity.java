package com.brainyapps.footprints;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.brainyapps.footprints.utils.ImagePicker;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.walnutlabs.android.ProgressHUD;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditprofileActivity extends AppCompatActivity implements View.OnClickListener{

    private final int PLACE_PICKER_REQUEST = 30;
    private final int REQUEST_IMAGE_CONTENT = 31;

    private User myprofile;
    private ImageView btn_back;
    private EditText first_name;
    private EditText last_name;
    private EditText old_password;
    private EditText new_password;
    private EditText confirm_password;
    private EditText brifBox;
    private ImageView image_on;
    private ImageView image_off;
    private CircleImageView circleImageView;
    private TextView btn_save;
    private String newAddress = "";
    private RelativeLayout getAddress;

    private Bitmap bitmap;

    Uri uriProfileImage;
    private StorageReference storePhoto;
    private String photoUrl;
    private String oldAddress;

    private FirebaseUser user;
    public static String TAG = "EditprofileActivity";

    private ProgressHUD mProgressDialog;

    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(EditprofileActivity.this, text, true);
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
        setContentView(R.layout.activity_editprofile);

        circleImageView = (CircleImageView) findViewById(R.id.edit_profile_image);
        btn_back = (ImageView) findViewById(R.id.editprofile_btn_back);
        btn_back.setOnClickListener(this);
        btn_save = (TextView) findViewById(R.id.edit_profile_btn_save);
        image_on = (ImageView) findViewById(R.id.private_account_switch_on);
        image_off = (ImageView) findViewById(R.id.private_account_switch_off);

        btn_save.setOnClickListener(this);
        image_on.setOnClickListener(this);
        image_off.setOnClickListener(this);
        circleImageView.setOnClickListener(this);

        first_name = (EditText) findViewById(R.id.profile_edit_first_name);
        last_name = (EditText) findViewById(R.id.profile_edit_last_name);
        old_password = (EditText) findViewById(R.id.profile_edit_old_password);
        new_password = (EditText) findViewById(R.id.profile_edit_new_password);
        confirm_password = (EditText) findViewById(R.id.profile_edit_confirm_password);
        brifBox = (EditText) findViewById(R.id.profile_edit_brif);
        getAddress = (RelativeLayout) findViewById(R.id.profile_edit_address);
        getAddress.setOnClickListener(this);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String my_userID = bundle.getString(IntentExtra.USER_ID);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query query = ref.child(DBInfo.TBL_USER).child(my_userID);
                showProgressHUD("");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            myprofile = dataSnapshot.getValue(User.class);
                            Utils.setAvatarImage(myprofile,getApplicationContext(),circleImageView);

                            first_name.setText(myprofile.firstName);
                            first_name.setSelection(first_name.getText().length());
                            last_name.setText(myprofile.lastName);
                            brifBox.setText(myprofile.brif);

                            if(myprofile.privacy.equals("On")){
                                image_on.setVisibility(View.VISIBLE);
                                image_off.setVisibility(View.GONE);
                            }else {
                                image_on.setVisibility(View.GONE);
                                image_off.setVisibility(View.VISIBLE);
                            }

                            if(!myprofile.signupType.equals("Email")){
                                old_password.setVisibility(View.GONE);
                                new_password.setVisibility(View.GONE);
                                confirm_password.setVisibility(View.GONE);
                            }
                            storePhoto = FirebaseStorage.getInstance().getReference();
                            photoUrl = myprofile.photoUrl;
                            oldAddress = myprofile.address;
                        }
                        hideProgressHUD();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void editprofile_goto_backpage(){
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editprofile_btn_back:
                editprofile_goto_backpage();
                break;
            case R.id.edit_profile_btn_save:
                saveProfile();
                break;
            case R.id.private_account_switch_on:
                image_on.setVisibility(View.GONE);
                image_off.setVisibility(View.VISIBLE);
                break;
            case R.id.private_account_switch_off:
                image_on.setVisibility(View.VISIBLE);
                image_off.setVisibility(View.GONE);
                break;
            case R.id.edit_profile_image:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_CONTENT);
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_IMAGE_CONTENT);
                break;
            case R.id.profile_edit_address:
                getLocation();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CONTENT && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
            circleImageView.setImageBitmap(bitmap);
        }
        if (requestCode == PLACE_PICKER_REQUEST &&resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            newAddress = place.getAddress().toString();
        }
    }

    public void getLocation(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void saveProfile() {
        if(checkValidation()){
            AlertFactory.showAlert(this, "Edit Profile", "Are you sure want to save now?", "YES", "NO", new AlertFactoryClickListener() {
                @Override
                public void onClickYes(final AlertDialog dialog) {
                    if(bitmap!=null){
                        dialog.dismiss();
                        Long tsLong = System.currentTimeMillis();
                        showProgressHUD("");

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] data = stream.toByteArray();

                        StorageReference filepath = storePhoto.child("Avatar").child(tsLong+".jpg");
                        filepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                photoUrl = downloadUri.toString();
                                saveAction();
                            }
                        });
                    }else {
                        saveAction();
                        dialog.dismiss();
                    }
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
    }

    public void saveAction(){
        FirebaseManager.getInstance().MyUsage = myprofile;
        FirebaseManager.getInstance().MyUsage.firstName = first_name.getText().toString();
        FirebaseManager.getInstance().MyUsage.lastName = last_name.getText().toString();
        FirebaseManager.getInstance().MyUsage.photoUrl = photoUrl;
        FirebaseManager.getInstance().MyUsage.brif = brifBox.getText().toString();
        if(image_on.getVisibility() == View.VISIBLE){
            FirebaseManager.getInstance().MyUsage.privacy = "On";
        }else {
            FirebaseManager.getInstance().MyUsage.privacy = "Off";
        }
        if(!newAddress.equals("")){
            oldAddress = newAddress;
            FirebaseManager.getInstance().MyUsage.address = oldAddress;
        }

        if(!TextUtils.isEmpty(new_password.getText().toString())){
            user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(myprofile.userEmail, myprofile.userPassword);

            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseManager.getInstance().MyUsage.userPassword = new_password.getText().toString();
                                            Map<String, Object> userUpdates = new HashMap<>();
                                            userUpdates.put("/" + DBInfo.TBL_USER + "/" + myprofile.userId, FirebaseManager.getInstance().MyUsage.toMap());
                                            FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    alertMessage("","Your Profile successfully changed.");
                                                }
                                            });
                                            Log.d(TAG, "Password updated");
                                        } else {
                                            Log.d(TAG, "Error password not updated");
                                        }
                                        hideProgressHUD();
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error auth failed");
                            }
                        }
                    });
        }else {
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("/" + DBInfo.TBL_USER + "/" + myprofile.userId, FirebaseManager.getInstance().MyUsage.toMap());
            FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    alertMessage("","Your Profile successfully uploaded.");
                }
            });
            hideProgressHUD();
        }
    }

    public boolean checkValidation(){
        int error = 0;
        if (TextUtils.isEmpty(first_name.getText().toString()) || first_name.getText().toString().length() < 1) {
            alertMessage("Error","You must input First Name.");
            first_name.requestFocus();
            return false;
        }else {
            String firstName = first_name.getText().toString();
            firstName = firstName.replaceFirst("^\\s*", "");
            firstName = firstName.replaceAll("\\s++$", "");
            firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
            first_name.setText(firstName);
        }
        if (TextUtils.isEmpty(last_name.getText().toString()) || last_name.getText().toString().length() < 1) {
            alertMessage("Error","You must input Last Name.");
            first_name.requestFocus();
            return false;
        }else {
            String lastName = last_name.getText().toString();
            lastName = lastName.replaceFirst("^\\s*", "");
            lastName = lastName.replaceAll("\\s++$", "");
            lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1);
            last_name.setText(lastName);
        }
        if (TextUtils.isEmpty(old_password.getText().toString())&& TextUtils.isEmpty(new_password.getText().toString()) && TextUtils.isEmpty(confirm_password.getText().toString())){

        }else {
            if(TextUtils.isEmpty(old_password.getText().toString())){
                alertMessage("Error","Please input your current password.");
                old_password.requestFocus();
                return false;
            }else {
                if (!old_password.getText().toString().equals(myprofile.userPassword)) {
                    alertMessage("Error","Old password is incorrect");
                    old_password.requestFocus();
                    return false;
                }
            }
            if (!TextUtils.isEmpty(new_password.getText().toString())) {
                String new_pw = new_password.getText().toString();
                if (!Utils.overLength(new_pw)) {
                    error++;
                }
                if (!Utils.containsNumber(new_pw)) {
                    error++;
                }

                if (!Utils.containsCharacter(new_pw)) {
                    error++;
                }
                if(error > 0){
                    alertMessage("Error","Password type is incorrect. Password must contains a letter and a number. And it muct contains at least 6 characters.");
                    new_password.requestFocus();
                    return false;
                }else {
                    if(TextUtils.isEmpty(confirm_password.getText().toString())){
                        alertMessage("Error","Please confirm new password.");
                        confirm_password.requestFocus();
                        return false;
                    }else {
                        if(!new_pw.equals(confirm_password.getText().toString())){
                            alertMessage("Error","Confirm password is incorrect. Please try again.");
                            confirm_password.requestFocus();
                            return false;
                        }
                    }
                }
            }else {
                alertMessage("Error","Please type new password.");
                new_password.requestFocus();
                return false;
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
