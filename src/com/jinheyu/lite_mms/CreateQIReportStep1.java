package com.jinheyu.lite_mms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

/**
 * Created by xc on 13-10-14.
 */
public class CreateQIReportStep1 extends Activity {

    private static final int SET_RESULT_CODE = 99;
    private WorkCommand workCommand;
    private EditText editTextQuantity;
    private EditText editTextWeight;
    private Toast backToast;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_qi_report_step1);
        TextView textViewWorkCommandProcessedWeight = (TextView) findViewById(R.id.textViewWorkCommandProcessedWeight);
        TextView textViewQualityInspectedWeight = (TextView) findViewById(R.id.textViewQualityInspectedWeight);
        TextView textViewHint = (TextView) findViewById(R.id.textViewHint);
        this.editTextQuantity = (EditText) findViewById(R.id.editTextQuantity);
        this.editTextWeight = (EditText)findViewById(R.id.editTextWeight);
        TableRow tableRowQuantity = (TableRow) findViewById(R.id.tableRowQuantity);

        this.workCommand = getIntent().getParcelableExtra("workCommand");
        if (workCommand.getOrderType() == Order.STANDARD_ORDER_TYPE) {
            tableRowQuantity.setVisibility(View.GONE);
        }
        int qualityInspectedWeight = 0;
        for (QualityInspectionReport qualityInspectionReport: MyApp.getQualityInspectionReports()) {
            qualityInspectedWeight += qualityInspectionReport.getWeight();
        }

        textViewQualityInspectedWeight.setText(String.valueOf(qualityInspectedWeight) + "公斤");
        textViewWorkCommandProcessedWeight.setText(String.valueOf(workCommand.getProcessedWeight()) + "公斤");
        textViewHint.setText(String.format("估计待质检重量为%d公斤", workCommand.getProcessedWeight() - qualityInspectedWeight));
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editTextWeight, 0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateQIReportStep1.this, CreateQIReportStep2.class);
                intent.putExtra("workCommand", CreateQIReportStep1.this.workCommand);
                QualityInspectionReport qualityInspectionReport = new QualityInspectionReport();
                qualityInspectionReport.setWeight(Integer.valueOf(CreateQIReportStep1.this.editTextWeight.getText().toString()));
                if (workCommand.getOrderType() == Order.EXTRA_ORDER_TYPE) {
                    qualityInspectionReport.setQuantity(Integer.valueOf(CreateQIReportStep1.this.editTextQuantity.getText().toString()));
                }
                intent.putExtra("qualityInspectionReport", qualityInspectionReport);
                startActivityForResult(intent, SET_RESULT_CODE);

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backToast != null && backToast.getView().getWindowToken() != null) {
            finish();
            backToast.cancel();
        } else {
            backToast = Toast.makeText(this, "再按一次返回将取消本次任务", Toast.LENGTH_SHORT);
            backToast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_RESULT_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
