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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.brainyapps.footprints.constants.DBInfo;
import com.brainyapps.footprints.models.GooglePosition;
import com.brainyapps.footprints.models.Post;
import com.brainyapps.footprints.models.User;
import com.brainyapps.footprints.utils.CameraUtils;
import com.brainyapps.footprints.utils.ImagePath_MarshMallow;
import com.brainyapps.footprints.utils.ImagePicker;
import com.brainyapps.footprints.views.AlertFactory;
import com.brainyapps.footprints.views.AlertFactoryClickListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.walnutlabs.android.ProgressHUD;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.Manifest;

public class PostActivity extends AppCompatActivity implements View.OnClickListener, LocationListener{

    private final int REQUEST_IMAGE_CAPTURE = 20;
    private final int REQUEST_IMAGE_CAPTURE_FROM_TEMP = 26;
    private final int REQUEST_VIDEO_CAPTURE = 21;
    private final int REQUEST_GALLERY_CAPTURE = 22;
    private final int PLACE_PICKER_REQUEST = 23;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 24;
    private static final int PERMISSION_MOVIE_REQUEST_CODE = 25;

    public EditText post_title;
    public EditText post_description;
    public LinearLayout get_location;
    public LinearLayout post_photo_from_camera;
    public LinearLayout post_from_gallery;
    public LinearLayout post_video_from_camera;
    private FirebaseUser user;

    private DatabaseReference mDatabase;
    private StorageReference storeMedia;
    private Bitmap bitmap;

    private RelativeLayout media_container;
    private ImageView image_viewer;
    private ImageView video_play_button;
    private VideoView video_viewer;

    private Uri photoUri;
    private Uri videoUri;

    private String getImageUrl = "";
    private String mediaUrl;
    private GooglePosition googlePosition;

    private GoogleApiClient mGoogleApiClient;
    Location myLocation;

    String mediaType = "Image";
    private boolean isOpenAutoPlace = false;

    private ProgressHUD mProgressDialog;
    private void showProgressHUD(String text) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mProgressDialog = ProgressHUD.show(PostActivity.this, text, true);
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
        setContentView(R.layout.activity_post);

        mediaUrl = "";
        user =  FirebaseAuth.getInstance().getCurrentUser();

        ImageView btn_back = (ImageView) findViewById(R.id.post_btn_back);
        btn_back.setOnClickListener(this);
        TextView btn_post = (TextView) findViewById(R.id.post_btn_post);
        btn_post.setOnClickListener(this);

        get_location = (LinearLayout) findViewById(R.id.get_your_location);
        get_location.setOnClickListener(this);
        post_photo_from_camera = (LinearLayout) findViewById(R.id.post_photo);
        post_photo_from_camera.setOnClickListener(this);
        post_from_gallery = (LinearLayout) findViewById(R.id.post_gallery);
        post_from_gallery.setOnClickListener(this);
        post_video_from_camera = (LinearLayout) findViewById(R.id.post_movie);
        post_video_from_camera.setOnClickListener(this);

        post_title = (EditText) findViewById(R.id.post_title_edit);
        post_description = (EditText) findViewById(R.id.post_description_edit);

        image_viewer = (ImageView) findViewById(R.id.post_image_viewer);
        image_viewer.setOnClickListener(this);
        video_viewer = (VideoView) findViewById(R.id.post_video_viewer);
        video_play_button = (ImageView) findViewById(R.id.post_video_play_icon);
        video_play_button.setOnClickListener(this);

        media_container = (RelativeLayout) findViewById(R.id.post_media_container);

        mDatabase = FirebaseDatabase.getInstance().getReference();
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

    public void postGotoBackpage(){
        super.onBackPressed();
    }

