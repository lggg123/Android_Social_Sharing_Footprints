package com.brainyapps.footprints.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.brainyapps.footprints.constants.DBInfo;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SuperMan on 4/23/2018.
 */

public class Report implements Parcelable{
    public static final String TABLE_NAME = "reports";
    public String reportId = "";
    public String reporterId = "";
    public String reportedId = "";
    public String reportContent = "";

    public Report(){
        reportId = "";
        reporterId = "";
        reportedId = "";
        reportContent = "";
    }

    protected Report(Parcel in) {
        reportId = in.readString();
        reporterId = in.readString();
        reportedId = in.readString();
        reportContent = in.readString();
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel parcel) {
            return new Report(parcel);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBInfo.REPORT_ID,reportId);
        result.put(DBInfo.REPORT_BY, reporterId);
        result.put(DBInfo.REPORT_IN, reportedId);
        result.put(DBInfo.REPORT_CONTENT, reportContent);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(reportId);
        parcel.writeString(reporterId);
        parcel.writeString(reportedId);
        parcel.writeString(reportContent);
    }
}
