package com.jinheyu.lite_mms;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;


public abstract class WorkCommandListActivity extends FragmentActivity {

    protected ActionBar.TabListener mTabListener = new ActionBar.TabListener() {
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

    };
    protected ViewPager mViewPager;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private boolean doubleBackToExitPressedOnce;

    public PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.exit_press_back_twice_message, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_command_list_main);

        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setCustomView(getSpinner());
        actionBar.setDisplayShowCustomEnabled(true);
    }

    protected abstract ArrayAdapter getArrayAdapter(int resource);

    protected abstract FragmentPagerAdapter getFragmentPagerAdapter(int position);

    private Spinner getSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter adapter = getArrayAdapter(android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final ActionBar actionBar = getActionBar();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FragmentPagerAdapter fragmentPagerAdapter = getFragmentPagerAdapter(position);
                mViewPager.setAdapter(fragmentPagerAdapter);
                mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between different app sections, select the corresponding tab.
                        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                        // Tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
                actionBar.removeAllTabs();
                for (int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
                    // Create a tab with text corresponding to the page title defined by the adapter.
                    // Also specify this Activity object, which implements the TabListener interface, as the
                    // listener for when this tab is selected.
                    actionBar.addTab(
                            actionBar.newTab()
                                    .setText(fragmentPagerAdapter.getPageTitle(i))
                                    .setTabListener(mTabListener));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return spinner;
    }
}
