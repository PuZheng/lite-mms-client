package com.jinheyu.lite_mms;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.jinheyu.lite_mms.data_structures.Harbor;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xc on 13-8-14.
 */
public class SelectHarborActivity extends ListActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_harbor);
        new GetHarborList().execute();
    }

    class GetHarborList extends AsyncTask<Void, Void, List<Harbor>> {

        private Exception ex = null;

        @Override
        protected List<Harbor> doInBackground(Void... voids) {
            try {
                List<Harbor> harborList = MyApp.getWebServieHandler().getHarborList();
                return harborList;
            } catch (IOException e) {
                e.printStackTrace();
                ex = e;
            } catch (JSONException e) {
                e.printStackTrace();
                ex = e;
            } catch (BadRequest badRequest) {
                badRequest.printStackTrace();
                ex = badRequest;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Harbor> harborList) {
            if (harborList == null) {
                Utils.displayError(SelectHarborActivity.this, ex);
                return;
            }
            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            for (Harbor harbor : harborList) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("name", harbor.getName());
                data.add(row);
            }
            setListAdapter(new SimpleAdapter(SelectHarborActivity.this, data, R.layout.simple_list_item,
                    new String[]{"name"}, new int[]{R.id.text1}));

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String harborName = ((Map<String, String>) adapterView.getAdapter().getItem(i)).get("name");
                    Intent intent = new Intent(SelectHarborActivity.this, SelectCustomerActivity.class);
                    Bundle bundle = getIntent().getExtras();
                    intent.putExtras(bundle);
                    intent.putExtra("harbor", new Harbor(harborName));
                    startActivity(intent);
                }
            });
        }
    }
}

