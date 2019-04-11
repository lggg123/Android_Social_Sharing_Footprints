package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.ValidationCheck;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignupEmailFragment extends android.app.Fragment implements View.OnClickListener {

    public static final String TAG = SignupEmailFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_signup_email_fragment_tag";

    private static Context mContext;

    private ValidationCheck validEmailIndicator;
    private ValidationCheck usedEmailIndicator;

    private EditText editEmail;

    private RelativeLayout btnNext;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String email = charSequence.toString();

            if (!TextUtils.isEmpty(email)) {
                if (Utils.isValidEmail(email)) {
                    validEmailIndicator.success();
                    checkUsedEmail(email);
                } else {
                    btnNext.setEnabled(false);
                    validEmailIndicator.fail();
                    usedEmailIndicator.fail();
                }
            } else {
                btnNext.setEnabled(false);
                validEmailIndicator.fail();
                usedEmailIndicator.fail();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public static android.app.Fragment newInstance(Context context) {
        mContext = context;

        android.app.Fragment f = new SignupEmailFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_signup_email, container, false);

        validEmailIndicator = (ValidationCheck) rootView.findViewById(R.id.valid_email_check_left_icon);
        usedEmailIndicator = (ValidationCheck) rootView.findViewById(R.id.used_email_check_left_icon);

        btnNext = (RelativeLayout) rootView.findViewById(R.id.signup_email_button_next);
        btnNext.setOnClickListener(this);
        btnNext.setEnabled(false);

        editEmail = (EditText) rootView.findViewById(R.id.signup_edit_email);
        editEmail.setText("");
        editEmail.addTextChangedListener(mTextWatcher);

        return rootView;
    }

    private void checkUsedEmail(String email) {
        usedEmailIndicator.fail();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(DBInfo.TBL_EMAIL);
        query.orderByChild(DBInfo.EMAIL).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    usedEmailIndicator.success();
                    btnNext.setEnabled(true);
                    return;
                }
                usedEmailIndicator.fail();
                btnNext.setEnabled(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usedEmailIndicator.fail();
                btnNext.setEnabled(false);
            }
        });
    }

    public void initialize() {
        if (editEmail != null)
            editEmail.setText("");

        if (btnNext != null)
            btnNext.setEnabled(false);
    }

    public OnSignupEmailListener mListener;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_email_button_next:
                if(mListener!=null){
                    mListener.onNextPassword(editEmail.getText().toString());
                }
                break;
        }
    }

    public interface OnSignupEmailListener {
        void onBackSignin();
        void onNextPassword(String email);
    }

    public void setOnSignupEmailListener(OnSignupEmailListener listener) {
        mListener = listener;
    }
}