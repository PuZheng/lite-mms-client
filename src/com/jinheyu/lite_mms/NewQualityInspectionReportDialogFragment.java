package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.io.File;

/**
 * Created by xc on 13-10-16.
 */
public class NewQualityInspectionReportDialogFragment extends DialogFragment {
    private final WorkCommand workCommand;
    private final QualityInspectionReport qualityInspectionReport;

    public NewQualityInspectionReportDialogFragment(WorkCommand workCommand, QualityInspectionReport qualityInspectionReport) {
        this.workCommand = workCommand;
        this.qualityInspectionReport = qualityInspectionReport;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("创建质检报告");
        View view = View.inflate(getActivity(), R.layout.dialog_create_quality_inspection_report, null);
        TextView textViewResult = (TextView) view.findViewById(R.id.textViewResult);
        TextView textViewWeight = (TextView) view.findViewById(R.id.textViewWeight);
        textViewResult.setText(qualityInspectionReport.getLiterableResult());
        textViewWeight.setText(Utils.getQIRWeightAndQuantity(qualityInspectionReport, workCommand));
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {

                String picFileName = "";
                File from = new File(Utils.getTempQIReportPicUri().getPath());
                QualityInspectionReport targetQualityInspectionReport = MyApp.getQualityInspectionReport(qualityInspectionReport.getResult());
                if (targetQualityInspectionReport == null) {
                    MyApp.addQualityInspectionReport(qualityInspectionReport);
                    targetQualityInspectionReport = qualityInspectionReport;
                } else {
                    targetQualityInspectionReport.setWeight(qualityInspectionReport.getWeight() + targetQualityInspectionReport.getWeight());
                    if (!workCommand.measuredByWeight()) {
                        targetQualityInspectionReport.setQuantity(qualityInspectionReport.getQuantity() + targetQualityInspectionReport.getQuantity());
                    }
                }
                if (from.exists()) {
                    picFileName = getFakeQIReportPicPath(targetQualityInspectionReport.getResult());
                    targetQualityInspectionReport.setLocalPicPath(picFileName);
                    File to = new File(picFileName);
                    from.renameTo(to);
                }
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private String getFakeQIReportPicPath(int i) {
        return Utils.getStorageDir() + "qir-" + "-" + i + ".jpeg";
    }
}
