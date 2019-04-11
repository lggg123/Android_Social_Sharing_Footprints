package com.brainyapps.footprints;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.fragments.SignupEmailFragment;
import com.brainyapps.footprints.fragments.SignupInfoFragment;
import com.brainyapps.footprints.fragments.SignupPasswordFragment;
import com.brainyapps.footprints.fragments.SignupRepasswordFragment;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.walnutlabs.android.ProgressHUD;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener, SignupEmailFragment.OnSignupEmailListener, SignupPasswordFragment.OnSignupPasswordListener, SignupRepasswordFragment.OnSignupRePasswordListener, SignupInfoFragment.OnSignupInfoListener{

    public static final String TAG = SignupActivity.class.getSimpleName();

    private static final int FRAGMENT_SIGNUPEMAIL_TAG = 0;
    private static final int FRAGMENT_SIGNUPPASSWORD_TAG = 1;
    private static final int FRAGMENT_SIGNUPREPASSWORD_TAG = 2;
    private static final int FRAGMENT_SIGNUPINFO_TAG = 3;

    private Map<String, Fragment> mFragmentMap;

    private Fragment mFragment;

    private int currentPosition = 0;

    private String mPassword;
    private String mEmail;
    private FirebaseAuth auth;
    private StorageReference storePhoto;
    private String photoUrl;

    private ImageView btn_back;

    public ProgressHUD mProgressDialog;

    public void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(SignupActivity.this, text, true);
        mProgressDialog.show();
    }

    public void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btn_back = (ImageView) findViewById(R.id.signup_back);
        btn_back.setOnClickListener(this);

        mPassword="";
        mEmail="";
        storePhoto = FirebaseStorage.getInstance().getReference();
        photoUrl = "";

        mFragmentMap = new HashMap<>();
        mFragmentMap.put(SignupEmailFragment.FRAGMENT_TAG, new SignupEmailFragment().newInstance(this));
        mFragmentMap.put(SignupPasswordFragment.FRAGMENT_TAG, new SignupPasswordFragment().newInstance(this));
        mFragmentMap.put(SignupRepasswordFragment.FRAGMENT_TAG, new SignupRepasswordFragment().newInstance(this));
        mFragmentMap.put(SignupInfoFragment.FRAGMENT_TAG, new SignupInfoFragment().newInstance(this));

        showFragment(FRAGMENT_SIGNUPEMAIL_TAG, true);
    }

    public void showFragment(int position, Boolean isPushed) {
        mFragment = null;
        currentPosition = position;

        switch (position) {
            case FRAGMENT_SIGNUPEMAIL_TAG:
                mFragment = mFragmentMap.get(SignupEmailFragment.FRAGMENT_TAG);
                ((SignupEmailFragment)mFragment).setOnSignupEmailListener(this);
                break;
            case FRAGMENT_SIGNUPPASSWORD_TAG:
                mFragment = mFragmentMap.get(SignupPasswordFragment.FRAGMENT_TAG);
                ((SignupPasswordFragment)mFragment).setOnSignupPasswordListener(this);
                break;
            case FRAGMENT_SIGNUPREPASSWORD_TAG:
                mFragment = mFragmentMap.get(SignupRepasswordFragment.FRAGMENT_TAG);
                ((SignupRepasswordFragment)mFragment).setPassword(mPassword);
                ((SignupRepasswordFragment)mFragment).setOnSignupRePasswordListener(this);
                break;
            case FRAGMENT_SIGNUPINFO_TAG:
                mFragment = mFragmentMap.get(SignupInfoFragment.FRAGMENT_TAG);
                ((SignupInfoFragment)mFragment).setOnSignupInfoListener(this);
                break;
            default:
                break;
        }

        if (mFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.signup_fragment, mFragment).commit();
        } else {
            Log.e(TAG, "Error in creating fragment");
        }
    }

    @Override
    public void onBackSignin() {
        super.onBackPressed();
    }

    @Override
    public void onNextPassword(String email) {
        mEmail = email;
        showFragment(FRAGMENT_SIGNUPPASSWORD_TAG, true);
    }

    @Override
    public void onNextRepassword(String password) {
        mPassword = password;
        showFragment(FRAGMENT_SIGNUPREPASSWORD_TAG, true);
    }

    @Override
    public void onNextInfo() {
        showFragment(FRAGMENT_SIGNUPINFO_TAG, true);
    }

    @Override
    public void onSignupDone(final String firstName, final String lastName, final Bitmap photo, final String brif, final String address) {
        showProgressHUD("");
        auth.getInstance().createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            final FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                if(photo != null){
                                    uploadAvatar(user,firstName,lastName,photo,brif, address);
                                }else {
                                    setInfoEmail(user,firstName,lastName,"",brif, address);
                                }
                            }
                        }
                    }
                });
    }

    public void uploadAvatar(final FirebaseUser user, final String firstName, final String lastName, Bitmap photo, final String brif, final String address){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        Long tsLong = System.currentTimeMillis();
        StorageReference filepath = storePhoto.child("Avatar").child(tsLong+".jpg");
        filepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                photoUrl = downloadUri.toString();
                setInfoEmail(user,firstName,lastName,photoUrl,brif, address);
            }
        });
    }

    public void setInfoEmail(FirebaseUser user, String firstName, String lastName, String photoUrl, String brif, String address){
        if (FirebaseManager.getInstance().MyUsage == null)
            FirebaseManager.getInstance().MyUsage = new User();

        FirebaseManager.getInstance().MyUsage.userId = user.getUid();
        FirebaseManager.getInstance().MyUsage.userEmail = mEmail;
        FirebaseManager.getInstance().MyUsage.userPassword = mPassword;
        FirebaseManager.getInstance().MyUsage.firstName = firstName;
        FirebaseManager.getInstance().MyUsage.lastName = lastName;
        FirebaseManager.getInstance().MyUsage.brif = brif;
        FirebaseManager.getInstance().MyUsage.address = address;
        FirebaseManager.getInstance().MyUsage.photoUrl = photoUrl;
        FirebaseManager.getInstance().MyUsage.privacy = "On";
        FirebaseManager.getInstance().MyUsage.facebookId = "";
        FirebaseManager.getInstance().MyUsage.signupType = "Email";
//        FirebaseManager.getInstance().MyUsage.posts = new HashMap<>();
//        FirebaseManager.getInstance().MyUsage.followers = new HashMap<>();
//        FirebaseManager.getInstance().MyUsage.followings = new HashMap<>();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + user.getUid(), FirebaseManager.getInstance().MyUsage.toMap());
        userUpdates.put("/" + DBInfo.TBL_EMAIL + "/" + user.getUid() + "/" + DBInfo.EMAIL, mEmail);
        userUpdates.put("/" + DBInfo.TBL_EMAIL + "/" + user.getUid() + "/" + DBInfo.ROLE, "user");
        FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressHUD();
                startActivity(new Intent(SignupActivity.this, OnboardingActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signup_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPosition == 0) {
            setResult(Activity.RESULT_CANCELED);
            super.onBackPressed();
        } else {
            if (currentPosition == (FRAGMENT_SIGNUPINFO_TAG + 1)) {
                setResult(Activity.RESULT_CANCELED);
            } else {
                if (mFragment instanceof SignupEmailFragment) {
                    ((SignupEmailFragment)mFragment).initialize();
                }
                if (mFragment instanceof SignupInfoFragment) {
                    ((SignupInfoFragment)mFragment).initialize();
                }
                if (mFragment instanceof SignupPasswordFragment) {
                    ((SignupPasswordFragment)mFragment).initialize();
                }
                if (mFragment instanceof SignupRepasswordFragment) {
                    ((SignupRepasswordFragment)mFragment).initialize();
                }
                currentPosition--;
                showFragment(currentPosition, false);
            }
        }
    }
}

