package com.jinheyu.lite_mms;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xc on 13-10-14.
 */
public class CreateQIReportStep2 extends ListActivity {

    private static final int TAKE_PICTURE_CODE = 100;
    private List<Pair<Integer, String>> pairs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_create_qi_report_step2);
        pairs = new ArrayList<Pair<Integer, String>>();
        pairs.add(new Pair<Integer, String>(QualityInspectionReport.NEXT_PROCEDURE, "通过并转下道工序"));
        pairs.add(new Pair<Integer, String>(QualityInspectionReport.DISCARD, "报废"));
        pairs.add(new Pair<Integer, String>(QualityInspectionReport.FINISHED, "通过"));
        pairs.add(new Pair<Integer, String>(QualityInspectionReport.REPAIR, "返修"));
        pairs.add(new Pair<Integer, String>(QualityInspectionReport.REPLATE, "返镀"));

        List<String> results = new ArrayList<String>();
        for (Pair<Integer, String> pair: pairs) {
            results.add(pair.second);
        }
        setListAdapter(new MyListAdapter(this, android.R.layout.simple_list_item_1, results));
    }

    class MyListAdapter extends ArrayAdapter<String> {

        public MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = super.getView(position, convertView, parent);
            ret.setTag(pairs.get(position).first);
            ret.setOnClickListener(new View.OnClickListener() {
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