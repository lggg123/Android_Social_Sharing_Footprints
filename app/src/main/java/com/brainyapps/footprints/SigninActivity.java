package com.brainyapps.footprints;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainyapps.footprints.admins.AdminMainActivity;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.FirebaseManager;
import com.brainyapps.footprints.utils.PrefUtils;
import com.brainyapps.footprints.utils.Utils;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.walnutlabs.android.ProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigninActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private enum SignInStatus {
        Connecting,
        Ready,
        Success
    }
    public static final int RC_GOOGLE_LOGIN = 1001;
    public final String TAG = "Log:";

    private EditText inputEmail, inputPassword;
    private ImageView facebook_login;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /* Listener for Firebase session changes */
    private Firebase.AuthStateListener mAuthStateListener;
    private CallbackManager mFacebookCallbackManager;
    private LoginButton mFacebookButton;

    /* *************************************
     *              GOOGLE                 *
     ***************************************/
    /* Request code used to invoke sign in user interactions for Google+ */
    private boolean mGoogleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;

    private SignInStatus signInStatus;
    GoogleApiClient googleApiClient;

    private ProgressHUD mProgressDialog;

    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(SigninActivity.this, text, true);
        mProgressDialog.show();
    }

    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        inputEmail = (EditText) findViewById(R.id.signin_edit_email);
        inputPassword = (EditText) findViewById(R.id.signin_edit_password);

        TextView btn_signup = (TextView) findViewById(R.id.signin_btn_signup);
        btn_signup.setOnClickListener(this);
        RelativeLayout btn_login = (RelativeLayout) findViewById(R.id.signin_btn_login);
        btn_login.setOnClickListener(this);
        facebook_login = (ImageView) findViewById(R.id.signin_btn_facebook);
        facebook_login.setOnClickListener(this);
        ImageView google_login = (ImageView) findViewById(R.id.signin_btn_google);
        google_login.setOnClickListener(this);

        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookButton = (LoginButton) findViewById(R.id.signin_fb_button);
        mFacebookButton.setReadPermissions("email", "public_profile");
        mFacebookButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginRes) {
                Log.e(TAG, "Facebook is allow to login");
                handleFacebookAccessToken(loginRes.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
                LoginManager.getInstance().logOut();
                signInStatus = SignInStatus.Ready;
                facebook_login.setEnabled(true);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
                LoginManager.getInstance().logOut();
                signInStatus = SignInStatus.Ready;
                facebook_login.setEnabled(true);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("611724790996-hfp0fldp2npp4k9sc0bisor3pv03u5bo.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInStatus = SignInStatus.Success;

        mAuth = FirebaseAuth.getInstance();
//        if (mAuth.getCurrentUser() != null) {
//            startActivity(new Intent(SigninActivity.this, MainActivity.class));
//            finish();
//        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                hideProgressHUD();

                if (!Utils.isNetworkAvailable(SigninActivity.this)) {
                    return;
                }

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    FirebaseManager.getInstance().setFirebaseAuth(firebaseAuth);
                    firebaseAuth.getCurrentUser().getToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                        @Override
                        public void onSuccess(GetTokenResult getTokenResult) {
                            FirebaseManager.getInstance().setAuthToken(getTokenResult.getToken());
                        }
                    });

                    if (!user.isEmailVerified()) {
                    }

                    FirebaseManager.getInstance().setCurrentuser(user);
                    FirebaseManager.getInstance().setDataBaseRef(FirebaseDatabase.getInstance().getReference());

                    if (signInStatus == SignInStatus.Success) {
                        signInStatus = SignInStatus.Ready;

                        showProgressHUD("");

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        Query query = databaseReference.child(DBInfo.TBL_USER + "/" + user.getUid());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                hideProgressHUD();

                                if (!dataSnapshot.exists()) {
                                    FirebaseAuth.getInstance().signOut();

                                    PrefUtils.getInstance().putInt(PrefUtils.PREF_USER_LOGGED_IN, PrefUtils.STATUS_LOGGED_OUT);
                                    return;
                                }

                                FirebaseManager.getInstance().MyUsage = dataSnapshot.getValue(User.class);
                                if (FirebaseManager.getInstance().MyUsage != null) {
                                    Intent mainIntent;
                                    mainIntent = new Intent(SigninActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                } else {
                                    signout();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                hideProgressHUD();
                                signout();
                            }
                        });
                    }
                } else {
                    hideProgressHUD();
                    signInStatus = SignInStatus.Ready;

                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                    mFBButton.setEnabled(true);
                }
                // ...
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFetchProvider(final String email) {
        showProgressHUD("");
        mAuth.fetchProvidersForEmail(email).addOnCompleteListener(this, new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    signInStatus = SignInStatus.Ready;
                    facebook_login.setEnabled(true);
                    PrefUtils.getInstance().putBoolean(PrefUtils.PREF_FACEBOOK_ON, false);
                    PrefUtils.getInstance().putBoolean(PrefUtils.PREF_GOOGLE_ON, false);
                    PrefUtils.getInstance().putInt(PrefUtils.PREF_USER_LOGGED_IN, PrefUtils.STATUS_LOGGED_OUT);

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    }
                    return;
                }

                ProviderQueryResult result = task.getResult();
                if (result == null)
                    return;

                List<String> providers = result.getProviders();
                if (providers == null)
                    return;

                String provider = "";
                int index = 0;
                while (index < providers.size()) {
                    if (providers.get(index).contains("google")) {
                        provider = provider + "\nGoogle account";
                    } else if (providers.get(index).contains("facebook")) {
                        provider = provider + "\nFacebook account";
                    } else {
                        provider = provider + "\nEmail account";
                    }

                    index++;
                }
                hideProgressHUD();
                AlertFactory.showAlert(SigninActivity.this, "Warning", "You have already registered account(s):" + provider);
            }
        });
    }

  /* ************************************
 *             FACEBOOK               *
 **************************************
 */
    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        showProgressHUD("");
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        signInStatus = SignInStatus.Ready;
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            facebook_login.setEnabled(true);
                            PrefUtils.getInstance().putBoolean(PrefUtils.PREF_FACEBOOK_ON, false);

                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {

                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        if (object.has("email")) {
                                            String email = null;
                                            try {
                                                email = object.getString("email");
                                                handleFetchProvider(email);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        LoginManager.getInstance().logOut();
                                    }
                                });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "email"); // Parámetros que pedimos a facebook
                                request.setParameters(parameters);
                                request.executeAsync();
                                return;
                            } else {
                            }
                            hideProgressHUD();
                            LoginManager.getInstance().logOut();
                            return;
                        }
                        String name = task.getResult().getUser().getDisplayName();
                        String[] names;
                        String firstName = "";
                        String lastName = "";
                        if (!TextUtils.isEmpty(name)) {
                            names = name.split(" ");
                            if (names.length > 0) {
                                firstName = names[0];
                                lastName = name.replace(firstName+" ", "");
                            }
                        }

                        User user = new User();
                        user.userId = task.getResult().getUser().getUid();
                        user.firstName = firstName;
                        user.lastName = lastName;
                        user.userEmail = task.getResult().getUser().getEmail();
                        user.photoUrl = String.valueOf(task.getResult().getUser().getPhotoUrl());
                        user.facebookId = token.getUserId();
                        user.signupType = "Facebook";
                        user.banned = 0;
                        socialSignin(user);
                        hideProgressHUD();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signin_btn_login:
                loginwithEmail();
                break;
            case R.id.signin_btn_google:
                loginwithGoogle();
                break;
            case R.id.signin_btn_facebook:
                loginwithFacebook();
                break;
            case R.id.signin_btn_signup:
                signupwithEmail();
            default:
                break;
        }
    }

    public void loginwithEmail(){
        final String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (!Utils.checkConnection(this)) {
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            inputEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            inputPassword.requestFocus();
            return;
        }

        showProgressHUD("");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(DBInfo.TBL_EMAIL);
        query.orderByChild(DBInfo.EMAIL).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    AlertFactory.showAlert(SigninActivity.this, "Error", "Email entered is not registered. Create an account now?", "TRY AGAIN", "SIGN UP", new AlertFactoryClickListener() {
                        @Override
                        public void onClickYes(AlertDialog dialog) {
                           hideProgressHUD();
                            dialog.dismiss();
                        }
                        @Override
                        public void onClickNo(AlertDialog dialog) {
                            signupwithEmail();
                        }
                        @Override
                        public void onClickDone(AlertDialog dialog) {
                            dialog.dismiss();
                        }
                    });
                    return;
                }

                final boolean isAdmin = dataSnapshot.getValue().toString().contains("role=admin");
                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                hideProgressHUD();
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    AlertFactory.showAlert(SigninActivity.this, "Error", "Password entered is incorrect.", "OKAY", "", new AlertFactoryClickListener() {
                                        @Override
                                        public void onClickYes(AlertDialog dialog) {

                                        }
                                        @Override
                                        public void onClickNo(AlertDialog dialog) {

                                        }
                                        @Override
                                        public void onClickDone(AlertDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    });
                                } else {
                                    if(isAdmin)
                                        gotoAdmin();
                                    else
                                        gotoMain();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void gotoAdmin(){
        Intent adminIntent = new Intent(this, AdminMainActivity.class);
        startActivity(adminIntent);
        finish();
        hideProgressHUD();
    }

    public void reset_password(View view){
        Intent reset_password_intent = new Intent(this, ForgotpasswordActivity.class);
        startActivity(reset_password_intent);
    }

    public void signupwithEmail(){
        Intent signup_intent = new Intent(this, SignupActivity.class);
        startActivity(signup_intent);
    }

    public void signout(){
        mAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("611724790996-hfp0fldp2npp4k9sc0bisor3pv03u5bo.apps.googleusercontent.com")
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SigninActivity.this,"Sign Out successfully", Toast.LENGTH_SHORT).show();
                        // ...
                    }
                });
    }

    public void loginwithFacebook(){
        if (!Utils.checkConnection(this)) {
            return;
        }
        LoginManager.getInstance().logOut();
        mFacebookButton.performClick();
    }

    public void loginwithGoogle(){
        if (!Utils.checkConnection(this)) {
            return;
        }
        mGoogleLoginClicked = true;
        showProgressHUD("");
        Intent signinGoogle = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signinGoogle,RC_GOOGLE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_GOOGLE_LOGIN){
            if (resultCode != RESULT_OK) {
                hideProgressHUD();
                mGoogleLoginClicked = false;
                signInStatus = SignInStatus.Ready;
                Toast.makeText(SigninActivity.this, "Google login failed", Toast.LENGTH_SHORT).show();
                return;
            }

            signInStatus = SignInStatus.Connecting;

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                hideProgressHUD();
                Toast.makeText(SigninActivity.this,"Signin Failed",Toast.LENGTH_LONG).show();
                // Google Sign In failed, update UI appropriately
                // ...
            }
            mGoogleIntentInProgress = false;
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account){
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            String name = task.getResult().getUser().getDisplayName();
                            String[] names;
                            String firstName = "";
                            String lastName = "";
                            if (!TextUtils.isEmpty(name)) {
                                names = name.split(" ");
                                if (names.length > 0) {
                                    firstName = names[0];
                                    lastName = name.replace(firstName+" ", "");
                                }
                            }

                            // ...
                            User user = new User();
                            user.userId = task.getResult().getUser().getUid();
                            user.userEmail = task.getResult().getUser().getEmail();
                            user.photoUrl = String.valueOf(account.getPhotoUrl());
                            user.firstName = firstName;
                            user.lastName = lastName;
                            user.signupType = "Google";
                            user.banned = 0;
                            socialSignin(user);
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SigninActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                if (account != null)
                                    handleFetchProvider(account.getEmail());
                            }
                            return;
                        }
                        // ...
                    }
                });
    }
    private void socialSignin(final User user){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(DBInfo.TBL_USER + "/" + user.userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    socialSignUp(user);
                    return;
                }

                FirebaseManager.getInstance().MyUsage = dataSnapshot.getValue(User.class);
                if (FirebaseManager.getInstance().MyUsage!= null) {
                    gotoMain();
                } else {
                    signout();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressHUD();
                signout();
            }
        });
    }

    private void socialSignUp(final User user){
        FirebaseManager.getInstance().MyUsage = user;

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_EMAIL + "/" + user.userId + "/" + DBInfo.EMAIL, user.userEmail);
        userUpdates.put("/" + DBInfo.TBL_EMAIL + "/" + user.userId + "/" + DBInfo.ROLE, "user");
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + user.userId, FirebaseManager.getInstance().MyUsage.toMap());

        FirebaseDatabase.getInstance().getReference().updateChildren(userUpdates);
        gotoOnboarding();
    }

    private void gotoMain(){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
        hideProgressHUD();
    }
    private void gotoOnboarding(){
        Intent onboardingIntent = new Intent(this, OnboardingActivity.class);
        startActivity(onboardingIntent);
        finish();
        hideProgressHUD();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
