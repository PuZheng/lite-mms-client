package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with IntelliJ IDEA.
 * User: yangminghua
 * Date: 13-8-27
 * Time: 下午12:07
 */
public class WorkCommand implements Parcelable {


    public static final Creator<WorkCommand> CREATOR = new Creator<WorkCommand>() {
        @Override
        public WorkCommand createFromParcel(Parcel source) {
            return new WorkCommand(source);
        }

        @Override
        public WorkCommand[] newArray(int size) {
            return new WorkCommand[0];
        }
    };
    private int id;
    private String createTime;
    private int departmentId;
    private int org_cnt;
    private int org_weight;
    private boolean urgent;
    private int previous_procedure_id;
    private int procedure_id;
    private int processed_cnt;
    private int processed_weight;
    private int status;
    private int sub_order_id;
    private String tag;
    private int teamId;
    private String tech_req;
    private String picPath;
    private int handleType;
    private int previousWorkCommandId;
    private String spec;
    private String type;
    private String lastMod;
    private int orderType;

    public WorkCommand(Parcel parcel) {
        this.id = parcel.readInt();
        this.picPath = parcel.readString();
        this.processed_weight = parcel.readInt();
        this.processed_cnt = parcel.readInt();
        this.org_cnt = parcel.readInt();
        this.org_weight = parcel.readInt();
        this.orderType = parcel.readInt();
    }

    public WorkCommand(int id, int org_cnt, int org_weight) {
        this.org_cnt = org_cnt;
        this.org_weight = org_weight;
        this.id = id;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(picPath);
        dest.writeInt(processed_weight);
        dest.writeInt(org_cnt);
        dest.writeInt(orderType);
        dest.writeInt(processed_cnt);
        dest.writeInt(processed_weight);
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getProcessedWeight() {
        return processed_weight;
    }

    public void setProcessedWeight(int processed_weight) {
        this.processed_weight = processed_weight;
    }

    public boolean measured_by_weight() {
        return orderType == Constants.STANDARD_ORDER_TYPE;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public int getOrgCnt() {
        return org_cnt;
    }

    public int getId() {
        return id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getOrgWeight() {
        return org_weight;
    }

    public int getProcessedCnt() {
        return processed_cnt;
    }

    public void setProcessedCnt(int processedCnt) {
        this.processed_cnt = processedCnt;
    }
}
