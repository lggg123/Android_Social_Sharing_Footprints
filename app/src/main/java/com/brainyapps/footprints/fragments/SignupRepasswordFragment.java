package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.views.ValidationCheck;


public class SignupRepasswordFragment extends android.app.Fragment implements View.OnClickListener{
    public static final String TAG = SignupRepasswordFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG = "com_mobile_signup_repassword_fragment_tag";

    private static Context mContext;

    private EditText editRePassword;

    private ValidationCheck matchIndicator;

    private static String mPassword;

    private RelativeLayout btnNext;

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String password = editable.toString();

            if (!TextUtils.isEmpty(password)) {
                if (TextUtils.equals(mPassword, password)) {
                    matchIndicator.success();
                } else {
                    matchIndicator.fail();
                }
            } else {
                matchIndicator.fail();
            }

            enableNext(password);
        }
    };

    public static android.app.Fragment newInstance(Context context) {
        mContext = context;

        android.app.Fragment f = new SignupRepasswordFragment();

        mPassword = "";

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_signup_repassword, container, false);

        matchIndicator = (ValidationCheck) rootView.findViewById(R.id.match_repassword_check_left_icon);
        editRePassword = (EditText) rootView.findViewById(R.id.signup_edit_repassword);

        btnNext = (RelativeLayout) rootView.findViewById(R.id.signup_repassword_button_next);
        btnNext.setOnClickListener(this);
        btnNext.setEnabled(false);

        editRePassword.setText("");
        editRePassword.addTextChangedListener(mTextWatcher);

        return rootView;
    }

    public void setPassword(String password) {
        Log.e(TAG, "pass " + password);
        mPassword = password;
    }

    private void enableNext(String password) {
        boolean isEnable = TextUtils.equals(mPassword, password);
        btnNext.setEnabled(isEnable);
    }

    public void initialize() {
        if (editRePassword != null)
            editRePassword.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_repassword_button_next:
                if(mListener!=null){
                    mListener.onNextInfo();
                }
                break;
        }
    }

    public OnSignupRePasswordListener mListener;

    public interface OnSignupRePasswordListener {
        void onNextInfo();
    }

    public void setOnSignupRePasswordListener(OnSignupRePasswordListener listener) {
        mListener = listener;
    }

}
