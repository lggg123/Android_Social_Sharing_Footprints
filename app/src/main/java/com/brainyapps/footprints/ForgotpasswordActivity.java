package com.brainyapps.footprints;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotpasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText forgot_email;
    private ProgressBar forgot_progressBar;
    private TextView btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        forgot_email = (EditText) findViewById(R.id.forgot_password_email);
        forgot_progressBar = (ProgressBar) findViewById(R.id.forgot_progressBar);
        btn_send = (TextView) findViewById(R.id.forgot_password_btn_save);
        auth = auth.getInstance();
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = forgot_email.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                forgot_progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotpasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ForgotpasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }
                                forgot_progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

    public void send_email(){
        String email = forgot_email.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
            return;
        }

        forgot_progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotpasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotpasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                        }
                        forgot_progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void forgot_password_goto_backpage(View view){
        super.onBackPressed();
    }
}
