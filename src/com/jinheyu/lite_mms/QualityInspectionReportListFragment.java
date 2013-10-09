package com.jinheyu.lite_mms;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

interface UpdateQualityInspectionReportList {

    void updateQualityInspectionReportListFailed(Exception ex);

    void updateQualityInspectionReportList(Pair<Integer, List<QualityInspectionReport>> qualityInspectionReports);

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
    private boolean loading;
    private List<QualityInspectionReport> qualityInspectionReports;

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
        noItems = rootView.findViewById(R.id.scrollViewNoItems);

        mPullToRefreshAttacher = ((GetPullToRefreshAttacher) getActivity()).getPullToRefreshAttacher();
        final PullToRefreshLayout ptrLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);

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
    public void updateQualityInspectionReportList(Pair<Integer, List<QualityInspectionReport>> pair) {
        mask.setVisibility(View.GONE);
        int orderType = pair.first;
        qualityInspectionReports = pair.second;
        if (qualityInspectionReports.isEmpty()) {
            noItems.setVisibility(View.VISIBLE);
        } else {
            setListAdapter(new MyAdapter(orderType, qualityInspectionReports));
            main.setVisibility(View.VISIBLE);
        }
        mPullToRefreshAttacher.setRefreshComplete();
        loading = false;
    }

    @Override
    public void beforeUpdateQualityInspectionReportList() {
        mask();
        this.loading = true;
    }

    private void mask() {
        mask.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        noItems.setVisibility(View.GONE);
    }

    public boolean loading() {
        return loading;
    }

    public List<QualityInspectionReport> getQualityInspectionReports() {
        return qualityInspectionReports;
    }

    private class GetInspectionReportListAsyncTask extends
            AsyncTask<Integer, Void, Pair<Integer, List<QualityInspectionReport>>> {
        private final UpdateQualityInspectionReportList updateQualityInspectionReportList;
        Exception ex;

        public GetInspectionReportListAsyncTask(UpdateQualityInspectionReportList updateQualityInspectionReportList) {
            this.updateQualityInspectionReportList = updateQualityInspectionReportList;
        }

        @Override
        protected void onPreExecute() {
            this.updateQualityInspectionReportList.beforeUpdateQualityInspectionReportList();
        }

        @Override
        protected Pair<Integer, List<QualityInspectionReport>> doInBackground(Integer... params) {
            try {
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
        protected void onPostExecute(Pair<Integer, List<QualityInspectionReport>> pair) {

            if (ex != null) {
                this.updateQualityInspectionReportList.updateQualityInspectionReportListFailed(ex);
            } else {
                this.updateQualityInspectionReportList.updateQualityInspectionReportList(pair);
            }
        }

    }


    private class MyAdapter extends BaseAdapter {

        private final List<QualityInspectionReport> qualityInspectionReports;
        private final int orderType;

        public MyAdapter(int orderType, List<QualityInspectionReport> qualityInspectionReports) {
            this.orderType = orderType;
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
            ImageButton imageButton;
            TextView textViewResult;
            TextView textViewWeight;

            public ViewHolder(ImageButton imageButton, TextView textViewResult, TextView testViewWeight) {
                this.imageButton = imageButton;
                this.textViewResult = textViewResult;
                this.textViewWeight = testViewWeight;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.quality_inspection_report_list_item, null);
                viewHolder = new ViewHolder((ImageButton) convertView.findViewById(R.id.imageButton),
                        (TextView) convertView.findViewById(R.id.textViewResult),
                        (TextView) convertView.findViewById(R.id.textViewWeight));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            // TODO image view
            QualityInspectionReport qualityInspectionReport = (QualityInspectionReport) getItem(position);
            viewHolder.textViewResult.setText(qualityInspectionReport.getLiterableResult());
            String weight = "";
            if (this.orderType == Order.EXTRA_ORDER_TYPE) {
                weight = qualityInspectionReport.getQuantity() + "件";
            }
            weight += qualityInspectionReport.getWeight() + "公斤";
            viewHolder.textViewWeight.setText(weight);
            return convertView;
        }
    }
}
