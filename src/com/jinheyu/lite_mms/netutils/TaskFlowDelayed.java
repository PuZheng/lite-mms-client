package com.jinheyu.lite_mms.netutils;

/**
 * Created by xc on 13-9-5.
 */
public class TaskFlowDelayed extends Exception {


    /**
     *
     */
    private static final long serialVersionUID = -4481187403136393864L;

    public TaskFlowDelayed(String reason) {
        super(reason);
    }
}
