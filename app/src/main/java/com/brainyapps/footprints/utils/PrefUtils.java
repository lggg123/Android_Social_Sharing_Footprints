package com.brainyapps.footprints.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {

    public static final String PREF_TUTORIAL_ON = "pref_tutorial_on";

    public static final String PREF_FACEBOOK_ON         = "preference_facebook_on";

    public static final String PREF_GOOGLE_ON           = "preference_google_on";

    public static final String PREF_USER_LOGGED_IN      = "preference_user_logged_in";

    public static final String PREF_USER_TYPE           = "preference_user_type";

    // Logged type
    public static final int STATUS_LOGGED_OUT           = 0;
    public static final int STATUS_LOGGED_IN_EMAIL      = 1;
    public static final int STATUS_LOGGED_IN_FACEBOOK   = 2;
    public static final int STATUS_LOGGED_IN_GOOGLE     = 4;


    public static final String DEFAULT_FILE = "pref_default_filepath";

    private static PrefUtils instance;

    private SharedPreferences prefs;

    private PrefUtils(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PrefUtils getInstance() {
        return instance;
    }

    public static void init(Context context) {
        instance = new PrefUtils(context);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).commit();
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).commit();
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
    }

    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).commit();
    }

    public void putFloat(String key, float value) {
        prefs.edit().putFloat(key, value).commit();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return prefs.getFloat(key, defaultValue);
    }
}