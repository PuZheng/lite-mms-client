package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-8-15.
 */
public class Harbor implements Parcelable {


    private final String name;

    public Harbor(String name) {
        this.name = name;
    }

    private Harbor(Parcel in) {
        this.name = in.readString();
    }


    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<Harbor> CREATOR
            = new Parcelable.Creator<Harbor>() {
        public Harbor createFromParcel(Parcel in) {
            return new Harbor(in);
        }

        public Harbor[] newArray(int size) {
            return new Harbor[size];
        }
    };


}
