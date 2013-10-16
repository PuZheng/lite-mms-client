package com.jinheyu.lite_mms;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.io.File;

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

        WorkCommand workCommand = getIntent().getParcelableExtra("workCommand");
        QualityInspectionReport qualityInspectionReport = getIntent().getParcelableExtra("qualityInspectionReport");
        DialogFragment dialog = new NewQualityInspectionReportDialogFragment(workCommand, qualityInspectionReport);
        dialog.show(ft, "NOTICE_FRAGMENT");
    }

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

}