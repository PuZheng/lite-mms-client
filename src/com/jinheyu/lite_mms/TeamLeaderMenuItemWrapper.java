package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.jinheyu.lite_mms.data_structures.Constants;

import java.util.Arrays;

/**
 * Created by abc549825@163.com
 * 2013-09-11
 */
public class TeamLeaderMenuItemWrapper {
    private Activity mActivity;

    public TeamLeaderMenuItemWrapper(Activity activity) {
        this.mActivity = activity;
    }

    public void carryForward(final int workCommandId) {
        newBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommandId),
                String.format("工单%d请求结转中", workCommandId),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public void run() throws Exception {
                        MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                    }
                },
                mActivity.getString(R.string.carryForward_success, workCommandId)
        ).show();
    }

    public void carryForward(final int[] workCommandIds) {
        final String workCommands_str = Arrays.toString(workCommandIds);

        newBuilder(mActivity.getString(R.string.confirm_carry_forward, workCommands_str),
                String.format("工单%s批量结转中", workCommands_str),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public void run() throws Exception {
                        for (int workCommandId : workCommandIds) {
                            MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                        }
                    }
                },
                mActivity.getString(R.string.carryForward_success, workCommands_str)
        ).show();

    }

    public void carryForwardQuickly(final int workCommandId) {
        newBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandId),
                String.format("工单%d快速结转中", workCommandId),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public void run() throws Exception {
                        MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_QUICK_CARRY_FORWARD, null);
                    }
                },
                mActivity.getString(R.string.quick_carryForward_success, workCommandId)
        ).show();
    }

    public void carryForwardQuickly(final int[] workCommandIds) {
        final String workCommandIds_str = Arrays.toString(workCommandIds);

        newBuilder(mActivity.getString(R.string.confirm_carry_forward_quickly, workCommandIds_str),
                String.format("工单%s批量快速结转中", workCommandIds_str),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public void run() throws Exception {
                        for (int workCommandId : workCommandIds) {
                            MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_QUICK_CARRY_FORWARD, null);
                        }
                    }
                },
                mActivity.getString(R.string.quick_carryForward_success, workCommandIds_str)
        ).show();
    }

    public void endWorkCommand(final int workCommandId) {
        newBuilder(mActivity.getString(R.string.confirm_end, workCommandId),
                String.format("工单%d请求结束中", workCommandId),
                new XProgressableRunnable.XRunnable() {
                    @Override
                    public void run() throws Exception {
                        MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_END, null);
                    }
                },
                mActivity.getString(R.string.end_success, workCommandId)
        ).show();
    }

    private AlertDialog.Builder newBuilder(final String titleString, final String startString, final XProgressableRunnable.XRunnable runnable, final String okString) {
        return new AlertDialog.Builder(mActivity)
                .setTitle(titleString)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
                        builder.msg(startString).run(runnable).after(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.onNavigateUp();
                            }
                        }).okMsg(okString).create().start();
                    }
                });
    }
}