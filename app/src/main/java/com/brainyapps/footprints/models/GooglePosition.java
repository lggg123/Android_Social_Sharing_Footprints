package com.brainyapps.footprints.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SuperMan on 4/20/2018.
 */

public class GooglePosition implements Parcelable {
    public String fullAddress;
    public String state;
    public String city;

    public double latitude;
    public double longitude;

    public GooglePosition() {
        this.fullAddress = "";
        this.state = "";
        this.city = "";

        latitude = 0.0;
        longitude = 0.0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("full_address", fullAddress);
        result.put("city", city);
        result.put("state", state);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        return result;
    }

    protected GooglePosition(Parcel in) {
        fullAddress = in.readString();
        city = in.readString();
        state = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {return longitude; }

    public static final Creator<GooglePosition> CREATOR = new Creator<GooglePosition>() {
        @Override
        public GooglePosition createFromParcel(Parcel in) {
            return new GooglePosition(in);
        }

        @Override
        public GooglePosition[] newArray(int size) {
            return new GooglePosition[size];
        }
    };

    public boolean containLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fullAddress);
        parcel.writeString(city);
        parcel.writeString(state);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}