    public void postArticle(){
        if(validationCheck()){
            AlertFactory.showAlert(this, "", "Are you sure want to post now?", "YES", "NO", new AlertFactoryClickListener() {
                @Override
                public void onClickYes(final AlertDialog dialog) {
                    if(checkMediaType() == "Image"){
                        mediaType = "Image";
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
                    }else if(checkMediaType() == "Video"){
                        mediaType = "Video";
                        if(videoUri!=null){
                            dialog.dismiss();
                            Long tsLong = System.currentTimeMillis();
                            showProgressHUD("");
                            StorageReference videoRef = storeMedia.child("Videos").child(tsLong+"");
                            uploadVidio(videoRef, videoUri);

                        }
                    }
                    else{
                        savePost();
                        dialog.dismiss();
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

    private boolean validationCheck(){
        if(TextUtils.isEmpty(post_title.getText().toString())){
            alertMessage("","Please input title.");
            post_title.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(post_description.getText().toString())){
            alertMessage("","Please input description.");
            post_description.requestFocus();
            return false;
        }
        if(googlePosition == null){
            if(myLocation != null){
                googlePosition = new GooglePosition();
                googlePosition.longitude = myLocation.getLongitude();
                googlePosition.latitude = myLocation.getLatitude();
            }else {
                alertMessage("","Please click Location to input location.");
                return false;
            }
        }
        if(checkMediaType() == null){
            alertMessage("","Please select media to post.");
            return false;
        }
        return true;
    }

    private String checkMediaType(){
        if(image_viewer.getVisibility() == View.VISIBLE){
            return "Image";
        }else if(video_viewer.getVisibility() == View.VISIBLE){
            return "Video";
        }else {
            return null;
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
        String title = post_title.getText().toString();
        String description = post_description.getText().toString();
        String userId = user.getUid();
        Long current_time = System.currentTimeMillis();

        Post new_post = new Post();
        new_post.userId = userId;
        new_post.postTitle = title;
        new_post.postDescription = description;
        new_post.postedTime = current_time;
        new_post.mediaUrl = mediaUrl;
        new_post.mediaType = mediaType;
        new_post.googlePosition = googlePosition;

        String postId = mDatabase.child(Post.TABLE_NAME).push().getKey();
        new_post.postId = postId;

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("/" + DBInfo.TBL_USER + "/" + userId + "/" + "posts" + "/" + postId, postId);
        userUpdates.put("/" + Post.TABLE_NAME + "/" + postId, new_post);
        mDatabase.updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressHUD();
                AlertFactory.showAlert(PostActivity.this, "", "Successfully posted!" , "OKAY", "", new AlertFactoryClickListener() {
                    @Override
                    public void onClickYes(AlertDialog dialog) {

                    }
                    @Override
                    public void onClickNo(AlertDialog dialog) {

                    }
                    @Override
                    public void onClickDone(AlertDialog dialog) {
                        redirecttoMap();
                    }
                });
            }
        });
    }

    public void redirecttoMap(){
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post_btn_back:
                postGotoBackpage();
                break;
            case R.id.post_btn_post:
                postArticle();
                break;
            case R.id.get_your_location:
                getLocation();
                break;
            case R.id.post_gallery:
                openGallery();
                break;
            case R.id.post_movie:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        openCameraforVideo();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, PERMISSION_MOVIE_REQUEST_CODE);
                        return;
                    }
                }else
                    openCameraforVideo();
                break;
            case R.id.post_photo:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                        openCameraforPhoto();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
                        return;
                    }
                }else
                    openCameraforPhoto();
                break;
            case R.id.post_video_play_icon:
                playvideo();
                break;
            case R.id.post_image_viewer:
                pausevideo();
            default:
                break;
        }
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

    public void openGallery(){
        Intent gallertIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        gallertIntent.setType("image/* video/*");
        startActivityForResult(gallertIntent, REQUEST_GALLERY_CAPTURE);
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException{
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

    public void playvideo(){
        if(video_viewer.isPlaying()){
            video_viewer.resume();
            video_play_button.setVisibility(View.GONE);
            return;
        }
        video_play_button.setVisibility(View.GONE);
        MediaController mc = new MediaController(this);
        mc.setAnchorView(video_viewer);
        video_viewer.setMediaController(mc);
        video_viewer.start();
        video_viewer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                video_play_button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void pausevideo(){
        if(video_viewer.isPlaying()){
            video_viewer.pause();
            video_play_button.setVisibility(View.VISIBLE);
        }
    }

    public Uri getImageUri(Bitmap inImage){
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage,"temp_file", null);
        return Uri.parse(path);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null ) {
            media_container.setBackgroundColor(Color.BLACK);
            image_viewer.setVisibility(View.VISIBLE);

            bitmap = ImagePicker.getImageFromResult(this, resultCode, data);

//            if(data.getData() != null){
//                bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
//            }else {
//                bitmap = ImagePicker.getBitmapFromPath(this, mCurrentPhotoPath);
//            }
            image_viewer.setImageBitmap(bitmap);
            video_play_button.setVisibility(View.GONE);
            video_viewer.setVisibility(View.GONE);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_TEMP && resultCode == RESULT_OK) {
            media_container.setBackgroundColor(Color.BLACK);
            image_viewer.setVisibility(View.VISIBLE);

            bitmap = ImagePicker.getBitmapFromPath(this, mCurrentPhotoPath);

            image_viewer.setImageBitmap(bitmap);
            video_play_button.setVisibility(View.GONE);
            video_viewer.setVisibility(View.GONE);
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            media_container.setBackgroundColor(Color.BLACK);
            image_viewer.setVisibility(View.GONE);
            video_viewer.setVisibility(View.VISIBLE);
            video_play_button.setVisibility(View.VISIBLE);
            if(data.getData()!=null){
                videoUri = data.getData();
                video_viewer.setVideoURI(videoUri);
            }
        }
        if (requestCode == REQUEST_GALLERY_CAPTURE && resultCode == RESULT_OK) {
            String path = data.getData().getPath();
            if (path.contains("/video/")) {
                media_container.setBackgroundColor(Color.BLACK);
                image_viewer.setVisibility(View.GONE);
                video_viewer.setVisibility(View.VISIBLE);
                video_play_button.setVisibility(View.VISIBLE);
                videoUri = data.getData();
                video_viewer.setVideoURI(videoUri);
            } else if (path.contains("/images/")) {
                media_container.setBackgroundColor(Color.BLACK);
                image_viewer.setVisibility(View.VISIBLE);
                bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                image_viewer.setImageBitmap(bitmap);
                video_play_button.setVisibility(View.GONE);
                video_viewer.setVisibility(View.GONE);
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST &&resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);

            if (googlePosition == null){
                googlePosition = new GooglePosition();
                googlePosition.fullAddress = place.getAddress().toString();
                googlePosition.latitude = place.getLatLng().latitude;
                googlePosition.longitude = place.getLatLng().longitude;
            }
        }
    }

    private void showCapturedImage() {
        if (!getImageUrl.equals("") && getImageUrl != null)
            image_viewer.setImageBitmap(CameraUtils.convertImagePathToBitmap(getImageUrl, false));
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
    public void onLocationChanged(Location location) {
        myLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (s.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        } else if (s.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
