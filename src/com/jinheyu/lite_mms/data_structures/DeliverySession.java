package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-8-17.
 */
public class DeliverySession implements Parcelable {

    private final int id;
    private final String plate;
    private final boolean locked;

    public DeliverySession(int id, String plate, boolean locked) {
        this.id = id;
        this.plate = plate;
        this.locked = locked;
    }

    private DeliverySession(Parcel in) {
        this.id = in.readInt();
        this.plate = in.readString();
        this.locked = (in.readByte() == 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(plate);
        parcel.writeByte((byte) (locked? 1: 0));
    }


    public static final Parcelable.Creator<DeliverySession> CREATOR =
            new Parcelable.Creator<DeliverySession>() {

                @Override
                public DeliverySession createFromParcel(Parcel parcel) {
                    return new DeliverySession(parcel);
                }

                @Override
                public DeliverySession[] newArray(int i) {
                    return new DeliverySession[i];
                }
            };


    public int getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public boolean isLocked() {
        return locked;
    }
}
