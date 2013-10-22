package com.jinheyu.lite_mms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;

import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-10-21.
 */
public class QualityInspectionReportActivity extends Activity {

    private EditText editTextWeight;
    private EditText editTextQuantity;
    private WorkCommand workCommand;
    private int qualityInspectionResult = -1;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private ImageButton imageButton;
    private QualityInspectionReport qualityInspectionReport;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_inspection_report);

        workCommand = getIntent().getParcelableExtra("workCommand");
        qualityInspectionReport = getIntent().getParcelableExtra("qualityInspectionReport");

        editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        editTextWeight.setText(String.valueOf(qualityInspectionReport.getWeight()));
        editTextWeight.setSelection(editTextWeight.getText().length());
        editTextQuantity = (EditText) findViewById(R.id.editTextQuantity);
        TableRow tableRowQuantity = (TableRow) findViewById(R.id.tableRowQuantity);
        if (workCommand.measuredByWeight()) {
            tableRowQuantity.setVisibility(View.GONE);
        } else {
            editTextQuantity.setText(String.valueOf(qualityInspectionReport.getQuantity()));
            editTextQuantity.setSelection(editTextQuantity.getText().length());
        }

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri fileUri = Utils.getTempQIReportPicUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        };
        if (!Utils.isEmptyString(qualityInspectionReport.getLocalPicPath())) {
            //InputStream inputStream = new FileInputStream(new File());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inSampleSize = Constants.MIDDLE_SAMPLE_SIZE;
            imageButton.setImageBitmap(BitmapFactory.decodeFile(qualityInspectionReport.getLocalPicPath(), options));
        } else if (!Utils.isEmptyString(qualityInspectionReport.getPicUrl())) {
            new GetImageTask(imageButton, qualityInspectionReport.getPicUrl(), true, onClickListener).execute(Constants.MIDDLE_SAMPLE_SIZE);
        } else {
            imageButton.setImageResource(R.drawable.content_picture);
        }
        imageButton.setOnClickListener(onClickListener);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> results = new ArrayList<String>();
        int position = 0;
        for (int i=0; i < QualityInspectionReport.getResultList().size(); ++i) {
            int result = QualityInspectionReport.getResultList().get(i);
            String literalResult = QualityInspectionReport.getLiteralResult(result);
            if (result == qualityInspectionReport.getResult()) {
                position = i;
            }
            results.add(literalResult);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.simple_spinner_item, android.R.id.text1,
                results);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                qualityInspectionResult = QualityInspectionReport.getResultList().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        File file = new File(Utils.getTempQIReportPicUri().getPath());
        if (file.exists()) {
            file.delete();
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quality_inspection_report_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                if (Utils.testLackInput(this, editTextWeight, "请填写重量") &&
                        (workCommand.measuredByWeight() || Utils.testLackInput(this, editTextQuantity, "请填写数量"))) {
                    MyApp.removeQualityInspectionReport(qualityInspectionReport.getResult());
                    QualityInspectionReport targetQualityInspectionReport = MyApp.getQualityInspectionReport(qualityInspectionResult);
                    if (targetQualityInspectionReport == null) {
                        targetQualityInspectionReport = new QualityInspectionReport();
                        targetQualityInspectionReport.setWeight(Integer.valueOf(editTextWeight.getText().toString()));
                        if (!workCommand.measuredByWeight()) {
                            targetQualityInspectionReport.setQuantity(Integer.valueOf(editTextQuantity.getText().toString()));
                        }
                        targetQualityInspectionReport.setResult(qualityInspectionResult);
                        targetQualityInspectionReport.setLocalPicPath(qualityInspectionReport.getLocalPicPath());
                        MyApp.addQualityInspectionReport(targetQualityInspectionReport);
                    } else {
                        targetQualityInspectionReport.setWeight(Integer.valueOf(editTextWeight.getText().toString())
                                + targetQualityInspectionReport.getWeight());
                        if (!workCommand.measuredByWeight()) {
                            targetQualityInspectionReport.setQuantity(Integer.valueOf(editTextQuantity.getText().toString())
                                    + targetQualityInspectionReport.getQuantity());
                        }
                        targetQualityInspectionReport.setLocalPicPath(qualityInspectionReport.getLocalPicPath());
                    }
                    File from = new File(Utils.getTempQIReportPicUri().getPath());
                    if (from.exists()) {
                        String picFileName = Utils.getFakeQIReportPicPath(targetQualityInspectionReport.getResult());
                        targetQualityInspectionReport.setLocalPicPath(picFileName);
                        File to = new File(picFileName);
                        from.renameTo(to);
                    }
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case R.id.action_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap photo;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = Constants.MIDDLE_SAMPLE_SIZE;
                photo = BitmapFactory.decodeFile(Utils.getTempQIReportPicUri().getPath(), options);
                imageButton.setImageBitmap(photo);

            }
        }
    }

}
