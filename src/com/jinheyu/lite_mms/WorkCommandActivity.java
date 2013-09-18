package com.jinheyu.lite_mms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.User;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.util.HashMap;


public class WorkCommandActivity extends FragmentActivity implements DialogFragmentProxy.DialogProxyListener,
        ImageFragment.ImageFragmentListener {
    private WorkCommand mWorkCommand;
    private EditText weightTextView;
    private EditText cntTextView;

    @Override
    public View getDefaultFragmentView() {
        LayoutInflater inflater = getLayoutInflater();
        View rootView = inflater.inflate(R.layout.fragement_add_weight, null);

        TextView mTextView = (TextView) rootView.findViewById(R.id.dialog_add_currentweight);
        mTextView.setText(String.format("%d 千克", mWorkCommand.getProcessedWeight()));

        weightTextView = (EditText) rootView.findViewById(R.id.dialog_add_edittext_weight);
        cntTextView = (EditText) rootView.findViewById(R.id.dialog_add_edittext_count);
        if (!mWorkCommand.measured_by_weight()) {
            rootView.findViewById(R.id.count_row).setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public String getFragmentPicUrl() {
        return mWorkCommand.getPicPath();
    }

    @Override
    public int getNeutralButtonId() {
        return R.string.part_weight;
    }

    @Override
    public Intent getParentActivityIntent() {
        return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
    }

    @Override
    public int getPositiveButtonId() {
        return R.string.completely_weight;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (MyApp.getCurrentUser().getGroupId()) {
            case User.TEAM_LEADER:
                _setTeamLeaderMenu(menu);
                break;
            case User.DEPARTMENT_LEADER:
                _setDepartmentLeaderMenu(menu);
            default:
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialogFragment) {
        _addWeight(false);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {
        _addWeight(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItemWrapper menuItemWrapper = new MenuItemWrapper(this);
        int workCommandId = mWorkCommand.getId();
        switch (item.getItemId()) {
            case R.id.quick_carryForward:
                menuItemWrapper.carryForwardQuickly(workCommandId);
                break;
            case R.id.carry_forward:
                menuItemWrapper.carryForward(workCommandId);
                break;
            case R.id.end_work_command:
                menuItemWrapper.endWorkCommand(workCommandId);
                break;
            case R.id.add_weight:
                if (mWorkCommand.getStatus() == Constants.STATUS_LOCKED) {
                    Toast.makeText(this, R.string.locked_work_command_warning, Toast.LENGTH_SHORT).show();
                } else {
                    DialogFragmentProxy dialog = new DialogFragmentProxy();
                    dialog.show(getSupportFragmentManager(), "AddWeightDialog");
                }
                break;
            case R.id.action_dispatch:
                menuItemWrapper.dispatch(mWorkCommand.getId(), mWorkCommand.getDepartmentId());
                break;
            case R.id.action_refuse:
                menuItemWrapper.refuse(mWorkCommand.getId());
                break;
            case R.id.action_confirm_retrieve:
                menuItemWrapper.confirm_retrieve(workCommandId);
                break;
            case R.id.action_deny_retrieve:
                menuItemWrapper.deny_retrieve(workCommandId);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_command_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        _initView();
    }

    /**
     * @param isFinished 是否完全称重
     */
    private void _addWeight(final boolean isFinished) {
        final int weight = Utils.parseInt(weightTextView.getText().toString(), 0);
        final int cnt = Utils.parseInt(cntTextView.getText().toString(), 0);
        if (_checkWeightAndCntValue(weight, cnt)) {
            if (isFinished) {
                String alertMessage = null;
                if (weight + mWorkCommand.getProcessedWeight() < mWorkCommand.getOrgWeight()) {
                    alertMessage = getString(R.string.previous_weight_greater);
                }
                if (!mWorkCommand.measured_by_weight() && (cnt + mWorkCommand.getProcessedCnt() < mWorkCommand.getOrgCnt())) {
                    alertMessage = getString(R.string.previous_count_greater);
                }
                if (!Utils.isEmptyString(alertMessage)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WorkCommandActivity.this);
                    builder.setMessage(alertMessage).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            _put2server(true, weight, cnt);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }
            }
            _put2server(isFinished, weight, cnt);

        }
    }

    private boolean _checkWeightAndCntValue(int weight, int cnt) {
        if (weight == 0) {
            Toast.makeText(WorkCommandActivity.this, R.string.invalid_weight_data, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mWorkCommand.measured_by_weight() && cnt == 0) {
            Toast.makeText(WorkCommandActivity.this, R.string.invalid_cnt_data, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mWorkCommand.getOrgCnt() * Utils.getMaxTimes(WorkCommandActivity.this) <= cnt) {
            Toast.makeText(WorkCommandActivity.this, R.string.too_large_data, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void _initView() {
        mWorkCommand = getIntent().getParcelableExtra("work_command");
        _setIdTextView();
        _setPic();
    }

    private void _put2server(final boolean isFinished, final int weight, final int cnt) {
        XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(WorkCommandActivity.this);
        builder.msg(getString(R.string.add_work_command_weight));
        builder.run(new XProgressableRunnable.XRunnable() {
            @Override
            public Void run() throws Exception {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("weight", String.valueOf(weight));
                params.put("is_finished", isFinished ? "1" : "0");
                if (!mWorkCommand.measured_by_weight()) {
                    params.put("quantity", String.valueOf(cnt));
                }
                MyApp.getWebServieHandler().updateWorkCommand(mWorkCommand.getId(), Constants.ACT_ADD_WEIGHT, params);
                return null;
            }
        });
        builder.okMsg(getString(R.string.add_work_command_weight_success));
        builder.create().start();
    }

    private void _setDepartmentLeaderMenu(Menu menu) {
        if (mWorkCommand.getStatus() != Constants.STATUS_LOCKED) {
            getMenuInflater().inflate(R.menu.department_leader_dispatch, menu);
        } else {
            getMenuInflater().inflate(R.menu.department_leader_locked, menu);
        }
    }

    private void _setIdTextView() {
        TextView idTextView = (TextView) findViewById(R.id.work_command_id);
        idTextView.setText(String.valueOf(mWorkCommand.getId()));
    }

    private void _setPic() {
        String url = mWorkCommand.getPicPath();
        getIntent().putExtra("picUrl", url);
        if (!Utils.isEmptyString(url)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ImageFragment imageFragment = new ImageFragment();
            ft.add(R.id.image_row, imageFragment, "IMAGE");
            ft.commit();
        }
    }

    private void _setTeamLeaderMenu(Menu menu) {
        if (mWorkCommand.getStatus() != Constants.STATUS_LOCKED) {
            getMenuInflater().inflate(R.menu.team_leader_work_command_menu, menu);
        }
    }
}
