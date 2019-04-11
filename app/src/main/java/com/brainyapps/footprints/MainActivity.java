package com.brainyapps.footprints;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.fragments.MainMapFragment;
import com.brainyapps.footprints.fragments.MainNotificationFragment;
import com.brainyapps.footprints.fragments.MainProfileFragment;
import com.brainyapps.footprints.fragments.MainSettingFragment;
import com.brainyapps.footprints.models.Notification;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.brainyapps.footprints.utils.PrefUtils;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FRAGMENT_MAP_TAG = 1;
    private static final int FRAGMENT_NOTIFICATIONS_TAG = 2;
    private static final int FRAGMENT_SETTINGS_TAG = 3;
    private static final int FRAGMENT_PROFILE_TAG = 4;

    private Map<String, Fragment> mFragmentMap;

    private Fragment mFragment;
    private int currentPosition = 0;

    private RelativeLayout btn_logout;

    private SigninActivity signinActivity;
    private GoogleSignInClient mGoogleSignInClient;

    private User currentUser;
    private ImageView back_img;
    private ImageView main_menu_icon;
    private RelativeLayout btn_map;
    private RelativeLayout btn_notification;
    private RelativeLayout btn_settings;
    private TextView num_notification;
    private int fragment_name = FRAGMENT_MAP_TAG;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Query userInfo;
    private Query notify_info;

    final FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
    final String user_id = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        navigation_menu.setVisibility(View.INVISIBLE);
