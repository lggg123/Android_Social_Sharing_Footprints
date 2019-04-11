package com.brainyapps.footprints.views;

import android.support.v7.app.AlertDialog;

/**
 * Created by SuperMan on 4/14/2018.
 */

public interface AlertFactoryClickListener {
    void onClickYes(AlertDialog dialog);

    void onClickNo(AlertDialog dialog);

    void onClickDone(AlertDialog dialog);

}
