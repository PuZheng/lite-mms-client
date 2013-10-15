package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class LogoutDialog {
    private AlertDialog.Builder mBuilder;

    public LogoutDialog(Activity activity) {
        this.mBuilder = initDialog(activity, false);
    }

    /**
     * @param activity
     * @param isOffDuty 是下班，还是一般的登出？
     */
    public LogoutDialog(Activity activity, boolean isOffDuty) {
        this.mBuilder = initDialog(activity, isOffDuty);
    }

    public void doLogout(Activity activity) {
        Utils.clearUserPrefs(activity);
        activity.finish();
        Intent intent = new Intent(activity, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    public void show() {
        mBuilder.show();
    }

    private AlertDialog.Builder initDialog(final Activity activity, final boolean isOffDuty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(isOffDuty ? "确认下班？" : "确认登出?");
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                XProgressableRunnable.Builder<String> runnable = new XProgressableRunnable.Builder<String>(activity);
                runnable.run(new XProgressableRunnable.XRunnable<String>() {
                    @Override
                    public String run() throws Exception {
                        if (isOffDuty) {
                            return MyApp.getWebServieHandler().offDuty();
                        } else {
                            return null;
                        }
                    }
                }).setOkMessageGenerator(new XProgressableRunnable.MessageGenerator<String>() {
                    @Override
                    public String genMessage(String s) {
                        return isOffDuty ? "下班成功" + (Utils.isEmptyString(s) ? "" : " ，结转了" + s + "个工单") : "登出成功";
                    }
                }).after(new Runnable() {
                    @Override
                    public void run() {
                        doLogout(activity);
                    }
                }).create().start();
            }

        });
        return builder;
    }
}
