package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class QualityInspectionReportListFragment extends ListFragment implements UpdateWorkCommand {
    private static final int MODIFY_QUALITY_INSPECTION_REPORT = 100;
    private static final String TAG = "QualityInspectionReportListFragment";
    private View mask;
    private View main;
    private View error;
    private boolean loading;
    private TextView textViewWorkCommandProcessed;
    private TextView textViewQualityInspected;
    private boolean modified;
    private WorkCommand workCommand;
    private TextView textViewWorkCommandProcessedRow;

    public QualityInspectionReportListFragment() {
    }

    @Override
    public void beforeUpdateWorkCommand() {
        mask();
        MyApp.getQualityInspectionReports().clear();
        this.loading = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean b) {
        modified = b;
    }

    public boolean loading() {
        return loading;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quality_inspection_report_list, container, false);
        mask = rootView.findViewById(R.id.linearLayoutMask);
        main = rootView.findViewById(R.id.linearLayoutMain);
        error = rootView.findViewById(R.id.linearyLayoutError);
        textViewWorkCommandProcessedRow = (TextView) rootView.findViewById(R.id.textViewWorkCommandProcessedWeightRow);
        textViewWorkCommandProcessed = (TextView) rootView.findViewById(R.id.textViewWorkCommandProcessedWeight);
        textViewQualityInspected = (TextView) rootView.findViewById(R.id.textViewQualityInspectedWeight);

        return rootView;
    }

    public void resetContent() {
        MyApp.getQualityInspectionReports().clear();
        for (QualityInspectionReport qualityInspectionReport : workCommand.getQualityInspectionReportList()) {
            MyApp.addQualityInspectionReport(qualityInspectionReport);
        }
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        setTextViewQualityInspected();
        modified = false;
    }

    public void setTextViewQualityInspected() {
        int qualityInspectedCnt = 0;
        int qualityInspectedWeight = 0;
        for (QualityInspectionReport qir : MyApp.getQualityInspectionReports()) {
            qualityInspectedCnt += qir.getQuantity();
            qualityInspectedWeight += qir.getWeight();
        }
        textViewQualityInspected.setText(Utils.getWeightAndQuantity(qualityInspectedWeight, qualityInspectedCnt, workCommand));
    }

    @Override
    public void updateWorkCommand(WorkCommand workCommand) {
        mask.setVisibility(View.GONE);
        this.workCommand = workCommand;
        for (QualityInspectionReport qualityInspectionReport : workCommand.getQualityInspectionReportList()) {
            MyApp.addQualityInspectionReport(qualityInspectionReport);
        }
        main.setVisibility(View.VISIBLE);
        setTextViewQualityInspected();
        setTextViewProcessed();
        if (getListAdapter() == null) {
            setListAdapter(new MyAdapter());
        } else {
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
        loading = false;
        modified = false;
    }

    @Override
    public void updateWorkCommandFailed(Exception ex) {
        mask.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        main.setVisibility(View.GONE);
    }

    private void mask() {
        if (mask != null && error != null && main != null) {
            mask.setVisibility(View.VISIBLE);
            main.setVisibility(View.GONE);
            error.setVisibility(View.GONE);
        }
    }

    private void setTextViewProcessed() {
        textViewWorkCommandProcessedRow.setText(workCommand.measuredByWeight() ? "工单重量:" : "工单重量/数量:");
        textViewWorkCommandProcessed.setText(Utils.getWeightAndQuantity(workCommand.getProcessedWeight(), workCommand.getProcessedCnt(), workCommand));
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
            // 若有对应的本地图片，读取本地对应的图片。注意，若是重新加载的质检报告列表，本地图片都是空的，所以也不存在会
            // 读取之前遗留的本地图片的问题
            if (!Utils.isEmptyString(qualityInspectionReport.getLocalPicPath())) {
                InputStream in = null;
                try {
                    in = new FileInputStream(new File(qualityInspectionReport.getLocalPicPath()));
                    if (in != null) {
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(in, 1024);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        options.inSampleSize = Constants.SMALL_SAMPLE_SIZE;
                        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options);
                        viewHolder.imageButton.setImageBitmap(bitmap);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (!Utils.isEmptyString(qualityInspectionReport.getSmallPicUrl())) {
                new GetImageTask(viewHolder.imageButton, qualityInspectionReport.getSmallPicUrl()).execute();
            } else {
                viewHolder.imageButton.setImageResource(R.drawable.content_picture);
            }
            viewHolder.textViewResult.setText(qualityInspectionReport.getLiterableResult());
            if (workCommand.getStatus() == Constants.STATUS_FINISHED) {
                viewHolder.imageButtonDiscard.setVisibility(View.GONE);
            } else {
                viewHolder.imageButtonDiscard.setVisibility(View.VISIBLE);
                viewHolder.imageButtonDiscard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("您确认要删除这一条质检报告?");
                        builder.setNegativeButton(android.R.string.cancel, null);
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
            }
            viewHolder.textViewWeight.setText(Utils.getQIRWeightAndQuantity(qualityInspectionReport, workCommand));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), QualityInspectionReportActivity.class);
                    intent.putExtra("qualityInspectionReport", qualityInspectionReport);
                    intent.putExtra("workCommand", workCommand);
                    startActivityForResult(intent, MODIFY_QUALITY_INSPECTION_REPORT);
                }
            });
            return convertView;
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MODIFY_QUALITY_INSPECTION_REPORT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "reset quality inspection report");
                ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
                setTextViewQualityInspected();
                modified = true;
            }
        }
    }

}
