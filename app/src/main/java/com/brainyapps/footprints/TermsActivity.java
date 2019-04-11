package com.brainyapps.footprints;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
    }

    public void terms_conditions_goto_backpage(View view){
        super.onBackPressed();
    }
}
