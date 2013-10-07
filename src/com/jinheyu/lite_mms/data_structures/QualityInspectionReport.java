package com.jinheyu.lite_mms.data_structures;

/**
 * Created by xc on 13-10-7.
 */
public class QualityInspectionReport {
    private final int id;
    private final int quantity;
    private final int weight;
    private final int result;
    private final int workCommandId;
    private final int actorId;

    public QualityInspectionReport(int id, int quantity, int weight, int result, int workCommandId, int actorId) {
        this.id = id;
        this.quantity = quantity;
        this.weight = weight;
        this.result = result;
        this.workCommandId = workCommandId;
        this.actorId = actorId;
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

    public int getWorkCommandId() {
        return workCommandId;
    }

    public int getActorId() {
        return actorId;
    }
}
