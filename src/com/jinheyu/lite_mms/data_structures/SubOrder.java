package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;

import com.jinheyu.lite_mms.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-8-19.
 */
public class SubOrder implements Parcelable{

    private List<StoreBill> storeBillList;
    private int id;

    public SubOrder() {
        this.storeBillList = new ArrayList<StoreBill>();
    }

    public SubOrder(int id) {
        this.storeBillList = new ArrayList<StoreBill>();
        this.id = id;
    }

    private SubOrder(Parcel in) {
        this.id = in.readInt();
        this.storeBillList = in.createTypedArrayList(StoreBill.CREATOR);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addStoreBill(StoreBill storeBill) {
        storeBillList.add(storeBill);
    }

    public List<StoreBill> getStoreBillList() {
        return storeBillList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeTypedList(storeBillList);
    }

    public static final Parcelable.Creator<SubOrder> CREATOR = new Parcelable.Creator<SubOrder>() {

        @Override
        public SubOrder createFromParcel(Parcel parcel) {
            return new SubOrder(parcel);
        }

        @Override
        public SubOrder[] newArray(int i) {
            return new SubOrder[i];
        }
    };

    public String getWholeProductName() {
        StringBuilder wholeProductName = new StringBuilder();
        for (StoreBill storeBill: storeBillList) {
            wholeProductName.append(storeBill.getProductName());
            boolean appendRightParen = false;
            String spec = storeBill.getSpec();
            String type = storeBill.getType();
            String specType = Utils.join(new String[]{spec, type}, "/");
            if (!specType.equals("/")) {
                wholeProductName.append(String.format("(%s)", specType));
            }
            break;
        }
        return wholeProductName.toString();
    }
}
