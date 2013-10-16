package com.jinheyu.lite_mms.data_structures;

public interface Constants {
    /**
     * 一般常量
     */
    int TRUE = 1;
    int FALSE = 0;

    /**
     * 订单类型
     */
    int EXTRA_ORDER_TYPE = 2;// 计件类型
    int STANDARD_ORDER_TYPE = 1;// 计重类型
    /**
     * 工单状态
     */
    int STATUS_DISPATCHING = 1;// 待排产
    int STATUS_ASSIGNING = 2;// 待分配
    int STATUS_LOCKED = 3;// 锁定， 待车间主任确认回收
    int STATUS_ENDING = 4;// 待结转或结束
    int STATUS_QUALITY_INSPECTING = 7;// 待质检
    int STATUS_REFUSED = 8;// 车间主任打回
    int STATUS_FINISHED = 9;// 已经结束
    /**
     * 工单操作
     */
    int ACT_CHECK = 201;// (调度员)检货
    int ACT_DISPATCH = 202;// (调度员)排产
    int ACT_ASSIGN = 203;// (车间主任)分配工单
    int ACT_ADD_WEIGHT = 204;// (班组长)增加工单白件重量
    int ACT_END = 205;// (班组长)请求结束
    int ACT_CARRY_FORWARD = 206;// (班组长)请求结转
    int ACT_REFUSE = 209;// (车间主任)打回工单
    int ACT_RETRIEVAL = 210;// (调度员)请求回收工单
    int ACT_AFFIRM_RETRIEVAL = 211;// (车间主任)确认回收
    int ACT_QI = 212;// (质检员)质检
    int ACT_REFUSE_RETRIEVAL = 213;// (车间主任)拒绝回收
    int ACT_RETRIVE_QI = 214;// (质检员)取消质检报告
    int ACT_QUICK_CARRY_FORWARD = 215;// (班组长)快速结转

    /**
     * 处理类型
     */
    int HT_NORMAL = 1;// 正常加工
    int HT_REPLATE = 2;// 返镀
    int HT_REPAIRE = 3;// 返修

    /**
     * 图片sampleSize
     */
    int LARGE_SAMPLE_SIZE = 1;
    int MIDDLE_SAMPLE_SIZE = 2;
    int SMALL_SAMPLE_SIZE = 16;
}

