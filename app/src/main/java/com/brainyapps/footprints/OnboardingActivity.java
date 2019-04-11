package com.brainyapps.footprints;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.brainyapps.footprints.fragments.OnboardingFirstFragment;
import com.brainyapps.footprints.fragments.OnboardingSecondFragment;
import com.brainyapps.footprints.fragments.OnboardingThirdFragment;
import com.brainyapps.footprints.views.CustomViewPager;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity implements View.OnClickListener,CustomViewPager.OnSwipeLeftRightListener{

    public static final String TAG = OnboardingActivity.class.getSimpleName();

    private static final int NUM_PAGES = 3;
    private static final int FRAGMENT_ONBOARDING_FRIST_TAG = 0;
    private static final int FRAGMENT_ONBOARDING_SECOND_TAG = 1;
    private static final int FRAGMENT_ONBOARDING_LAST_TAG = 2;

    private Map<String, Fragment> mFragmentMap;

    private Fragment mFragment;

    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private RelativeLayout btn_next;
    private RelativeLayout btn_done;
    private RelativeLayout btn_skip;

    private ImageView first_dot;
    private ImageView second_dot;
    private ImageView third_dot;

    int selectedIndex = 0;
    private boolean isFromMain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        if (getIntent() != null) {
            isFromMain = getIntent().getBooleanExtra("tutorial", false);
        }

        btn_next = (RelativeLayout)findViewById(R.id.onboarding_btn_next);
        btn_next.setOnClickListener(this);
        btn_done = (RelativeLayout)findViewById(R.id.onboarding_btn_done);
        btn_done.setOnClickListener(this);
        btn_skip = (RelativeLayout)findViewById(R.id.onboarding_btn_skip);
        btn_skip.setOnClickListener(this);

        first_dot = (ImageView)findViewById(R.id.onboarding_first_dot);
        second_dot = (ImageView)findViewById(R.id.onboarding_second_dot);
        third_dot = (ImageView)findViewById(R.id.onboarding_third_dot);

        mFragmentMap = new HashMap<>();
        mFragmentMap.put("0", new OnboardingFirstFragment().newInstance(this));
        mFragmentMap.put("1", new OnboardingSecondFragment().newInstance(this));
        mFragmentMap.put("2", new OnboardingThirdFragment().newInstance(this));

        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;

                if (position == FRAGMENT_ONBOARDING_FRIST_TAG) {
                    btn_next.setVisibility(View.VISIBLE);
                    btn_skip.setVisibility(View.VISIBLE);
                    btn_done.setVisibility(View.GONE);
                    first_dot.setImageResource(R.drawable.selected_sel);
                    second_dot.setImageResource(R.drawable.unselected_sel);
                    third_dot.setImageResource(R.drawable.unselected_sel);
                }
                if (position == FRAGMENT_ONBOARDING_SECOND_TAG) {
                    btn_next.setVisibility(View.VISIBLE);
                    btn_skip.setVisibility(View.VISIBLE);
                    btn_done.setVisibility(View.GONE);
                    first_dot.setImageResource(R.drawable.unselected_sel);
                    second_dot.setImageResource(R.drawable.selected_sel);
                    third_dot.setImageResource(R.drawable.unselected_sel);
                }
                if (position == FRAGMENT_ONBOARDING_LAST_TAG) {
                    btn_next.setVisibility(View.GONE);
                    btn_skip.setVisibility(View.GONE);
                    btn_done.setVisibility(View.VISIBLE);
                    first_dot.setImageResource(R.drawable.unselected_sel);
                    second_dot.setImageResource(R.drawable.unselected_sel);
                    third_dot.setImageResource(R.drawable.selected_sel);
                }
            }
        });
        mPager.setOnSwipeLeftRightListener(this);
    }

    private void gotoMain() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.onboarding_btn_next:
                gotoNextPage();
                break;
            case R.id.onboarding_btn_skip:
                gotoMain();
                break;
            case R.id.onboarding_btn_done:
                gotoMain();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSwipeLeft() {

    }

    @Override
    public void onSwipeRight() {
        if (selectedIndex == NUM_PAGES - 1) {
            gotoMain();
        }
    }

    public void gotoNextPage(){
        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mFragmentMap.get(String.valueOf(position));
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
