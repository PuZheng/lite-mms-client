package com.jinheyu.lite_mms.data_structures;

import java.util.ArrayList;
import java.util.List;


public class DeliverySessionDetail {

    private final int id;
    private final String plate;
    private List<Order> orderList;


    public DeliverySessionDetail(int id, String plate) {
        this.id = id;
        this.plate = plate;
        this.orderList = new ArrayList<Order>();
    }

    public void addOrder(Order order) {
        orderList.add(order);
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public int getStoreBillCount() {
        int storeBillCount = 0;
        for (Order order: this.orderList) {
            for (SubOrder subOrder: order.getSubOrderList()) {
                storeBillCount += subOrder.getStoreBillList().size();
            }
        }
        return storeBillCount;
    }

    public int getSubOrderCount() {
        int subOrderCount = 0;
        for (Order order: this.orderList) {
            subOrderCount += order.getSubOrderList().size();
        }
        return subOrderCount;
    }
}
