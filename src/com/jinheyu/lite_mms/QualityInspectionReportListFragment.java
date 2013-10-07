package com.jinheyu.lite_mms;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
}

class QualityInspectionReportListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener, UpdateQualityInspectionReportList {

    private final int workCommandId;
    private PullToRefreshAttacher mPullToRefreshAttacher;

    public QualityInspectionReportListFragment(int workCommandId) {
        this.workCommandId = workCommandId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quality_inspection_report_list, container, false);
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

    public void setRefreshComplete() {
        mPullToRefreshAttacher.setRefreshComplete();
    }

    @Override
    public void updateQualityInspectionReportListFailed(Exception ex) {

    }

    @Override
    public void updateQualityInspectionReportList(List<QualityInspectionReport> qualityInspectionReports) {

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
}
