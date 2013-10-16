package com.jinheyu.lite_mms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.ImageCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by xc on 13-10-14.
 */
public class CreateQIReportStep3 extends FragmentActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "CreateQIREportStep3";
    private ImageView imageView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qi_report_step3);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (new File(Utils.getTempQIReportPicUri().getPath()).exists()) {
            Bitmap photo;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            photo = BitmapFactory.decodeFile(Utils.getTempQIReportPicUri().getPath(), options);
            imageView.setImageBitmap(photo);
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri fileUri = Utils.getTempQIReportPicUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:
                if (!new File(Utils.getTempQIReportPicUri().getPath()).exists()) {
                    Toast.makeText(this, "请拍照后提交", Toast.LENGTH_SHORT).show();
                } else {
                    showNoticeDialog();
                }
                break;
            case R.id.action_take_pic:
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri fileUri = Utils.getTempQIReportPicUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNoticeDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(
                "NOTICE_FRAGMENT");
        if (prev != null) {
            ft.remove(prev);
        }

        DialogFragment dialog = new NoticeDialogFragment();
        dialog.show(ft, "NOTICE_FRAGMENT");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap photo;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                photo = BitmapFactory.decodeFile(Utils.getTempQIReportPicUri().getPath(), options);
                imageView.setImageBitmap(photo);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_qi_report_step3, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class NoticeDialogFragment extends DialogFragment {

        private NoticeDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("创建质检报告");
            View view = View.inflate(getActivity(), R.layout.dialog_create_quality_inspection_report, null);
            TextView textViewResult = (TextView) view.findViewById(R.id.textViewResult);
            TextView textViewWeight = (TextView) view.findViewById(R.id.textViewWeight);
            final QualityInspectionReport qualityInspectionReport = getIntent().getParcelableExtra("qualityInspectionReport");
            final WorkCommand workCommand = getIntent().getParcelableExtra("workCommand");
            textViewResult.setText(qualityInspectionReport.getLiterableResult());
            textViewWeight.setText(Utils.getQIRWeightAndQuantity(qualityInspectionReport, workCommand));
            builder.setView(view);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {

                    boolean mergedWithOld = false;
                    for (int i=0; i < MyApp.getQualityInspectionReports().size(); ++i) {
                        QualityInspectionReport oldQualityInspectionReport = MyApp.getQualityInspectionReports().get(i);
                        if (oldQualityInspectionReport.getResult() == qualityInspectionReport.getResult()) {
                            oldQualityInspectionReport.setWeight(qualityInspectionReport.getWeight() + oldQualityInspectionReport.getWeight());
                            if (!workCommand.measured_by_weight()) {
                                oldQualityInspectionReport.setQuantity(qualityInspectionReport.getQuantity() + oldQualityInspectionReport.getQuantity());
                            }
                            try {
                                String url = getFakeQIReportUrl(workCommand, i);
                                InputStream inputStream = new FileInputStream(new File(Utils.getTempQIReportPicUri().getPath()));
                                ImageCache.getInstance(getActivity()).addBitmapToCache(Utils.getMd5Hash(url), inputStream);
                                oldQualityInspectionReport.setPicUrl(url);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            mergedWithOld = true;
                            break;
                        }
                    }
                    if (!mergedWithOld) {
                        InputStream inputStream = null;
                        try {
                            inputStream = new FileInputStream(new File(Utils.getTempQIReportPicUri().getPath()));
                            String url = getFakeQIReportUrl(workCommand, MyApp.getQualityInspectionReports().size());
                            ImageCache.getInstance(getActivity()).addBitmapToCache(Utils.getMd5Hash(url), inputStream);
                            qualityInspectionReport.setPicUrl(url);
                            MyApp.addQualityInspectionReport(qualityInspectionReport);
                            Log.d(TAG, "add quality inspection report");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    new File(Utils.getTempQIReportPicUri().getPath()).delete();
                    CreateQIReportStep3.this.setResult(RESULT_OK);
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        private String getFakeQIReportUrl(WorkCommand workCommand, int i) {
            return "qir-" + workCommand.getId() + "-" + i + ".jpeg";
        }
    }

}