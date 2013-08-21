package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-8-18.
 */
public class StoreBill implements Parcelable {

    private final int id;
    private final String harborName;
    private final String productName;
    private final String customerName;
    private final String picUrl;
    private final String unit;
    private final int weight;
    private final String spec;
    private final String type;

    public StoreBill(int id, String harborName, String productName, String customerName,
                     String picUrl, String unit, int weight, String spec, String type) {
        this.id = id;
        this.harborName = harborName;
        this.productName = productName;
        this.customerName = customerName;
        this.picUrl = picUrl;
        this.unit = unit;
        this.weight = weight;
        this.spec = spec;
        this.type = type;
    }

    private StoreBill(Parcel in) {
        this.id = in.readInt();
        this.harborName = in.readString();
        this.productName = in.readString();
        this.customerName = in.readString();
        this.picUrl = in.readString();
        this.unit = in.readString();
        this.weight = in.readInt();
        this.spec = in.readString();
        this.type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(harborName);
        parcel.writeString(productName);
        parcel.writeString(customerName);
        parcel.writeString(picUrl);
        parcel.writeString(unit);
        parcel.writeInt(weight);
        parcel.writeString(spec);
        parcel.writeString(type);
    }

    public static final Parcelable.Creator<StoreBill> CREATOR = new Parcelable.Creator<StoreBill>() {
        @Override
        public StoreBill createFromParcel(Parcel parcel) {
            return new StoreBill(parcel);
        }

        @Override
        public StoreBill[] newArray(int i) {
            return new StoreBill[i];
        }
    };


    public int getId() {
        return id;
    }

    public String getHarborName() {
        return harborName;
    }

    public String getProductName() {
        return productName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getUnit() {
        return unit;
    }

    public String getSpec() {
        return spec;
    }

    public String getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }
}
