package com.brainyapps.footprints.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainyapps.footprints.R;
import com.brainyapps.footprints.SignupActivity;
import com.brainyapps.footprints.utils.ImagePicker;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.brainyapps.footprints.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupInfoFragment extends android.app.Fragment implements View.OnClickListener{
    private static int PLACE_PICKER_REQUEST = 50;

    public static final String FRAGMENT_TAG = "com_mobile_signup_done_fragment_tag";

    public static final int REQUEST_CAMERA_CONTENT = 2011;

    public static final int REQUEST_IMAGE_CONTENT = 2012;

    public static final int REQUEST_IMAGE_CROP = 2014;

    private static Context mContext;

    private CircleImageView mImgUser;

    private byte[] byteData = null;

    private RelativeLayout btnNext;
    private String newAddress = "";

    private EditText editFirstName;
    private EditText editLastName;
    private EditText editBrif;
    private RelativeLayout editAddress;
    private StorageReference storePhoto;
    private String photoUrl;

    private Bitmap bitmap;

    private AppCompatCheckBox checkTerms;

    public static android.app.Fragment newInstance(Context context) {
        mContext = context;

        android.app.Fragment f = new SignupInfoFragment();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storePhoto = FirebaseStorage.getInstance().getReference();
        photoUrl = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_signup_info, container, false);

        mImgUser = (CircleImageView) rootView.findViewById(R.id.img_photo_frame);
        mImgUser.setOnClickListener(this);

        btnNext = (RelativeLayout) rootView.findViewById(R.id.signup_button_done);
        btnNext.setOnClickListener(this);

        editFirstName = (EditText) rootView.findViewById(R.id.signup_edit_first_name);
        editLastName = (EditText) rootView.findViewById(R.id.signup_edit_last_name);
        editBrif = (EditText) rootView.findViewById(R.id.signup_edit_brif);
        editAddress = (RelativeLayout)rootView.findViewById(R.id.signup_edit_address);
        editAddress.setOnClickListener(this);


        editFirstName.setText("");
        editLastName.setText("");
        editBrif.setText("");

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_button_done:
                if (!isValidInformation()) {
                    return;
                }
                if (!Utils.checkConnection(getActivity())) {
                    return;
                }
                actionSignup();
                break;
            case R.id.img_photo_frame:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_CONTENT);
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_IMAGE_CONTENT);
                break;
            case R.id.signup_edit_address:
                getAddress();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CONTENT && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            bitmap = ImagePicker.getImageFromResult(getActivity(),resultCode,data);
            mImgUser.setImageBitmap(bitmap);
//            uriProfileImage = data.getData();
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriProfileImage);
//                mImgUser.setImageBitmap(bitmap);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        if (requestCode == PLACE_PICKER_REQUEST &&resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data,getActivity());
            newAddress = place.getAddress().toString();
        }
    }

    private void getAddress(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void actionSignup() {
        String firstName = editFirstName.getText().toString();
        if(firstName!=null){
            firstName = firstName.replaceFirst("^\\s*", "");
            firstName = firstName.replaceAll("\\s++$", "");
            firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
            editFirstName.setText(firstName);
        }
        String lastName = editLastName.getText().toString();
        if(lastName!=null){
            lastName = lastName.replaceFirst("^\\s*", "");
            lastName = lastName.replaceAll("\\s++$", "");
            lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1);
            editLastName.setText(lastName);
        }

        callListener();
    }

    private void callListener(){
        String firstName = editFirstName.getText().toString();
        String lastName = editLastName.getText().toString();

        if (mListener != null) {
            mListener.onSignupDone(firstName, lastName, bitmap, editBrif.getText().toString(),newAddress);
        }
    }

    private boolean isValidInformation() {
        String text = editFirstName.getText().toString();
        text = text.replaceAll("\\s+", "");
        int error = 0;
        if (TextUtils.isEmpty(text) || text.length() < 1) {
            error++;
        }

        text = editLastName.getText().toString();
        text = text.replaceAll("\\s+", "");
        if (TextUtils.isEmpty(text) || text.length() < 1) {
            error++;
        }

        if(error > 0){
            AlertFactory.showAlert(getActivity(), "Error", "You must input First Name and Last Name.", "Ok", "", new AlertFactoryClickListener() {
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
            return false;
        }
        if(newAddress.equals("")){
            AlertFactory.showAlert(getActivity(), "Error", "You must input Your address.", "Ok", "", new AlertFactoryClickListener() {
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
            return false;
        }
        return true;
    }

    public void initialize() {
        if (editFirstName != null)
            editFirstName.setText("");
        if (editLastName != null)
            editLastName.setText("");
        if (editBrif != null)
            editBrif.setText("");
        if (btnNext != null)
            btnNext.setEnabled(false);

        mImgUser.setImageResource(R.drawable.photo_frame);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public OnSignupInfoListener mListener;

    public interface OnSignupInfoListener {
        void onSignupDone(String firstName, String lastName, Bitmap photo, String brif, String address);
//        void onSignupPhoto(String firstName, String lastName, GooglePlace googlePlace, byte[] bytes);
    }

    public void setOnSignupInfoListener(OnSignupInfoListener listener) {
        mListener = listener;
    }

}
