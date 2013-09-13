package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abc549825@163.com
 * 2013-09-12
 */
public class DepartmentLeaderActivity extends FragmentActivity implements ActionBar.TabListener, PullToRefresh {
    ViewPager mViewPager;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private FragmentPagerAdapter mAdapter;

    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_command_list_main);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mAdapter = new DepartmentLeaderAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mAdapter.getPageTitle(i)).setTabListener(this));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DepartmentLeaderActivity.this);
        builder.setMessage("您确认要登出?");
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.clearUserToken(DepartmentLeaderActivity.this);
                finish();
                Intent intent = new Intent(DepartmentLeaderActivity.this, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.create().show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public class DepartmentLeaderAdapter extends FragmentPagerAdapter {

        private int[] statuses = new int[]{Constants.STATUS_ASSIGNING, Constants.STATUS_LOCKED};

        public DepartmentLeaderAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return statuses.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format("状态 %s", WorkCommand.getStatusString(statuses[position]));
        }

        @Override
        public Fragment getItem(int position) {
            return DepartmentListWorkCommandListFragment.newInstance(statuses[position]);
        }
    }
}

class DepartmentListWorkCommandListFragment extends WorkCommandListFragment {

    @Override
    protected String[] getDrawerTitles() {
        return new String[]{"状态123","状态234"};
    }

    @Override
    protected void loadWorkCommandList() {
        new GetWorkCommandListTask(MyApp.getCurrentUser().getDepartmentIdList(), getSymbol(), this).execute();
    }

    public static DepartmentListWorkCommandListFragment newInstance(int symbol) {
        DepartmentListWorkCommandListFragment mFragment = new DepartmentListWorkCommandListFragment();
        Bundle args = new Bundle();
        args.putInt(WorkCommandListFragment.ARG_SECTION_NUMBER, symbol);
        mFragment.setArguments(args);
        return mFragment;
    }

    class GetWorkCommandListTask extends AbstractGetWorkCommandList {
        private int[] departmentIds;
        private int status;

        public GetWorkCommandListTask(int[] departmentIds, int status, WorkCommandListFragment listFragment) {
            super(listFragment);
            this.departmentIds = departmentIds;
            this.status = status;
        }

        @Override
        protected List<WorkCommand> getWorkCommandList() throws IOException, JSONException, BadRequest {
            List<WorkCommand> workCommandList = new ArrayList<WorkCommand>();
            for (int departmentId : departmentIds) {
                workCommandList.addAll(MyApp.getWebServieHandler().getWorkCommandListByDepartmentId(departmentId, status));
            }
            return workCommandList;
        }
    }
}
