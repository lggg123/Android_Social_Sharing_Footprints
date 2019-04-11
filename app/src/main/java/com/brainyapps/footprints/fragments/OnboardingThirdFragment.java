package com.brainyapps.footprints.fragments;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.R;

public class OnboardingThirdFragment extends Fragment{


    public static final String FRAGMENT_TAG = "com_footprints_onboarding_last_fragment_tag";

    private static Context mContext;

    private RelativeLayout btnDone;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static android.app.Fragment newInstance(Context context) {
        mContext = context;

        android.app.Fragment f = new OnboardingThirdFragment();
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_onboarding_third, container, false);

        return rootView;
    }
}