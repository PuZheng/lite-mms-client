package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.*;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.Team;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yangminghua
 * Date: 13-8-23
 * Time: 上午10:11
 */
public class TeamLeaderActivity extends FragmentActivity implements ActionBar.TabListener, PullToRefresh {
    TeamLeaderAdapter mTeamLeaderAdapter;
    ViewPager mViewPager;
    private PullToRefreshAttacher mPullToRefreshAttacher;

    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_command_list_main);
        mTeamLeaderAdapter = new TeamLeaderAdapter(getSupportFragmentManager());
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTeamLeaderAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mTeamLeaderAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mTeamLeaderAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeamLeaderActivity.this);
            builder.setMessage("您确认要登出?");
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Utils.clearUserToken(TeamLeaderActivity.this);
                    finish();
                    Intent intent = new Intent(TeamLeaderActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. The previous tab's unselect and this tab's select will be
     *            executed in a single transaction. This FragmentTransaction does not support
     *            being added to the back stack.
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            during a tab switch. This tab's unselect and the newly selected tab's select
     *            will be executed in a single transaction. This FragmentTransaction does not
     *            support being added to the back stack.
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    /**
     * Called when a tab that is already selected is chosen again by the user.
     * Some applications may use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     * @param ft  A {@link android.app.FragmentTransaction} for queuing fragment operations to execute
     *            once this method returns. This FragmentTransaction does not support
     *            being added to the back stack.
     */
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public static class TeamLeaderAdapter extends FragmentPagerAdapter {

        private List<Team> teamList;

        public TeamLeaderAdapter(FragmentManager fm) {
            super(fm);
            teamList = MyApp.getCurrentUser().getTeamList();
        }

        @Override
        public int getCount() {
            return teamList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format("班组 %s", getCurrentTeamName(position));
        }

        private String getCurrentTeamName(int position) {
            return teamList.get(position).getName();
        }

        @Override
        public Fragment getItem(int i) {
            // The other sections of the app are dummy placeholders.
            return TeamLeaderWorkCommandListFragment.newInstance(teamList.get(i).getId());
        }
    }
}

class TeamLeaderWorkCommandListFragment extends WorkCommandListFragment {
    public static TeamLeaderWorkCommandListFragment newInstance(int teamId) {
        TeamLeaderWorkCommandListFragment fragment = new TeamLeaderWorkCommandListFragment();
        Bundle args = new Bundle();
        args.putInt(WorkCommandListFragment.ARG_SECTION_NUMBER, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void loadWorkCommandList() {
        new GetWorkCommandListTask(getSymbol(), this).execute();
    }

    class GetWorkCommandListTask extends AbstractGetWorkCommandList {
        private int teamId;

        GetWorkCommandListTask(int teamId, WorkCommandListFragment fragment) {
            super(fragment);
            this.teamId = teamId;
        }

        @Override
        protected List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest {
            return MyApp.getWebServieHandler().getWorkCommandListByTeamId(teamId, new int[]{Constants.STATUS_ENDING, Constants.STATUS_LOCKED});
        }
    }
}