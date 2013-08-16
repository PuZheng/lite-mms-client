package com.jinheyu.lite_mms.data_structures;

import com.jinheyu.lite_mms.LoaderMainActivity;
import com.jinheyu.lite_mms.LogInActivity;

/**
 * Created by xc on 13-8-12.
 */
public class User {
    private static final int LOADER = 3;
    private String userName;
    private String token;
    private int groupId;
    private int id;

    public User(int id, String userName, String token, int groupId) {
        this.id = id;
        this.userName = userName;
        this.token = token;
        this.groupId = groupId;
    }

    public String getUserName() {
        return userName;
    }


    public String getToken() {
        return token;
    }

    public Class getDefaultActivity() {
        switch (groupId) {
            case LOADER:
                return LoaderMainActivity.class;
            default:
                return LogInActivity.class;
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public int getId() {
        return id;
    }
}
