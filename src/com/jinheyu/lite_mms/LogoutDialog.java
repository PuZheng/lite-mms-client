package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class LogoutDialog {
    private AlertDialog.Builder mBuilder;

    public LogoutDialog(Activity activity) {
        this.mBuilder = initDialog(activity);
    }

    public void show() {
        mBuilder.show();
    }

    private AlertDialog.Builder initDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("您确认要登出?");
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.clearUserToken(activity);
                activity.finish();
                Intent intent = new Intent(activity, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        });
        return builder;
    }
}
