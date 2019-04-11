package com.brainyapps.footprints.utils;

import android.content.Context;
import android.text.TextUtils;

import com.brainyapps.footprints.models.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Created by SuperMan on 4/17/2018.
 */

public class FirebaseManager {
    private static FirebaseManager instance;

    public Context mContext;
    public User MyUsage;

    public float mScale;

    private Firebase mFirebaseRef;

    private FirebaseUser mCurrentUser;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private String mAuthToken;

    public GoogleApiClient mGoogleApiClient;

    private AuthData mAuthData;

    public boolean isLoaded;

    private FirebaseManager(Context context) {
        mContext = context;
        MyUsage = new User();

        isLoaded = false;
    }

    public static void init(Context context) {
        instance = new FirebaseManager(context);
    }

    public static FirebaseManager getInstance() {
        return instance;
    }

    public void setFirebaseRef(Firebase firebaseRef) {
        mFirebaseRef = firebaseRef;
    }

    public Firebase getFirebaseRef() {
        return mFirebaseRef;
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setCurrentuser(FirebaseUser user) {
        mCurrentUser = user;
    }

    public void setFirebaseAuth(FirebaseAuth auth) {
        mAuth = auth;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }

    public void setDataBaseRef(DatabaseReference dataBaseRef) {
        mDatabase = dataBaseRef;
    }

    public DatabaseReference getDataBaseRef() {
        return mDatabase;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setAuthData(AuthData authData) {
        mAuthData = authData;
    }

    public AuthData getAuthData() {
        return mAuthData;
    }

    public String getMyUserId() {
        String userId = MyUsage.userId;
        if (TextUtils.isEmpty(userId) && FirebaseAuth.getInstance().getCurrentUser() != null)
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (TextUtils.isEmpty(userId))
            userId = "";

        return userId;
    }

    public void clear() {
        MyUsage = new User();
        mCurrentUser = null;
    }
}
