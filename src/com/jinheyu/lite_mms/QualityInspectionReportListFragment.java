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

class QualityInspectionReportListFragment extends ListFragment implements UpdateWorkCommand {

    private View rootView;
    private View mask;
    private View main;
    private View error;
    private View noItems;
    private boolean loading;
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
        for (QualityInspectionReport qualityInspectionReport : workCommand.getQualityInspectionReportList()) {
            MyApp.addQualityInspectionReport(qualityInspectionReport);
        }
        if (MyApp.getQualityInspectionReports().isEmpty()) {
            noItems.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
        } else {
            setListAdapter(new MyAdapter());
            main.setVisibility(View.VISIBLE);
            noItems.setVisibility(View.GONE);
            setTextViewQualityInspected();
            setTextViewProcessed();
        }
        loading = false;
        modified = false;
    }

    private void setTextViewProcessed() {
        textViewWorkCommandProcessed.setText(workCommand.getProcessedWeight() + "公斤");
    }

    private void setTextViewQualityInspected() {
        int qualityInspectedCnt = 0;
        int qualityInspectedweight = 0;
        for (QualityInspectionReport qir : MyApp.getQualityInspectionReports()) {
            qualityInspectedCnt += qir.getQuantity();
            qualityInspectedweight += qir.getWeight();
        }
        String qualityInspected = "";
        if (workCommand.getOrderType() == Order.EXTRA_ORDER_TYPE) {
            qualityInspected += qualityInspectedCnt + "件";
        }
        qualityInspected += qualityInspectedweight + "公斤";
        textViewQualityInspected.setText(qualityInspected);
    }

    @Override
    public void beforeUpdateWorkCommand() {
        mask();
        MyApp.getQualityInspectionReports().clear();
        this.loading = true;
    }

    private void mask() {
        if (mask != null && error != null && main != null && noItems != null) {
            mask.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
            error.setVisibility(View.GONE);
            noItems.setVisibility(View.GONE);
        }
    }

    public boolean loading() {
        return loading;
    }

    public boolean isModified() {
        return modified;
    }

    public void resetContent() {
        MyApp.getQualityInspectionReports().clear();
        for (QualityInspectionReport qualityInspectionReport: workCommand.getQualityInspectionReportList()) {
            MyApp.addQualityInspectionReport(qualityInspectionReport);
        }
        ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
        setTextViewQualityInspected();
        modified = false;
    }

    public void setModified(boolean b) {
        modified = b;
    }


    private class MyAdapter extends BaseAdapter {

        public MyAdapter() {
        }

        @Override
        public int getCount() {
            return MyApp.getQualityInspectionReports().size();
        }

        @Override
        public Object getItem(int position) {
            return MyApp.getQualityInspectionReports().get(position);
        }

        @Override
        public long getItemId(int position) {
            return MyApp.getQualityInspectionReports().get(position).getId();
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
                            MyApp.getQualityInspectionReports().remove(position);
                            modified = true;
                            notifyDataSetChanged();
                            setTextViewProcessed();
                            setTextViewQualityInspected();
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

}
