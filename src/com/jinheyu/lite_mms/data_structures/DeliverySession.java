package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-8-17.
 */
public class DeliverySession implements Parcelable {

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
    private final int id;
    private final String plate;
    private boolean locked;
    private List<Order> orderList;

    public DeliverySession(int id, String plate, boolean locked) {
        this.id = id;
        this.plate = plate;
        this.locked = locked;
        this.orderList = new ArrayList<Order>();
    }

    public DeliverySession(int id, String plate) {
        this.id = id;
        this.plate = plate;
        this.orderList = new ArrayList<Order>();
    }

    private DeliverySession(Parcel in) {
        this.id = in.readInt();
        this.plate = in.readString();
        this.locked = (in.readByte() == 1);
    }

    public void addOrder(Order order) {
        orderList.add(order);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public String getPlate() {
        return plate;
    }

    public int getSubOrderCount() {
        int subOrderCount = 0;
        for (Order order : this.orderList) {
            subOrderCount += order.getSubOrderList().size();
        }
        return subOrderCount;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(plate);
        parcel.writeByte((byte) (locked ? 1 : 0));
    }
}
