package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-8-14.
 */
public class UnloadSession implements Parcelable {

    private int id;
    private String plate;
    private boolean locked;


    public UnloadSession(int id, String plate, boolean locked) {
        this.id = id;
        this.plate = plate;
        this.locked = locked;
    }

    private UnloadSession(Parcel in) {
        this.id = in.readInt();
        this.plate = in.readString();
        this.locked = in.readByte() == 1;
    }

    public int getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(plate);
        parcel.writeByte((byte) (locked ? 1 : 0));
    }

    public static final Parcelable.Creator<UnloadSession> CREATOR
            = new Parcelable.Creator<UnloadSession>() {
        public UnloadSession createFromParcel(Parcel in) {
            return new UnloadSession(in);
        }

        public UnloadSession[] newArray(int size) {
            return new UnloadSession[size];
        }
    };

}
