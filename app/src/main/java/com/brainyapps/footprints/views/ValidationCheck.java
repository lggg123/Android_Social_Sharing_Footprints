package com.brainyapps.footprints.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.brainyapps.footprints.R;

/**
 * Created by SuperMan on 4/12/2018.
 */

public class ValidationCheck extends FrameLayout {

    private Context mContext;
    private ImageView imageView;
    private boolean started;

    public ValidationCheck(@NonNull Context context) {
        super(context);
        mContext = context;
        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ico_before_validation_check);

        addView(imageView);
        imageView.setVisibility(View.VISIBLE);

        started = false;
    }

    public ValidationCheck(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ico_before_validation_check);

        addView(imageView);
        imageView.setVisibility(View.VISIBLE);

        started = false;
    }

    public ValidationCheck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ico_before_validation_check);

        addView(imageView);
        imageView.setVisibility(View.VISIBLE);

        started = false;
    }

    public void fail() {
        imageView.setImageResource(R.drawable.ico_before_validation_check);
        imageView.setVisibility(View.VISIBLE);

        started = false;
    }

    public void success() {
        imageView.setImageResource(R.drawable.ico_after_validation_check);
        imageView.setVisibility(View.VISIBLE);

        started = false;
    }
}
