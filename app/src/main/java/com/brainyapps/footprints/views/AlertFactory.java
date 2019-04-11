package com.brainyapps.footprints.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brainyapps.footprints.R;

/**
 * Created by SuperMan on 4/14/2018.
 */

public class AlertFactory {

    public static boolean isShowed = false;
    public AlertFactory() {
    }

    public static void showAlert(Context context, String title, String message) {
        if (isShowed)
            return;

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLACK);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        isShowed = false;
                    }
                });

        dialog.show();

        isShowed = true;
    }

    public static void showAlert(@NonNull Activity context, String title, String message, @NonNull String doneString, String cancelString,
                                 final AlertFactoryClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_alert, null);

        final TextView tvTitle = (TextView) convertView.findViewById(R.id.dialog_alert_title);
        final TextView tvMessage = (TextView) convertView.findViewById(R.id.dialog_alert_message);

        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.INVISIBLE);
        }

        tvTitle.setText(title);
        tvMessage.setText(message);

        final Button btnYes = (Button) convertView.findViewById(R.id.dialog_alert_yes);
        final Button btnNo = (Button) convertView.findViewById(R.id.dialog_alert_no);
        final Button btnDone = (Button) convertView.findViewById(R.id.dialog_alert_done);

        View vYesNo = (View) convertView.findViewById(R.id.dialog_alert_yesno);
        View vOk = (View) convertView.findViewById(R.id.dialog_alert_ok);
        if (TextUtils.isEmpty(cancelString)) {
            vYesNo.setVisibility(View.GONE);
            vOk.setVisibility(View.VISIBLE);
            btnDone.setText(doneString);
        } else {
            vYesNo.setVisibility(View.VISIBLE);
            vOk.setVisibility(View.GONE);
            btnYes.setText(doneString);
            btnNo.setText(cancelString);
        }

        builder.setView(convertView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickYes(dialog);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickNo(dialog);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickDone(dialog);
            }
        });
    }

    public static void showAlertDone(@NonNull Activity context, String title, String message, @NonNull String doneString, String cancelString,
                                     final AlertFactoryClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_alert, null);

        final TextView tvTitle = (TextView) convertView.findViewById(R.id.dialog_alert_title);
        final TextView tvMessage = (TextView) convertView.findViewById(R.id.dialog_alert_message);

        tvTitle.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.INVISIBLE);
        }

        tvTitle.setText(title);
        tvMessage.setText(message);

        final Button btnYes = (Button) convertView.findViewById(R.id.dialog_alert_yes);
        final Button btnNo = (Button) convertView.findViewById(R.id.dialog_alert_no);
        final Button btnDone = (Button) convertView.findViewById(R.id.dialog_alert_done);

        btnYes.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        btnNo.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        btnDone.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));

        View vYesNo = (View) convertView.findViewById(R.id.dialog_alert_yesno);
        View vOk = (View) convertView.findViewById(R.id.dialog_alert_ok);
        if (TextUtils.isEmpty(cancelString)) {
            vYesNo.setVisibility(View.GONE);
            vOk.setVisibility(View.VISIBLE);
            btnDone.setText(doneString);
        } else {
            vYesNo.setVisibility(View.VISIBLE);
            vOk.setVisibility(View.GONE);
            btnYes.setText(doneString);
            btnNo.setText(cancelString);
        }

        builder.setView(convertView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickYes(dialog);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickNo(dialog);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickDone(dialog);
            }
        });
    }
}