package com.jinheyu.lite_mms;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

interface UpdateQualityInspectionReportList {

    void updateQualityInspectionReportListFailed(Exception ex);

    void updateQualityInspectionReportList(List<QualityInspectionReport> qualityInspectionReports);

    void beforeUpdateQualityInspectionReportList();
}

class QualityInspectionReportListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener, UpdateQualityInspectionReportList {

    private final int workCommandId;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private View rootView;
    private View mask;
    private View main;
    private View error;
    private View noItems;

    public QualityInspectionReportListFragment(int workCommandId) {
        this.workCommandId = workCommandId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_quality_inspection_report_list, container, false);
        mask = rootView.findViewById(R.id.linearLayoutMask);
        main = rootView.findViewById(R.id.linearLayoutMain);
        error = rootView.findViewById(R.id.linearyLayoutError);
        noItems = rootView.findViewById(R.id.linearyLayoutNoItems);

        mPullToRefreshAttacher = ((GetPullToRefreshAttacher) getActivity()).getPullToRefreshAttacher();
        final PullToRefreshLayout ptrLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);

        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        TextView noDataView = (TextView) rootView.findViewById(android.R.id.empty);
        noDataView.setMovementMethod(new ScrollingMovementMethod());

        listView.setEmptyView(noDataView);

        new GetInspectionReportListAsyncTask(this).execute(workCommandId);
        return rootView;
    }

    @Override
    public void onRefreshStarted(View view) {
        new GetInspectionReportListAsyncTask(this).execute(workCommandId);
    }

    @Override
    public void updateQualityInspectionReportListFailed(Exception ex) {
        mask.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        mPullToRefreshAttacher.setRefreshComplete();
    }

    @Override
    public void updateQualityInspectionReportList(List<QualityInspectionReport> qualityInspectionReports) {
        mask.setVisibility(View.GONE);
        if (qualityInspectionReports.isEmpty()) {
            noItems.setVisibility(View.VISIBLE);
        } else {
            setListAdapter(new MyAdapter(qualityInspectionReports));
            main.setVisibility(View.VISIBLE);
        }
        mPullToRefreshAttacher.setRefreshComplete();
    }

    @Override
    public void beforeUpdateQualityInspectionReportList() {
        mask();
    }

    private void mask() {
        mask.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        noItems.setVisibility(View.GONE);
    }

    private class GetInspectionReportListAsyncTask extends
            AsyncTask<Integer, Void, List<QualityInspectionReport>> {
        private final UpdateQualityInspectionReportList updateQualityInspectionReportList;
        Exception ex;

        public GetInspectionReportListAsyncTask(UpdateQualityInspectionReportList updateQualityInspectionReportList) {
            this.updateQualityInspectionReportList = updateQualityInspectionReportList;
        }

        @Override
        protected List<QualityInspectionReport> doInBackground(Integer... params) {
            try {
                this.updateQualityInspectionReportList.beforeUpdateQualityInspectionReportList();
                return MyApp.getWebServieHandler().getQualityInspectionReportList(workCommandId);
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

        @Override
        protected void onPostExecute(List<QualityInspectionReport> qualityInspectionReports) {

            if (ex != null) {
                this.updateQualityInspectionReportList.updateQualityInspectionReportListFailed(ex);
            } else {
                this.updateQualityInspectionReportList.updateQualityInspectionReportList(qualityInspectionReports);
            }
        }

    }


    private class MyAdapter extends BaseAdapter {

        private final List<QualityInspectionReport> qualityInspectionReports;

        public MyAdapter(List<QualityInspectionReport> qualityInspectionReports) {
            this.qualityInspectionReports = qualityInspectionReports;
        }

        @Override
        public int getCount() {
            return qualityInspectionReports.size();
        }

        @Override
        public Object getItem(int position) {
            return qualityInspectionReports.get(position);
        }

        @Override
        public long getItemId(int position) {
            return qualityInspectionReports.get(position).getId();
        }

        class ViewHolder {
            ImageView imageView;
            TextView textViewResult;
            TextView textViewWeight;

            public ViewHolder(ImageView imageView, TextView textViewResult, TextView testViewWeight) {
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.quality_inspection_report_list_item, null);
                viewHolder = new ViewHolder((ImageView) convertView.findViewById(R.id.imageView),
                        (TextView) convertView.findViewById(R.id.textViewResult),
                        (TextView) convertView.findViewById(R.id.textViewWeight));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            QualityInspectionReport qualityInspectionReport = (QualityInspectionReport) getItem(position);
            //viewHolder.imageView;
            viewHolder.textViewResult.setText(qualityInspectionReport.getLiterableResult());
            String weight = qualityInspectionReport.getWeight() + "公斤";
            viewHolder.textViewWeight.setText(weight);
            return convertView;
        }
    }
}