//        back_img = (ImageView) findViewById(R.id.home_user_background);
        userInfo = databaseReference.child(DBInfo.TBL_USER + "/" + user.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User.class);
                    if(currentUser.banned == 1){
                        AlertFactory.showAlert(MainActivity.this, "Error","Your account has been blocked.", "OKAY", "", new AlertFactoryClickListener() {
                            @Override
                            public void onClickYes(AlertDialog dialog) {

                            }
                            @Override
                            public void onClickNo(AlertDialog dialog) {

                            }
                            @Override
                            public void onClickDone(AlertDialog dialog) {
                                logout();
                            }
                        });
                    }else {
                        callFramgment(fragment_name);
                        setResource(currentUser);
//                                if(!currentUser.photoUrl.equals("")){
//                                    Glide.with(getApplicationContext()).load(currentUser.photoUrl).into(back_img);
//                                }
//                                Blurry.with(getApplicationContext())
//                                        .radius(25)
//                                        .sampling(3)
//                                        .async()
//                                        .color(Color.argb(66, 55, 55, 55))
//                                        .capture(findViewById(R.id.home_user_background))
//                                        .into((ImageView) findViewById(R.id.home_user_background));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        notify_info = databaseReference.child(Notification.TBL_NAME).child(user_id);
        notify_info.addValueEventListener(getNotifyInfoListener);

        btn_map = (RelativeLayout) findViewById(R.id.navigation_home_field);
        btn_map.setOnClickListener(this);
        btn_notification = (RelativeLayout) findViewById(R.id.navigation_notifications_field);
        btn_notification.setOnClickListener(this);
        btn_settings = (RelativeLayout) findViewById(R.id.navigation_settings_field);
        btn_settings.setOnClickListener(this);
        btn_logout = (RelativeLayout) findViewById(R.id.navigation_logout_field);
        btn_logout.setOnClickListener(this);

        num_notification = (TextView) findViewById(R.id.navigation_notifications_count);
        num_notification.setVisibility(View.GONE);
        main_menu_icon = (ImageView) findViewById(R.id.home_menu);
        main_menu_icon.setOnClickListener(this);

        final ImageView main_post_icon = (ImageView) findViewById(R.id.home_post);
        main_post_icon.setVisibility(View.VISIBLE);
        main_post_icon.setOnClickListener(this);

        final TextView main_title = (TextView) findViewById(R.id.home_title);
        main_title.setText("Footprints");
    }

    private ValueEventListener getNotifyInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                int i = 0;
                for (DataSnapshot notifyInfo : dataSnapshot.getChildren()) {
                    Notification n = notifyInfo.getValue(Notification.class);
                    if(n.isRead == 0)
                        i++;
                }
                if(i == 0 ){
                    num_notification.setVisibility(View.GONE);
                }else {
                    num_notification.setVisibility(View.VISIBLE);
                    num_notification.setText(String.valueOf(i));
                }
            }else
                num_notification.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void callFramgment(int fragment_type){
        mFragmentMap = new HashMap<>();
        mFragmentMap.put(MainMapFragment.FRAGMENT_TAG, MainMapFragment.newInstance(this,user_id));
        mFragmentMap.put(MainSettingFragment.FRAGMENT_TAG, MainSettingFragment.newInstance(this,user_id));
        mFragmentMap.put(MainNotificationFragment.FRAGMENT_TAG, MainNotificationFragment.newInstance(this,user_id));
        mFragmentMap.put(MainProfileFragment.FRAGMENT_TAG, MainProfileFragment.newInstance(this,user_id));

        showFragment(fragment_type, true);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){

        }
    }

    private void setResource(User user){
        TextView user_name = (TextView) findViewById(R.id.home_user_name);
        user_name.setText(user.getName());
        CircleImageView profile_img = (CircleImageView) findViewById(R.id.home_profile_image);
        Utils.setAvatarImage(user,getApplicationContext(),profile_img);
    }

    public void logout(){
        if (FirebaseAuth.getInstance() != null) {

            if (FirebaseManager.getInstance().getGoogleApiClient() != null)
                FirebaseManager.getInstance().getGoogleApiClient().disconnect();

            FirebaseManager.getInstance().clear();
            FirebaseAuth.getInstance().signOut();

            if(currentUser.signupType.equals("Google")){
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("611724790996-hfp0fldp2npp4k9sc0bisor3pv03u5bo.apps.googleusercontent.com")
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this,"Sign Out successfully", Toast.LENGTH_SHORT).show();
                                // ...
                            }
                        });
            }else if(currentUser.signupType.equals("Facebook")){
                LoginManager.getInstance().logOut();
            }

            Firebase firebase = new Firebase("https://footprints-fef2a.firebaseio.com/");
            firebase.unauth();

            Intent loginintent = new Intent(this, SigninActivity.class);
            startActivity(loginintent);
            finish();
        }
        signOutAction();
    }

    public void signOutAction(){
        Intent signinIntent = new Intent(this, SigninActivity.class);
        finish();
        startActivity(signinIntent);
    }

    public void showFragment(int position, Boolean isPushed) {
        mFragment = null;

        if (currentPosition == position)
            return;

        currentPosition = position;
        final TextView main_title = (TextView) findViewById(R.id.home_title);
        switch (position) {
            case FRAGMENT_MAP_TAG:
                mFragment = mFragmentMap.get(MainMapFragment.FRAGMENT_TAG);
                main_title.setText("Footprints");
                break;
            case FRAGMENT_NOTIFICATIONS_TAG:
                mFragment = mFragmentMap.get(MainNotificationFragment.FRAGMENT_TAG);
                main_title.setText("Notifications");
                break;
            case FRAGMENT_PROFILE_TAG:
                mFragment = mFragmentMap.get(MainProfileFragment.FRAGMENT_TAG);
                main_title.setText(currentUser.getName());
                break;
            case FRAGMENT_SETTINGS_TAG:
                mFragment = mFragmentMap.get(MainSettingFragment.FRAGMENT_TAG);
                main_title.setText("Settings");
                break;
            default:
                break;
        }

        if (mFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.main_fragment_container, mFragment).commit();
        } else {
            Log.e(TAG, "Error in creating fragment");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.home_post:
                postPageView();
                break;
            case R.id.navigation_logout_field:
                userLogout();
                break;
            case R.id.home_menu:
                showNavigationBar();
                break;
            case R.id.navigation_home_field:
                navigation_home();
                break;
            case R.id.navigation_notifications_field:
                navigation_notifications();
                break;
            case R.id.navigation_settings_field:
                navigation_settings();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notify_info.removeEventListener(getNotifyInfoListener);
    }
    @Override
    public void onResume(){
        super.onResume();
        userInfo = databaseReference.child(DBInfo.TBL_USER + "/" + user.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User.class);
                    setResource(currentUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showNavigationBar(){
        hideSoftKeyboard();
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        navigation_menu.setVisibility(View.VISIBLE);

    }

    public void navigation_layout(View view){
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        navigation_menu.setVisibility(View.INVISIBLE);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case Utils.PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.e("","ACCEPTED!!!");
//                } else {
//                    Log.e("","DENIED!!!");
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request.
//        }
//    }

    public void navigation_home(){
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        final ImageView main_post_icon = (ImageView) findViewById(R.id.home_post);
        restoreTitleBar();
        showFragment(FRAGMENT_MAP_TAG, true);
        navigation_menu.setVisibility(View.INVISIBLE);
        main_post_icon.setVisibility(View.VISIBLE);
    }

    public void navigation_notifications(){
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        final ImageView main_post_icon = (ImageView) findViewById(R.id.home_post);
        restoreTitleBar();
        main_post_icon.setVisibility(View.GONE);
        showFragment(FRAGMENT_NOTIFICATIONS_TAG, true);
        navigation_menu.setVisibility(View.INVISIBLE);
    }

    public void navigation_settings(){
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        final ImageView main_post_icon = (ImageView) findViewById(R.id.home_post);
        restoreTitleBar();
        showFragment(FRAGMENT_SETTINGS_TAG, true);
        navigation_menu.setVisibility(View.INVISIBLE);
        main_post_icon.setVisibility(View.GONE);
    }

    public void navigation_profile(View view){
        final FrameLayout navigation_menu = (FrameLayout) findViewById(R.id.navigation_view);
        final ImageView main_post_icon = (ImageView) findViewById(R.id.home_post);
        RelativeLayout fragment_container = (RelativeLayout) findViewById(R.id.main_fragment_container);
        RelativeLayout home_title_bar = (RelativeLayout) findViewById(R.id.home_title_bar);
        showFragment(FRAGMENT_PROFILE_TAG, true);
        home_title_bar.setBackgroundColor(Color.parseColor("#40000000"));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) fragment_container.getLayoutParams();
        lp.addRule(RelativeLayout.BELOW,0);
        navigation_menu.setVisibility(View.INVISIBLE);
        main_post_icon.setVisibility(View.GONE);
    }

    public void restoreTitleBar(){
        RelativeLayout fragment_container = (RelativeLayout) findViewById(R.id.main_fragment_container);
        RelativeLayout home_title_bar = (RelativeLayout) findViewById(R.id.home_title_bar);
        home_title_bar.setBackgroundResource(R.color.colorTopBack);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) fragment_container.getLayoutParams();
        lp.addRule(RelativeLayout.BELOW,R.id.home_title_bar);
    }

    public void navigation_logout(View view){
        Intent signip_intent = new Intent(this, SigninActivity.class);
        startActivity(signip_intent);
    }

    public void postPageView(){
        Intent post_intent = new Intent(this, PostActivity.class);
        startActivity(post_intent);
    }

    public void userLogout(){
        AlertFactory.showAlert(MainActivity.this, "FootPrints","Are you sure want to logout?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(AlertDialog dialog) {
                logout();
            }
            @Override
            public void onClickNo(AlertDialog dialog) {
                dialog.dismiss();
            }
            @Override
            public void onClickDone(AlertDialog dialog) {
                logout();
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertFactory.showAlert(MainActivity.this, "FootPrints","Are you sure want to exit?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(AlertDialog dialog) {
                MainActivity.super.onBackPressed();
            }
            @Override
            public void onClickNo(AlertDialog dialog) {
                dialog.dismiss();
            }
            @Override
            public void onClickDone(AlertDialog dialog) {
                logout();
            }
        });
    }

}
