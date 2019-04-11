package com.brainyapps.footprints;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.constants.IntentExtra;
import com.brainyapps.footprints.models.GooglePosition;
import com.brainyapps.footprints.models.Report;
import com.brainyapps.footprints.utils.ImagePicker;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.walnutlabs.android.ProgressHUD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class EditpostActivity extends AppCompatActivity implements View.OnClickListener,EasyVideoCallback, LocationListener{

    private final int REQUEST_IMAGE_CAPTURE = 200;
    private final int REQUEST_IMAGE_CAPTURE_FROM_TEMP = 206;
    private final int REQUEST_VIDEO_CAPTURE = 201;
    private final int REQUEST_GALLERY_CAPTURE = 202;
    private final int PLACE_PICKER_REQUEST = 203;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 204;
    private static final int PERMISSION_MOVIE_REQUEST_CODE = 205;

    private String postId;
    private EditText title;
    private EditText description;

    public LinearLayout get_location;
    public LinearLayout post_photo_from_camera;
    public LinearLayout post_from_gallery;
    public LinearLayout post_video_from_camera;

    private RelativeLayout media_container;
    private ImageView posted_image;
    private EasyVideoPlayer posted_video;
    private ImageView play_icon;
    private TextView delete_post;
    private RelativeLayout btn_change;

    private DatabaseReference mDatabase;
    private StorageReference storeMedia;
    private Bitmap bitmap;

    private Uri videoUri;

    private String getImageUrl = "";
    private String mediaUrl;
    private GooglePosition googlePosition;

    private GoogleApiClient mGoogleApiClient;
    Location myLocation;

    Boolean isLocationChanged = false;
    Boolean isMediaChanged = false;
    String mediaType = "Image";
    private boolean isOpenAutoPlace = false;

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(this, text, true);
        mProgressDialog.show();
    }
    private void hideProgressHUD() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private final String myUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editpost);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        title = (EditText) findViewById(R.id.editpost_title_edit);
        description = (EditText) findViewById(R.id.editpost_description_edit);
        delete_post = (TextView) findViewById(R.id.editpost_btn_delete);
        media_container = (RelativeLayout)findViewById(R.id.edit_post_media_container);

        get_location = (LinearLayout) findViewById(R.id.edit_post_get_your_location);
        get_location.setOnClickListener(this);
        post_photo_from_camera = (LinearLayout) findViewById(R.id.edit_post_photo);
        post_photo_from_camera.setOnClickListener(this);
        post_from_gallery = (LinearLayout) findViewById(R.id.edit_post_gallery);
        post_from_gallery.setOnClickListener(this);
        post_video_from_camera = (LinearLayout) findViewById(R.id.edit_post_movie);
        post_video_from_camera.setOnClickListener(this);

        btn_change = (RelativeLayout) findViewById(R.id.save_post_change_btn);
        posted_image = (ImageView) findViewById(R.id.edit_post_image_viewer);
        posted_video = (EasyVideoPlayer) findViewById(R.id.edit_post_video_viewer);
        play_icon = (ImageView) findViewById(R.id.edit_post_play_icon);
        play_icon.setOnClickListener(this);
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference();
                postId = bundle.getString(IntentExtra.POST_ID);
                btn_change.setOnClickListener(this);
                delete_post.setOnClickListener(this);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query query = ref.child(DBInfo.TBL_POST).child(postId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Map<String, Object> post_info = (Map<String,Object>)dataSnapshot.getValue();
                            title.setText(post_info.get("postTitle").toString());
                            description.setText(post_info.get("postDescription").toString());
                            if(post_info.get("mediaType").toString().equals("Image")){
                                showImage(post_info.get("mediaUrl").toString());
                            }else if(post_info.get("mediaType").toString().equals("Video")){
                                showVideo(post_info.get("mediaUrl").toString());
                                posted_video.hideControls();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        storeMedia = FirebaseStorage.getInstance().getReference();

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public void showImage(String url){
        posted_image.setVisibility(View.VISIBLE);
        posted_video.setVisibility(View.GONE);
        play_icon.setVisibility(View.GONE);
        Glide.with(getApplicationContext()).load(url).into(posted_image);
    }

    public void showVideo(String url){
        posted_image.setVisibility(View.GONE);
        posted_video.setVisibility(View.VISIBLE);
        play_icon.setVisibility(View.VISIBLE);
        posted_video.setCallback(this);
        posted_video.setSource(Uri.parse(url));
    }
    public void editpost_goto_backpage(View view){
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null ) {
            media_container.setBackgroundColor(Color.BLACK);
            posted_image.setVisibility(View.VISIBLE);

            bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
            posted_image.setImageBitmap(bitmap);
            play_icon.setVisibility(View.GONE);
            posted_video.setVisibility(View.GONE);
            isMediaChanged = true;
            mediaType = "Image";
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_TEMP) {
            media_container.setBackgroundColor(Color.BLACK);
            posted_image.setVisibility(View.VISIBLE);

            bitmap = ImagePicker.getBitmapFromPath(this, mCurrentPhotoPath);

            posted_image.setImageBitmap(bitmap);
            play_icon.setVisibility(View.GONE);
            posted_video.setVisibility(View.GONE);
            isMediaChanged = true;
            mediaType = "Image";
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            media_container.setBackgroundColor(Color.BLACK);
            posted_image.setVisibility(View.GONE);
            posted_video.setVisibility(View.VISIBLE);
            play_icon.setVisibility(View.VISIBLE);
            if(data.getData()!=null){
                videoUri = data.getData();
                posted_video.setSource(videoUri);
                isMediaChanged = true;
                mediaType = "Video";
            }
        }
        if (requestCode == REQUEST_GALLERY_CAPTURE && resultCode == RESULT_OK) {
            String path = data.getData().getPath();
            if (path.contains("/video/")) {
                media_container.setBackgroundColor(Color.BLACK);
                posted_image.setVisibility(View.GONE);
                posted_video.setVisibility(View.VISIBLE);
                play_icon.setVisibility(View.VISIBLE);
                videoUri = data.getData();
                posted_video.setSource(videoUri);
                isMediaChanged = true;
                mediaType = "Video";
            } else if (path.contains("/images/")) {
                media_container.setBackgroundColor(Color.BLACK);
                posted_image.setVisibility(View.VISIBLE);
                bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                posted_image.setImageBitmap(bitmap);
                play_icon.setVisibility(View.GONE);
                posted_video.setVisibility(View.GONE);
                isMediaChanged = true;
                mediaType = "Image";
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST &&resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);

            if (googlePosition == null){
                googlePosition = new GooglePosition();
                googlePosition.fullAddress = place.getAddress().toString();
                googlePosition.latitude = place.getLatLng().latitude;
                googlePosition.longitude = place.getLatLng().longitude;
                isLocationChanged = true;
            }
        }
    }

    private void getLocation(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            isOpenAutoPlace = true;
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.editpost_btn_delete:
                deletePost();
                break;
            case R.id.save_post_change_btn:
                changePost();
                break;
            case R.id.edit_post_play_icon:
                posted_video.start();
                play_icon.setVisibility(View.GONE);
                break;
            case R.id.edit_post_get_your_location:
                getLocation();
                break;
            case R.id.edit_post_gallery:
                openGallery();
                break;
            case R.id.edit_post_movie:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(checkSelfPermission(android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        openCameraforVideo();
                    }else{
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, PERMISSION_MOVIE_REQUEST_CODE);
                        return;
                    }
                }else
                    openCameraforVideo();
                break;
            case R.id.edit_post_photo:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(checkSelfPermission(android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        openCameraforPhoto();
                    }else{
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
                        return;
                    }
                }else
                    openCameraforPhoto();
                break;
            default:
                break;
        }
    }

    public void openGallery(){
        Intent gallertIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        gallertIntent.setType("image/* video/*");
        startActivityForResult(gallertIntent, REQUEST_GALLERY_CAPTURE);
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void captureImage(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException ex){

            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.brainyapps.footprints.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_FROM_TEMP);
            }
        }
    }

    public void openCameraforPhoto(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            captureImage();
        }else{
            Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(photoCaptureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void openCameraforVideo(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void deletePost() {
        AlertFactory.showAlert(this, "Edit Post", "Are you sure want to delete this?", "YES", "NO", new AlertFactoryClickListener() {
            @Override
            public void onClickYes(final AlertDialog dialog) {
                mDatabase.child(DBInfo.TBL_POST).child(postId).setValue(null);
                mDatabase.child(DBInfo.TBL_USER).child(myUserID).child("posts").child(postId).setValue(null);
                alertMessage("","The post successfully removed.");
                callback();
            }

            @Override
            public void onClickNo(AlertDialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void onClickDone(AlertDialog dialog) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    openCameraforPhoto();
                } else {
                    return;
                }
                return;
            }
            case PERMISSION_MOVIE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    openCameraforVideo();
                } else {
                    return;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void changePost(){
        if(checkValidation()){
            AlertFactory.showAlert(this, "", "Are you sure want to update now?", "YES", "NO", new AlertFactoryClickListener() {
                @Override
                public void onClickYes(final AlertDialog dialog) {
                    if(isMediaChanged){
                        if(mediaType.equals("Image")){
                            if(bitmap!=null){
                                dialog.dismiss();
                                Long tsLong = System.currentTimeMillis();
                                showProgressHUD("");

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                byte[] data = stream.toByteArray();

                                StorageReference filepath = storeMedia.child("Images").child(tsLong+".jpg");
                                filepath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                                        mediaUrl = downloadUri.toString();
                                        savePost();
                                    }
                                });
                            }
                        }else if(mediaType.equals("Video")){
                            mediaType = "Video";
                            if(videoUri!=null){
                                dialog.dismiss();
                                Long tsLong = System.currentTimeMillis();
                                showProgressHUD("");
                                StorageReference videoRef = storeMedia.child("Videos").child(tsLong+"");
                                uploadVidio(videoRef, videoUri);

                            }
                        }
                    }else {
                        dialog.dismiss();
                        savePost();
                    }
                }
                @Override
                public void onClickNo(AlertDialog dialog) {
                    dialog.dismiss();
                }
                @Override
                public void onClickDone(AlertDialog dialog) {
                    dialog.dismiss();
                }
            });
        }
    }

    public void uploadVidio(StorageReference storeRef, Uri selectedVideoUri){
        if(selectedVideoUri != null){
            UploadTask uploadTask = storeRef.putFile(selectedVideoUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    mediaUrl = downloadUri.toString();
                    savePost();
                }
            });
        }
    }

    public void savePost(){
        if(isMediaChanged){
            if(isLocationChanged){
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postTitle").setValue(title.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postDescription").setValue(description.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("googlePosition").setValue(googlePosition);
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("mediaType").setValue(mediaType);
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("mediaUrl").setValue(mediaUrl);
            }else {
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postTitle").setValue(title.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postDescription").setValue(description.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("mediaType").setValue(mediaType);
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("mediaUrl").setValue(mediaUrl);
            }
        }else {
            if(isLocationChanged){
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postTitle").setValue(title.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postDescription").setValue(description.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("googlePosition").setValue(googlePosition);
            }else {
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postTitle").setValue(title.getText().toString());
                mDatabase.child(DBInfo.TBL_POST).child(postId).child("postDescription").setValue(description.getText().toString());
            }
        }
        alertMessage("","The post successfully changed.");
        callback();
    }

    public void callback(){
        super.onBackPressed();
    }

    public boolean checkValidation(){
        if(title.getText().toString().equals("")){
            alertMessage("Error","Please input title.");
            return false;
        }
        if(description.getText().toString().equals("")){
            alertMessage("Error","Please input description now.");
            return false;
        }
        return true;
    }

    public void alertMessage(String title, String message){
        AlertFactory.showAlert(this, title, message, "OKAY", "", new AlertFactoryClickListener() {
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
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        play_icon.setVisibility(View.GONE);
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        super.onPause();

        posted_video.pause();
        play_icon.setVisibility(View.VISIBLE);
        posted_video.hideControls();
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        showProgressHUD("");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        hideProgressHUD();
    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        play_icon.setVisibility(View.VISIBLE);
        posted_video.hideControls();
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        } else if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
