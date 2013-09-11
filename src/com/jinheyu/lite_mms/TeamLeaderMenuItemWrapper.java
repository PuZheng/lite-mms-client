package com.jinheyu.lite_mms;

import android.app.Activity;
import android.widget.Toast;
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
        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
        builder.msg(String.format("工单%d请求结转中", workCommandId)).run(new XProgressableRunnable.XRunnable() {
            @Override
            public void run() throws Exception {
                MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
            }
        }).okMsg(mActivity.getString(R.string.carryForward_success, workCommandId)).create().start();
    }

    public void carryForward(final long[] workCommandIds) {
        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
        builder.msg(String.format("工单%s批量结转中", Arrays.toString(workCommandIds))).run(new XProgressableRunnable.XRunnable() {
            @Override
            public void run() throws Exception {
                for (long workCommandId : workCommandIds) {
                    MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_CARRY_FORWARD, null);
                }
            }
        }).okMsg(mActivity.getString(R.string.carryForward_success, Arrays.toString(workCommandIds))).create().start();
    }

    public void carryForwardQuickly(final int workCommandId) {
        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
        builder.msg(String.format("工单%d快速结转中", workCommandId)).run(new XProgressableRunnable.XRunnable() {
            @Override
            public void run() throws Exception {
                MyApp.getWebServieHandler().updateWorkCommand(workCommandId, Constants.ACT_QUICK_CARRY_FORWARD, null);
            }
        }).after(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity,
                        mActivity.getString(R.string.quick_carryForward_success, workCommandId), Toast.LENGTH_SHORT).show();
            }
        }).create().start();
    }

    public void endWorkCommand(final int workCommand) {
        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(mActivity);
        builder.msg(String.format("工单%d请求结束中", workCommand)).run(new XProgressableRunnable.XRunnable() {
            @Override
            public void run() throws Exception {
                MyApp.getWebServieHandler().updateWorkCommand(workCommand, Constants.ACT_END, null);
            }
        }).okMsg(mActivity.getString(R.string.end_success, workCommand)).create().start();
    }
}
