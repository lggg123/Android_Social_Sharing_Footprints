package com.brainyapps.footprints.utils;

import android.os.Environment;

/**
 * Created by SuperMan on 5/3/2018.
 */

public class CheckForSDCard {
    public boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
