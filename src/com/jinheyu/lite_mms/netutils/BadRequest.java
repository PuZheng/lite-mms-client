package com.jinheyu.lite_mms.netutils;

public class BadRequest extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -4481187403136393864L;

    public BadRequest(String reason) {
        super(reason);
    }
}
