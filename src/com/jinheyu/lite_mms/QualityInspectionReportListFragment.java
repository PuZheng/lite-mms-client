package com.jinheyu.lite_mms;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.util.List;

class QualityInspectionReportListFragment extends ListFragment implements UpdateWorkCommand {

    private View rootView;
    private View mask;
    private View main;
    private View error;
    private View noItems;
    private boolean loading;
    private List<QualityInspectionReport> qualityInspectionReports;
    private TextView textViewWorkCommandProcessed;
    private TextView textViewQualityInspected;

    public QualityInspectionReportListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_quality_inspection_report_list, container, false);
        mask = rootView.findViewById(R.id.linearLayoutMask);
        main = rootView.findViewById(R.id.linearLayoutMain);
        error = rootView.findViewById(R.id.linearyLayoutError);
        noItems = rootView.findViewById(R.id.scrollViewNoItems);

        textViewWorkCommandProcessed = (TextView) rootView.findViewById(R.id.textViewWorkCommandProcessedWeight);
        textViewQualityInspected = (TextView) rootView.findViewById(R.id.textViewQualityInspectedWeight);

        return rootView;
    }

    @Override
    public void updateWorkCommandFailed(Exception ex) {
        mask.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateWorkCommand(WorkCommand workCommand) {
        mask.setVisibility(View.GONE);
        int orderType = workCommand.getOrderType();
        qualityInspectionReports = workCommand.getQualityInspectionReportList();
        if (qualityInspectionReports.isEmpty()) {
            noItems.setVisibility(View.VISIBLE);
        } else {
            setListAdapter(new MyAdapter(orderType, qualityInspectionReports));
            main.setVisibility(View.VISIBLE);
            int qualityInspectedCnt = 0;
            int qualityInspectedweight = 0;
            for (QualityInspectionReport qir: workCommand.getQualityInspectionReportList()) {
                qualityInspectedCnt += qir.getQuantity();
                qualityInspectedweight += qir.getWeight();
            }
            String processed = "";
            String qualityInspected = "";
            if (workCommand.getOrderType() == Order.EXTRA_ORDER_TYPE) {
                processed += workCommand.getProcessedCnt() + "件";
                qualityInspected += qualityInspectedCnt + "件";
            }
            processed += workCommand.getProcessedWeight() + "公斤";
            qualityInspected += qualityInspectedweight + "公斤";
            textViewWorkCommandProcessed.setText(processed);
            textViewQualityInspected.setText(qualityInspected);
        }
        loading = false;
    }

    @Override
    public void beforeUpdateWorkCommand() {
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

    public boolean modified() {
        return false;
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
            QualityInspectionReport qualityInspectionReport = (QualityInspectionReport) getItem(position);
            new GetImageTask(viewHolder.imageButton, qualityInspectionReport.getPicUrl()).execute();
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
