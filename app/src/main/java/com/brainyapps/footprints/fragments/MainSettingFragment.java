package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brainyapps.footprints.AboutActivity;
import com.brainyapps.footprints.EditpostActivity;
import com.brainyapps.footprints.EditprofileActivity;
import com.brainyapps.footprints.MainActivity;
import com.brainyapps.footprints.PrivacyActivity;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.TermsActivity;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.brainyapps.footprints.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

public class MainSettingFragment extends android.app.Fragment implements View.OnClickListener{

    public static final String TAG = MainSettingFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_main_setting_fragment_tag";

    private static Context mContext;
    private static String my_id;
    private User myprofile;

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = ProgressHUD.show(mContext, text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String email = charSequence.toString();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public static android.app.Fragment newInstance(Context context, String user_id) {
        mContext = context;
        my_id = user_id;
        android.app.Fragment f = new MainSettingFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(DBInfo.TBL_USER).child(my_id);
        showProgressHUD("");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    myprofile = dataSnapshot.getValue(User.class);
                }
                hideProgressHUD();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_setting, container, false);

        View vSettingEditProfile = (View)rootView.findViewById(R.id.setting_edit_profile);
        vSettingEditProfile.setOnClickListener(this);
        View vSettingAboutApp = (View)rootView.findViewById(R.id.setting_about_the_app);
        vSettingAboutApp.setOnClickListener(this);
        View vSettingPrivacyPolicy = (View)rootView.findViewById(R.id.setting_privacy_policy);
        vSettingPrivacyPolicy.setOnClickListener(this);
        View vSettingTerms = (View)rootView.findViewById(R.id.setting_terms_conditions);
        vSettingTerms.setOnClickListener(this);
        View vSettingReportProblem = (View)rootView.findViewById(R.id.setting_report_problem);
        vSettingReportProblem.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting_edit_profile:
                callEditProfile();
                break;
            case R.id.setting_about_the_app:
                Intent about_app_intent = new Intent(mContext, AboutActivity.class);
                startActivity(about_app_intent);
                break;
            case R.id.setting_privacy_policy:
                Intent privacy_policy_intent = new Intent(mContext, PrivacyActivity.class);
                startActivity(privacy_policy_intent);
                break;
            case R.id.setting_terms_conditions:
                Intent terms_conditions_intent = new Intent(mContext, TermsActivity.class);
                startActivity(terms_conditions_intent);
                break;
            case R.id.setting_report_problem:
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
/* Fill it with Data */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"footprintsapphelp@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

/* Send it off to the Activity-Chooser */
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            default:
                break;
        }
    }

    public void callEditProfile(){
        Intent edit_profile_intent = new Intent(mContext, EditprofileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.USER_ID, my_id);
//        bundle.putParcelable(IntentExtra.USER_PROFILE, myprofile);
        edit_profile_intent.putExtras(bundle);
        startActivity(edit_profile_intent);
    }
}

