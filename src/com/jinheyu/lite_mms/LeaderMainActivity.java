package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;
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
public class LeaderMainActivity extends FragmentActivity implements ActionBar.TabListener {
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;
    private PullToRefreshAttacher mPullToRefreshAttacher;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_main);
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
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
        AlertDialog.Builder builder = new AlertDialog.Builder(LeaderMainActivity.this);
        builder.setMessage("您确认要登出?");
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.clearUserToken(LeaderMainActivity.this);
                finish();
                Intent intent = new Intent(LeaderMainActivity.this, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.create().show();
        return super.onOptionsItemSelected(item);
    }

    PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
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

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            // The other sections of the app are dummy placeholders.
            Fragment mFragment = new WorkCommandListFragment(getCurrentTeam(i).getId());
            Bundle args = new Bundle();
            args.putInt(WorkCommandListFragment.ARG_SECTION_NUMBER, i);
            mFragment.setArguments(args);
            return mFragment;
        }


        @Override
        public int getCount() {
            return MyApp.getCurrentUser().getTeamIdList().length;
        }

        private Team getCurrentTeam(int position) {
            return MyApp.getCurrentUser().getTeamByIndex(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format("班组%s", getCurrentTeam(position).getName());
        }
    }

    public static class WorkCommandListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private TextView noDataView;
        private ProgressBar mProgressBar;
        private int teamId;
        private AsyncTask<Void, Void, List<WorkCommand>> task;
        private int mCategoryId;
        private PullToRefreshAttacher mPullToRefreshAttacher;

        public WorkCommandListFragment(int teamId) {
            this.teamId = teamId;
        }

        public void loadWorkCommandList() {
            task = new GetWorkCommandListTask(teamId);
            task.execute();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_work_command_list, container, false);
            ListView listView = (ListView) rootView.findViewById(android.R.id.list);
            mPullToRefreshAttacher = ((LeaderMainActivity) getActivity()).getPullToRefreshAttacher();
            mPullToRefreshAttacher.addRefreshableView(listView, this);
            ScrollView mScrollView = (ScrollView) rootView.findViewById(R.id.scroll_view);
            mPullToRefreshAttacher.addRefreshableView(mScrollView, this);
            noDataView = (TextView) rootView.findViewById(android.R.id.empty);
            mCategoryId = getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;
            mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            loadWorkCommandList();
            return rootView;
        }

        @Override
        public void onRefreshStarted(View view) {
            loadWorkCommandList();
        }


        public class ViewHolder {
            public TextView idTextView;

            public ViewHolder(TextView idTextView) {
                this.idTextView = idTextView;
            }
        }

        class WorkCommandListAdapter extends BaseAdapter {
            private final LayoutInflater mInflater;
            private List<WorkCommand> workCommandList;

            public WorkCommandListAdapter(Context context, List<WorkCommand> workCommandList) {
                this.workCommandList = workCommandList;
                this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            /**
             * How many items are in the data set represented by this Adapter.
             *
             * @return Count of items.
             */
            @Override
            public int getCount() {
                return workCommandList.size();
            }

            /**
             * Get the data item associated with the specified position in the data set.
             *
             * @param position Position of the item whose data we want within the adapter's
             *                 data set.
             * @return The data at the specified position.
             */
            @Override
            public Object getItem(int position) {
                return workCommandList.get(position);
            }

            /**
             * Get the row id associated with the specified position in the list.
             *
             * @param position The position of the item within the adapter's data set whose row id we want.
             * @return The id of the item at the specified position.
             */
            @Override
            public long getItemId(int position) {
                return workCommandList.get(position).getId();
            }

            /**
             * Get a View that displays the data at the specified position in the data set. You can either
             * create a View manually or inflate it from an XML layout file. When the View is inflated, the
             * parent View (GridView, ListView...) will apply default layout parameters unless you use
             * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
             * to specify a root view and to prevent attachment to the root.
             *
             * @param position    The position of the item within the adapter's data set of the item whose view
             *                    we want.
             * @param convertView The old view to reuse, if possible. Note: You should check that this view
             *                    is non-null and of an appropriate type before using. If it is not possible to convert
             *                    this view to display the correct data, this method can create a new view.
             *                    Heterogeneous lists can specify their number of view types, so that this View is
             *                    always of the right type (see {@link #getViewTypeCount()} and
             *                    {@link #getItemViewType(int)}).
             * @param parent      The parent that this view will eventually be attached to
             * @return A View corresponding to the data at the specified position.
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final WorkCommand workCommand = workCommandList.get(position);
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.fragment_work_command, null);
                    viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.idTextView));
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                viewHolder.idTextView.setText(String.valueOf(workCommand.getId()));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return convertView;
            }
        }

        class GetWorkCommandListTask extends AsyncTask<Void, Void, List<WorkCommand>> {
            private final int teamId;
            Exception ex;

            public GetWorkCommandListTask(int teamdId) {
                this.teamId = teamdId;
            }

            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected List<WorkCommand> doInBackground(Void... params) {
                try {
                    int[] i = {1, 2};
                    return MyApp.getWebServieHandler().getWorkCommandListByTeamId(teamId, i);
                } catch (IOException e) {
                    e.printStackTrace();
                    ex = e;
                } catch (JSONException e) {
                    e.printStackTrace();
                    ex = e;
                } catch (BadRequest badRequest) {
                    badRequest.printStackTrace();
                    ex = badRequest;
                }
                return null;
            }

            /**
             * <p>Runs on the UI thread after {@link #doInBackground}. The
             * specified result is the value returned by {@link #doInBackground}.</p>
             * <p/>
             * <p>This method won't be invoked if the task was cancelled.</p>
             *
             * @param workCommandList The result of the operation computed by {@link #doInBackground}.
             * @see #onPreExecute
             * @see #doInBackground
             * @see #onCancelled(Object)
             */
            @Override
            protected void onPostExecute(List<WorkCommand> workCommandList) {
                if (ex != null) {
                    Utils.displayError(WorkCommandListFragment.this.getActivity(), ex);
                    return;
                }
                if (workCommandList.isEmpty()) {
                    noDataView.setVisibility(View.VISIBLE);
                    getListView().setVisibility(View.GONE);
                } else {
                    doUpdateView(workCommandList);
                }
                mProgressBar.setVisibility(View.GONE);
                mPullToRefreshAttacher.setRefreshComplete();
            }

            private void doUpdateView(List<WorkCommand> workCommandList) {
                noDataView.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
                setListAdapter(new WorkCommandListAdapter(WorkCommandListFragment.this.getActivity(), workCommandList));
            }
        }
    }


}