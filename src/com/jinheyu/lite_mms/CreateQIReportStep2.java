package com.jinheyu.lite_mms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-10-14.
 */
public class CreateQIReportStep2 extends FragmentActivity {

    private static final int TAKE_PICTURE_CODE = 100;
    private ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_create_qi_report_step2);
        List<String> results = new ArrayList<String>();
        for (int result: QualityInspectionReport.getResultList()) {
            String literalResult = QualityInspectionReport.getLiteralResult(result);
            results.add(literalResult);
        }
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new MyListAdapter(this, R.layout.qi_report_result_list_item, R.id.text1, results));
    }

    class MyListAdapter extends ArrayAdapter<String> {

        public MyListAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = super.getView(position, convertView, parent);
            Button button = (Button) ret.findViewById(R.id.buttonNextStep);
            button.setTag(QualityInspectionReport.getResultList().get(position));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreateQIReportStep2.this, CreateQIReportStep3.class);
                    intent.putExtra("workCommand", getIntent().getParcelableExtra("workCommand"));
                    QualityInspectionReport qualityInspectionReport = getIntent().getParcelableExtra("qualityInspectionReport");
                    qualityInspectionReport.setResult((Integer) v.getTag());
                    intent.putExtra("qualityInspectionReport", qualityInspectionReport);
                    startActivityForResult(intent, TAKE_PICTURE_CODE);
                }
            });
            button = (Button) ret.findViewById(R.id.buttonSubmit);
            button.setTag(QualityInspectionReport.getResultList().get(position));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    QualityInspectionReport qualityInspectionReport = getIntent().getParcelableExtra("qualityInspectionReport");
                    qualityInspectionReport.setResult((Integer) v.getTag());
                    WorkCommand workCommand = getIntent().getParcelableExtra("workCommand");
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag(
                            "NOTICE_FRAGMENT");
                    if (prev != null) {
                        ft.remove(prev);
                    }

                    DialogFragment dialog = new NewQualityInspectionReportDialogFragment(workCommand, qualityInspectionReport);
                    dialog.show(ft, "NOTICE_FRAGMENT");

                }
            });
            return ret;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

}