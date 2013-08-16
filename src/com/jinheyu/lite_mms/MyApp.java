package com.jinheyu.lite_mms;

import android.app.Application;
import android.content.Context;

import com.jinheyu.lite_mms.data_structures.User;
import com.jinheyu.lite_mms.netutils.WebService;

/**
 * Created by xc on 13-8-12.
 */
public class MyApp extends Application {

    public static final String DEFAULT_SERVER_IP = "192.168.0.181";
    public static final int DEFAULT_SERVER_PORT = 80;
    private static Context context;
    private static String server_address;
    private static WebService webServieHandler;
    private static User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
        webServieHandler = WebService.getInstance(MyApp.context);
        Utils.assertDirExists(Utils.getStorageDir());
    }

    public static WebService getWebServieHandler() {
        return webServieHandler;
    }


    public static void setCurrentUser(User currentUser) {
        MyApp.currentUser = currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

}
