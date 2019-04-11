package com.brainyapps.footprints.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by HappyBear on 6/21/2018.
 */

public class CustomViewPager extends ViewPager {
    float mStartDragX;

    public CustomViewPager(Context context) {
        super(context);

    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        final int action = ev.getAction();
        float x = ev.getX();
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mStartDragX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (x < mStartDragX) {
                    mListener.onSwipeRight();
                } else {
                    mStartDragX = 0;
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    OnSwipeLeftRightListener mListener;

    public void setOnSwipeLeftRightListener(OnSwipeLeftRightListener listener) {
        mListener = listener;
    }

    public interface OnSwipeLeftRightListener {

        public void onSwipeLeft();

        public void onSwipeRight();
    }
}
