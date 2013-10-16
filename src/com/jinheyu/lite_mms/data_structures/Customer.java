package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-8-15.
 */
public class Customer implements Parcelable {

    private int id;
    private String abbr;
    private String name;

    public Customer(int id, String name, String abbr) {
        this.abbr = abbr;
        this.name = name;
        this.id = id;
    }

    private Customer(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.abbr = in.readString();
    }

    public String getAbbr() {
        return abbr;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(abbr);
    }

    public static final Parcelable.Creator<Customer> CREATOR
            = new Parcelable.Creator<Customer>() {
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

}
