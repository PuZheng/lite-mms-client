package com.jinheyu.lite_mms.data_structures;

/**
 * Created by xc on 13-10-7.
 */
public class QualityInspectionReport {

    private static final int FINISHED = 1;
    private static final int NEXT_PROCEDURE = 2;
    private static final int REPAIR = 3;
    private static final int REPLATE = 4;
    private static final int DISCARD = 5;

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
}
