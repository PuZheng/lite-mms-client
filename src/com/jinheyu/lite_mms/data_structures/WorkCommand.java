package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

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
    private final static SparseArray<String> mSatuses = new SparseArray<String>() {{
        put(Constants.STATUS_DISPATCHING, "待排产");
        put(Constants.STATUS_ASSIGNING, "待分配");
        put(Constants.STATUS_LOCKED, "已锁定");
        put(Constants.STATUS_ENDING, "待结转或结束");
        put(Constants.STATUS_QUALITY_INSPECTING, "待质检");
        put(Constants.STATUS_REFUSED, "车间主任打回");
        put(Constants.STATUS_FINISHED, "已结束");
    }};
    private final static SparseArray<String> mHandleTypes = new SparseArray<String>() {{
        put(Constants.HT_NORMAL, "正常加工");
        put(Constants.HT_REPAIRE, "返修");
        put(Constants.HT_REPLATE, "返镀");
    }};
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
    private String unit;
    private int orderNumber;
    private String customerName;
    private boolean reject;

    public WorkCommand(Parcel parcel) {
        this.id = parcel.readInt();
        this.picPath = parcel.readString();
        this.org_weight = parcel.readInt();
        this.org_cnt = parcel.readInt();
        this.orderType = parcel.readInt();
        this.processed_weight = parcel.readInt();
        this.processed_cnt = parcel.readInt();
        this.status = parcel.readInt();
        this.departmentId = parcel.readInt();
        this.teamId = parcel.readInt();
        this.unit = parcel.readString();
        this.customerName = parcel.readString();
        this.orderNumber = parcel.readInt();
        this.urgent = parcel.readInt() == Constants.TRUE;
        this.handleType = parcel.readInt();
        this.reject = parcel.readInt() == Constants.TRUE;
    }

    public WorkCommand(int id, int org_cnt, int org_weight, int status, boolean isUrgent, boolean isRejected) {
        this.org_cnt = org_cnt;
        this.org_weight = org_weight;
        this.id = id;
        this.status = status;
        this.urgent = isUrgent;
        this.reject = isRejected;
    }

    public static String getStatusString(int status) {
        return mSatuses.get(status);
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getHandleType() {
        return handleType;
    }

    public void setHandleType(int handleType) {
        this.handleType = handleType;
    }

    public String getHandleTypeString() {
        return mHandleTypes.get(handleType);
    }

    public int getId() {
        return id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getOrgCnt() {
        return org_cnt;
    }

    public int getOrgWeight() {
        return org_weight;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public int getProcessedCnt() {
        return processed_cnt;
    }

    public void setProcessedCnt(int processedCnt) {
        this.processed_cnt = processedCnt;
    }

    public int getProcessedWeight() {
        return processed_weight;
    }

    public void setProcessedWeight(int processed_weight) {
        this.processed_weight = processed_weight;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusString() {
        return mSatuses.get(status);
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isRejected() {
        return reject;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public boolean measured_by_weight() {
        return orderType == Constants.STANDARD_ORDER_TYPE;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
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
        dest.writeInt(org_weight);
        dest.writeInt(org_cnt);
        dest.writeInt(orderType);
        dest.writeInt(processed_weight);
        dest.writeInt(processed_cnt);
        dest.writeInt(status);
        dest.writeInt(departmentId);
        dest.writeInt(teamId);
        dest.writeString(unit);
        dest.writeString(customerName);
        dest.writeInt(orderNumber);
        dest.writeInt(urgent ? Constants.TRUE : Constants.FALSE);
        dest.writeInt(handleType);
        dest.writeInt(reject ? Constants.TRUE : Constants.FALSE);
    }
}