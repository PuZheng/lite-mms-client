package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xc on 13-10-7.
 */
public class QualityInspectionReport implements Parcelable {

    private static final int FINISHED = 1;
    private static final int NEXT_PROCEDURE = 2;
    private static final int REPAIR = 3;
    private static final int REPLATE = 4;
    private static final int DISCARD = 5;

    private final int id;
    private final int quantity;
    private final int weight;
    private final int result;
    private final int actorId;
    private final String picUrl;

    public QualityInspectionReport(int id, int quantity, int weight, int result,
                                   int actorId, String picUrl) {
        this.id = id;
        this.quantity = quantity;
        this.weight = weight;
        this.result = result;
        this.actorId = actorId;
        this.picUrl = picUrl;
    }

    public QualityInspectionReport(Parcel in) {
        id = in.readInt();
        quantity = in.readInt();
        weight = in.readInt();
        result = in.readInt();
        actorId = in.readInt();
        picUrl = in.readString();
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getWeight() {
        return weight;
    }

    public int getResult() {
        return result;
    }

    public int getActorId() {
        return actorId;
    }

    public String getPicUrl() {
        return picUrl;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(quantity);
        dest.writeInt(weight);
        dest.writeInt(result);
        dest.writeInt(actorId);
        dest.writeString(picUrl);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QualityInspectionReport createFromParcel(Parcel in) {
            return new QualityInspectionReport(in);
        }

        public QualityInspectionReport[] newArray(int size) {
            return new QualityInspectionReport[size];
        }
    };
}
