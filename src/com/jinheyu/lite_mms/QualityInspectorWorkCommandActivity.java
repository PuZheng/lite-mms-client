package com.jinheyu.lite_mms;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;


public class QualityInspectorWorkCommandActivity extends FragmentActivity implements GetPullToRefreshAttacher, ActionBar.TabListener, UpdateWorkCommand {

    private PullToRefreshAttacher mPullToRefreshAttacher;
    private ViewPager mViewPager;
    private int workCommandId;

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

        workCommandId = getIntent().getIntExtra("workCommandId", 0);

        /**
         * 之所以要将获取工单信息和质检报告列表的操作统一放在Activity中进行，原因是这两个Fragment的信息要时刻保证一致，
         * 不能一个刷新了一个还没有刷新
         */
        new GetWorkCommandAsyncTask(this).execute(workCommandId);
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
                QualityInspectionReportListFragment qualityInspectionReportListFragment;
                qualityInspectionReportListFragment = (QualityInspectionReportListFragment) ((MyFragmentPagerAdapter)mViewPager.getAdapter()).getRegisteredFragment(0);
                if (!qualityInspectionReportListFragment.loading()) {
                    List<QualityInspectionReport> qualityInspectionReports = qualityInspectionReportListFragment.getQualityInspectionReports();
                    if (qualityInspectionReports.isEmpty()) {
                        Toast.makeText(QualityInspectorWorkCommandActivity.this, "请生成质检报告后再提交", Toast.LENGTH_SHORT).show();
                    } else {
                        showNoticeDialog(qualityInspectionReports, workCommandId, qualityInspectionReportListFragment.getWorkCommandProcessedWeight(),
                                qualityInspectionReportListFragment.getOrderType());
                    }
                }
                break;
            case R.id.action_new_quality_inspection_report:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showNoticeDialog(List<QualityInspectionReport> qualityInspectionReports, int workCommandId, int workCommandProcessedWeight,
                                  int orderType) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("NOTICE_FRAGMENT");
        if (prev != null) {
            ft.remove(prev);
        }
        DialogFragment dialog = new NoticeDialogFragment(qualityInspectionReports, workCommandId, workCommandProcessedWeight, orderType);
        dialog.show(ft, "NOTICE_FRAGMENT");
    }

    @Override
    public void updateWorkCommand(WorkCommand workCommand) {

    }

    @Override
    public void updateWorkCommandFailed(Exception ex) {

    }

    @Override
    public void beforeUpdateWorkCommand() {

    }


    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        private final int workCommandId;
        List<Fragment> registeredFragments;

        public MyFragmentPagerAdapter(int workCommandId, FragmentManager fm) {
            super(fm);
            registeredFragments = new ArrayList<Fragment>();
            this.workCommandId = workCommandId;
            registeredFragments.add(new QualityInspectionReportListFragment(workCommandId));
            registeredFragments.add(new WorkCommandFragment(workCommandId));
        }

        @Override
        public Fragment getItem(int i) {
            return registeredFragments.get(i);
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

        Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    private class NoticeDialogFragment extends DialogFragment {
        private final List<QualityInspectionReport> qualityInspectionReports;
        private final int workCommandProcessedWeight;
        private final int orderType;
        private final int workCommandId;

        public NoticeDialogFragment(List<QualityInspectionReport> qualityInspectionReports, int workCommandId,
                                    int workCommandProcessedWeight, int orderType) {
            this.workCommandId = workCommandId;
            this.qualityInspectionReports = qualityInspectionReports;
            this.workCommandProcessedWeight = workCommandProcessedWeight;
            this.orderType = orderType;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(String.format("确认提交工单%d质检结果?", workCommandId));
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_quality_inspect, null);
            TableLayout tableLayoutResults = view.findViewById(R.id.tableLayoutResults);

            builder.setView(view);
            int totalWeight = 0;
            for (QualityInspectionReport qir: qualityInspectionReports) {
                TableRow tableRow = new TableRow(getActivity());
                TextView textViewResult = new TextView(getActivity());
                textViewResult.setText(qir.getLiterableResult());
                textViewResult.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
                TextView textViewWeight = new TextView(getActivity());
                String weight = "";
                if (orderType == Order.EXTRA_ORDER_TYPE) {
                    weight = qir.getQuantity() + "件; ";
                }
                weight += qir.getWeight() + "公斤";
                textViewWeight.setText(weight);
                tableRow.addView(textViewResult, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tableRow.addView(textViewWeight, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tableLayoutResults.addView(tableRow);
                totalWeight += qir.getWeight();
            }

            builder.setNegativeButton(android.R.string.cancel, null);
            final int finalTotalWeight = totalWeight;
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (finalTotalWeight < workCommandProcessedWeight) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.warning);
                        builder.setMessage(String.format("工单重量为%d, 你提交的质检重量是%d, 是否仍然要提交质检结果?",
                                workCommandProcessedWeight, finalTotalWeight)));
                        builder.setNegativeButton(R.string.cancel, null);
                        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                submitQualityInspectionReport();
                            }
                        });
                    } else {
                        submitQualityInspectionReport();
                    }
                }
            });
            return builder.create();
        }

        private void submitQualityInspectionReport() {

            XProgressableRunnable.Builder<Void> builder = new XProgressableRunnable.Builder<Void>(getActivity());
            builder.msg("正在提交质检结果");
            builder.run(new XProgressableRunnable.XRunnable<Void>() {
                @Override
                public Void run() throws Exception {
                    MyApp.getWebServieHandler().submitQualityInspection(workCommandId, qualityInspectionReports);
                    return null;
                }
            });
            builder.okMsg("提交成功");
            builder.after(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            });

        }

    }
}
