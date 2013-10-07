package com.jinheyu.lite_mms;

import android.app.*;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.*;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;


public class QualityInspectorWorkCommandActivity extends FragmentActivity implements GetPullToRefreshAttacher, ActionBar.TabListener {

    private PullToRefreshAttacher mPullToRefreshAttacher;
    private ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_inspector_work_command);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        // make action bar hide title
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        // add pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        FragmentPagerAdapter fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // add tabs
        actionBar.removeAllTabs();
        for (int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(i == 0? getString(R.string.quality_inspection_report_list) :
                                    getString(R.string.work_command_detail))
                            .setTabListener(this));
        }
    }

    @Override
    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quality_inspector_work_command_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_complete_quality_inspection:
                break;
            case R.id.action_new_quality_inspection_report:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            int workCommandId = getIntent().getIntExtra("workCommandId", 0);
            if (i == 0) {
                return new QualityInspectionReportListFragment(workCommandId);
            } else {
                return new WorkCommandFragment(workCommandId);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0? getString(R.string.quality_inspection_report_list):
                    getString(R.string.work_command_detail);
        }
    }
}
