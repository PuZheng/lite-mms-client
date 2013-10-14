package com.jinheyu.lite_mms;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    private boolean modified;
    private WorkCommand workCommand;

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
        main.setVisibility(View.GONE);
        noItems.setVisibility(View.GONE);
    }

    @Override
    public void updateWorkCommand(WorkCommand workCommand) {
        mask.setVisibility(View.GONE);
        this.workCommand = workCommand;
        int orderType = workCommand.getOrderType();
        qualityInspectionReports.addAll(workCommand.getQualityInspectionReportList());
        if (qualityInspectionReports.isEmpty()) {
            noItems.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
        } else {
            setListAdapter(new MyAdapter());
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
        qualityInspectionReports.clear();
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

    public boolean isModified() {
        return modified;
    }

    public int getQualityInspectedWeight() {
        int ret = 0;
        for (QualityInspectionReport qualityInspectionReport: qualityInspectionReports) {
            ret += qualityInspectionReport.getWeight();
        }
        return ret;
    }

    private class MyAdapter extends BaseAdapter {

        public MyAdapter() {
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
            ImageButton imageButtonDiscard;

            public ViewHolder(ImageButton imageButton, TextView textViewResult, TextView textViewWeight,
                              ImageButton imageButtonDiscard) {
                this.imageButton = imageButton;
                this.textViewResult = textViewResult;
                this.textViewWeight = textViewWeight;
                this.imageButtonDiscard = imageButtonDiscard;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.quality_inspection_report_list_item, null);
                viewHolder = new ViewHolder((ImageButton) convertView.findViewById(R.id.imageButton),
                        (TextView) convertView.findViewById(R.id.textViewResult),
                        (TextView) convertView.findViewById(R.id.textViewWeight),
                        (ImageButton) convertView.findViewById(R.id.imageButtonDiscard));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final QualityInspectionReport qualityInspectionReport = (QualityInspectionReport) getItem(position);
            new GetImageTask(viewHolder.imageButton, qualityInspectionReport.getPicUrl()).execute();
            viewHolder.textViewResult.setText(qualityInspectionReport.getLiterableResult());
            String weight = "";
            if (workCommand.getOrderType() == Order.EXTRA_ORDER_TYPE) {
                weight = qualityInspectionReport.getQuantity() + "件";
            }
            viewHolder.imageButtonDiscard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("您确认要删除这一条质检报告?");
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            qualityInspectionReports.remove(position);
                            modified = true;
                            notifyDataSetChanged();
                        }
                    });
                    builder.show();
                }
            });
            weight += qualityInspectionReport.getWeight() + "公斤";
            viewHolder.textViewWeight.setText(weight);
            return convertView;
        }
    }

    public void addQualityInspectionReport(QualityInspectionReport qualityInspectionReport) {
        qualityInspectionReports.add(qualityInspectionReport);
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        modified = true;
    }
}
