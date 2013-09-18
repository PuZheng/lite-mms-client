package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;
import android.widget.ArrayAdapter;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.Team;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class TeamLeaderActivity extends WorkCommandListActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_or_off_duty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new LogoutDialog(this).show();
                break;
            case R.id.action_off_duty:
                new LogoutDialog(this, true).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected ArrayAdapter<Team> getArrayAdapter(int resource) {
        return new ArrayAdapter<Team>(this, resource, MyApp.getCurrentUser().getTeamList());
    }

    @Override
    protected FragmentPagerAdapter getFragmentPagerAdapter(int position) {
        return new TeamLeaderAdapter(getSupportFragmentManager(), MyApp.getCurrentUser().getTeamIds()[position]);
    }
}

class TeamLeaderAdapter extends FragmentPagerAdapter {

    private int[] statuses = new int[]{Constants.STATUS_ENDING, Constants.STATUS_LOCKED};
    private int teamId;

    public TeamLeaderAdapter(FragmentManager fm, int teamId) {
        super(fm);
        this.teamId = teamId;
    }

    @Override
    public int getCount() {
        return statuses.length;
    }

    @Override
    public Fragment getItem(int i) {
        return TeamLeaderWorkCommandListFragment.newInstance(teamId, statuses[i]);
    }

    @Override
    public long getItemId(int position) {
        return teamId * getCount() + position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.format("状态 %s", WorkCommand.getStatusString(statuses[position]));
    }
}

class TeamLeaderWorkCommandListFragment extends WorkCommandListFragment {
    public static TeamLeaderWorkCommandListFragment newInstance(int teamId, int status) {
        TeamLeaderWorkCommandListFragment fragment = new TeamLeaderWorkCommandListFragment();
        Bundle args = new Bundle();
        args.putIntArray(WorkCommandListFragment.ARG_SECTION_NUMBER, new int[]{teamId, status});
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                TeamLeaderMenuItemWrapper wrapper = new TeamLeaderMenuItemWrapper(getActivity(), mode);
                switch (item.getItemId()) {
                    case R.id.carry_forward:
                        wrapper.carryForward(getCheckedWorkCommandIds());
                        return true;
                    case R.id.quick_carryForward:
                        wrapper.carryForwardQuickly(getCheckedWorkCommandIds());
                        return true;
                    case R.id.end_work_command:
                        wrapper.endWorkCommand(getCheckedWorkCommandIds());
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater mInflater = mode.getMenuInflater();
                mInflater.inflate(R.menu.team_leader_work_command_list_menu, menu);
                mode.setTitle(getString(R.string.please_select));
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                clearAllCheckedItems();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        };
    }

    @Override
    protected void loadWorkCommandList() {
        int[] symbols = getSymbols();
        new GetWorkCommandListTask(symbols[0], symbols[1], this).execute();
    }

    class GetWorkCommandListTask extends AbstractGetWorkCommandListTask {
        private int status;
        private int teamId;

        GetWorkCommandListTask(int teamId, int status, WorkCommandListFragment fragment) {
            super(fragment);
            this.teamId = teamId;
            this.status = status;
        }

        @Override
        protected List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest {
            return MyApp.getWebServieHandler().getWorkCommandListByTeamId(teamId, status);
        }
    }
}
