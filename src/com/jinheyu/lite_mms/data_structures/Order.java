package com.jinheyu.lite_mms.data_structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-8-19.
 */
public class Order {
    public static int STANDARD_ORDER_TYPE = 1;
    public static int EXTRA_ORDER_TYPE = 2;

    private List<SubOrder> subOrderList;
    private int id;
    private String customerOrderNumber;


    public Order() {
        this.subOrderList = new ArrayList<SubOrder>();
    }

    public Order(int id, String customerOrderNumber) {
        this.id = id;
        this.customerOrderNumber = customerOrderNumber;
        this.subOrderList = new ArrayList<SubOrder>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerOrderNumber() {
        return customerOrderNumber;
    }

    public void setCustomerOrderNumber(String customerOrderNumber) {
        this.customerOrderNumber = customerOrderNumber;
    }

    public void addSubOrder(SubOrder subOrder) {
        subOrderList.add(subOrder);
    }

    public List<SubOrder> getSubOrderList() {
        return this.subOrderList;
    }

    public String getCustomerName() {
        String customerName = "";
        for (SubOrder subOrder: subOrderList) {
            for (StoreBill storeBill: subOrder.getStoreBillList()) {
                customerName = storeBill.getCustomerName();
                break;
            }
        }
        return customerName;
    }

}
