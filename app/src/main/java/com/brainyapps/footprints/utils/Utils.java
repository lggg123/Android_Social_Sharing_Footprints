package com.brainyapps.footprints.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.brainyapps.footprints.R;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.views.AlertFactory;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by SuperMan on 4/12/2018.
 */

public class Utils {
    public static Date ServerTime;
    public static double ServerOffset = 0.0;

    public static final int PERMISSIONS_REQUEST_LOCATION = 9007;

    public static boolean isValidEmail(String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                    .matches();
        }
    }

    public static boolean containsCharacter(String string) {
        return string.matches(".*[a-zA-Z].*");
    }

    public static boolean containsNumber(String string) {
        return string.matches(".*\\d.*");
    }

    public static boolean overLength(String string) {
        if(string.length() >= 6){
            return true;
        }else {
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean checkConnection(Context context) {
        boolean networkStatus = isNetworkAvailable(context);
        if (!networkStatus) {
            AlertFactory.showAlert(context, "Warning",
                    "Oops! Please connect to the internet and try again.");
        }
        return networkStatus;
    }

    public static void loadName(final Context context, final TextView name, final String userId, final String displayName) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child(DBInfo.TBL_USER + "/" + userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    name.setText(user.getName());
                } else {
                    name.setText(displayName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                name.setText(displayName);
            }
        });
    }

    public static void setAvatarImage(User user, Context context, CircleImageView image){
        if(user.photoUrl.equals("")){
            char first_letter = user.firstName.charAt(0);
            char last_letter = user.lastName.charAt(0);
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .fontSize(30)
                    .bold()
                    .width(100)  // width in px
                    .height(100) // height in px
                    .endConfig()
                    .buildRect(new StringBuilder().append(first_letter).append(last_letter).toString(), Color.rgb(10,127,181));
            image.setImageDrawable(drawable);
        }else {
            Glide.with(context).load(user.photoUrl).into(image);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkLocationPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }

    public static String converteTimestamp(long mileSegundos) {
        double estimatedServerTimeMs = System.currentTimeMillis() + Utils.ServerOffset;
        ServerTime = new Date((long)estimatedServerTimeMs);
        long period = ServerTime.getTime() - mileSegundos;
        long value = TimeUnit.MINUTES.convert(period, TimeUnit.MILLISECONDS);
        if (value == 0) {
            return "Just Now";
        } else if (value < 60) {
            return String.valueOf(value) + " mins ago";
        } else if (value == 60) {
            return "1 hour ago";
        } else if (value < 120) {
            return "1 hour " + String.valueOf(value - 60) + " mins ago";
        } else if (value < 720) {
            return "" + String.valueOf(value / 60) + " hours ago";
        } else if (value < 1440) {
            return "Today " + new SimpleDateFormat("HH:mm").format(new Date(mileSegundos));
        } else if (value < 2880) {
            return "Yesterday " + new SimpleDateFormat("HH:mm").format(new Date(mileSegundos));
        }
        return new SimpleDateFormat("MM/dd, yyyy").format(new Date(mileSegundos));
    }

    public static String converteTimestamp(String mileSegundos) {
        double estimatedServerTimeMs = System.currentTimeMillis() + Utils.ServerOffset;
        ServerTime = new Date((long)estimatedServerTimeMs);
        long time = Long.parseLong(mileSegundos);

        long period = ServerTime.getTime() - time;
        long value = TimeUnit.MINUTES.convert(period, TimeUnit.MILLISECONDS);
        if (value == 0) {
            return "Just Now";
        } else if (value < 60) {
            return String.valueOf(value) + " mins ago";
        } else if (value == 60) {
            return "1 hour ago";
        } else if (value < 120) {
            return "1 hour " + (value - 60) + " mins ago";
        } else if (value < 720) {
            return "" + String.valueOf(value / 60) + " hours ago";
        } else if (value < 1440) {
            return "Today " + new SimpleDateFormat("HH:mm").format(new Date(time));
        } else if (value < 2880) {
            return "Yesterday " + new SimpleDateFormat("HH:mm").format(new Date(time));
        }
        return new SimpleDateFormat("MM/dd, yyyy").format(new Date(time));
    }
}
