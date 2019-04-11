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
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.ValidationCheck;


public class SignupPasswordFragment extends android.app.Fragment implements View.OnClickListener{


    public static final String FRAGMENT_TAG = "com_mobile_signup_password_fragment_tag";

    private static Context mContext;

    private EditText editPassword;

    private ValidationCheck lengthIndicator;
    private ValidationCheck numberIndicator;
    private ValidationCheck letterIndicator;

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
                if (Utils.overLength(password)) {
                    lengthIndicator.success();
                } else {
                    lengthIndicator.fail();
                }

                if (Utils.containsNumber(password)) {
                    numberIndicator.success();
                } else {
                    numberIndicator.fail();
                }

                if (Utils.containsCharacter(password)) {
                    letterIndicator.success();
                } else {
                    letterIndicator.fail();
                }

            } else {
                lengthIndicator.fail();
                numberIndicator.fail();
                letterIndicator.fail();
            }
            enableNext(password);
        }
    };

    public static android.app.Fragment newInstance(Context context) {
        mContext = context;

        android.app.Fragment f = new SignupPasswordFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_signup_password, container, false);

        editPassword = (EditText) rootView.findViewById(R.id.signup_edit_password);

        lengthIndicator = (ValidationCheck) rootView.findViewById(R.id.length_password_check_left_icon);
        numberIndicator = (ValidationCheck) rootView.findViewById(R.id.contain_number_password_check_left_icon);
        letterIndicator = (ValidationCheck) rootView.findViewById(R.id.contain_letter_password_check_left_icon);

        btnNext = (RelativeLayout) rootView.findViewById(R.id.signup_password_button_next);
        btnNext.setOnClickListener(this);
        btnNext.setEnabled(false);

        editPassword.setText("");
        editPassword.addTextChangedListener(mTextWatcher);

        return rootView;
    }

    private void enableNext(String password) {
        boolean isEnable = Utils.overLength(password) && Utils.containsCharacter(password) && Utils.containsNumber(password);
        btnNext.setEnabled(isEnable);
    }

    public void initialize() {
        if (editPassword != null)
            editPassword.setText("");

        if (btnNext != null)
            btnNext.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public OnSignupPasswordListener mListener;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_password_button_next:
                if(mListener!=null){
                    mListener.onNextRepassword(editPassword.getText().toString());
                }
                break;
        }
    }

    public interface OnSignupPasswordListener {
        void onNextRepassword(String password);
    }

    public void setOnSignupPasswordListener(OnSignupPasswordListener listener) {
        mListener = listener;
    }

}
