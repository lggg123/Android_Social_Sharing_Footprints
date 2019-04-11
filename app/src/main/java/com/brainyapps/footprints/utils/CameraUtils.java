package com.brainyapps.footprints.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by HappyBear on 9/5/2018.
 */

public class CameraUtils {
    //Get Uri Of captured Image
    public static Uri getOutputMediaFileUri(Context context) {
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Camera");
        //If File is not present create directory
        if (!mediaStorageDir.exists()) {
            if (mediaStorageDir.mkdir())
                Log.e("Create Directory", "Main Directory Created : " + mediaStorageDir);
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());//Get Current timestamp
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");//create image path with system mill and image format
        return Uri.fromFile(mediaFile);

    }

    /*  Convert Captured image path into Bitmap to display over ImageView  */
    public static Bitmap convertImagePathToBitmap(String imagePath, boolean scaleBitmap) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);//Decode image path

        //If you want to scale bitmap/reduce captured image size then send true
        if (scaleBitmap)
            return Bitmap.createScaledBitmap(bitmap, 500, 500, true);
        else
            //if you don't want to scale bitmap then send false
            return bitmap;
    }
}
