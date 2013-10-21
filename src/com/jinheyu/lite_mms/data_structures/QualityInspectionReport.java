package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-10-7.
 */
public class QualityInspectionReport implements Parcelable {

    public static final int FINISHED = 1;
    public static final int NEXT_PROCEDURE = 2;
    public static final int REPAIR = 3;
    public static final int REPLATE = 4;
    public static final int DISCARD = 5;
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QualityInspectionReport createFromParcel(Parcel in) {
            return new QualityInspectionReport(in);
        }

        public QualityInspectionReport[] newArray(int size) {
            return new QualityInspectionReport[size];
        }
    };
    private int id;
    private int quantity;
    private int weight;
    private int result;
    private int actorId;
    private String picUrl;
    private String localPicPath;
    private String smallPicUrl;

    public QualityInspectionReport(int id, int quantity, int weight, int result,
                                   int actorId, String picUrl, String smallPicUrl) {
        this.id = id;
        this.quantity = quantity;
        this.weight = weight;
        this.result = result;
        this.actorId = actorId;
        this.picUrl = picUrl;
        this.smallPicUrl = smallPicUrl;
    }

    public QualityInspectionReport(Parcel in) {
        id = in.readInt();
        quantity = in.readInt();
        weight = in.readInt();
        result = in.readInt();
        actorId = in.readInt();
        picUrl = in.readString();
        localPicPath = in.readString();
        smallPicUrl = in.readString();
    }

    public QualityInspectionReport() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getActorId() {
        return actorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLiterableResult() {
        String ret = "";
        switch (result) {
            case FINISHED:
                ret = "完成";
                break;
            case NEXT_PROCEDURE:
                ret = "转下道工序";
                break;
            case REPAIR:
                ret = "返镀";
                break;
            case REPLATE:
                ret = "返修";
                break;
            case DISCARD:
                ret = "废弃";
                break;
            default:
                break;
        }
        return ret;
    }

    public String getLocalPicPath() {
        return localPicPath;
    }

    public void setLocalPicPath(String localPicPath) {
        this.localPicPath = localPicPath;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSmallPicUrl() {
        return smallPicUrl;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(quantity);
        dest.writeInt(weight);
        dest.writeInt(result);
        dest.writeInt(actorId);
        dest.writeString(picUrl);
        dest.writeString(localPicPath);
        dest.writeString(smallPicUrl);
    }
}
