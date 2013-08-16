package com.jinheyu.lite_mms.netutils;

/**
 * Created by xc on 13-8-12.
 */
public class ValidationError extends Exception {
    public ValidationError(String reason) {
        super(reason);
    }
}
