package com.jinheyu.lite_mms;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.jinheyu.lite_mms.data_structures.Customer;
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
public class SelectCustomerActivity extends PtrListActivity {

    private EditText editTextAbbr;
    private ProgressBar progressBar;
    private List<Map<String, Object>> appearedCustomerList = null;
    private List<Customer> customerList = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_customer);
        editTextAbbr = (EditText) findViewById(R.id.editTextAbbr);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        editTextAbbr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String needle = editable.toString();
                appearedCustomerList.clear();
                for (Customer customer: customerList) {
                    if (customer.getAbbr().startsWith(needle)) {
                        Map<String, Object> row = new HashMap<String, Object>();
                        row.put("name", customer.getName());
                        row.put("id", customer.getId());
                        appearedCustomerList.add(row);
                    }
                }
                ((SimpleAdapter)getListAdapter()).notifyDataSetChanged();
            }
        });

        appearedCustomerList = new ArrayList<Map<String, Object>>();

        pullToRefreshInit();

        new GetCustomerListTask().execute();
    }

    @Override
    public void onRefreshStarted(View view) {
        new GetCustomerListTask().execute();
    }

    class GetCustomerListTask extends AsyncTask<Void, Void, Boolean> {

        private Exception ex = null;


        @Override
        protected void onPreExecute () {
            progressBar.setVisibility(View.VISIBLE);
            getListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                customerList = MyApp.getWebServieHandler().getCustomerList();
                return Boolean.TRUE;
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
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Utils.displayError(SelectCustomerActivity.this, ex);
                return;
            }

            for (Customer customer: customerList) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("name", customer.getName());
                row.put("id", customer.getId());
                appearedCustomerList.add(row);
            }

            if (getListAdapter() == null) {
                setListAdapter(new SimpleAdapter(SelectCustomerActivity.this, appearedCustomerList, R.layout.simple_list_item,
                        new String[] {"name"}, new int[] {R.id.text1}));
            } else {
                ((BaseAdapter)getListAdapter()).notifyDataSetChanged();
            }
            editTextAbbr.setText("");
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(SelectCustomerActivity.this, TakeUnloadTaskPicActivity.class);
                    Map<String, Object> customer = (Map<String, Object>) getListAdapter().getItem(i);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra("customer", new Customer((Integer) customer.get("id"), (String) customer.get("name"), ""));
                    startActivity(intent);
                }
            });
            getListView().setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            getPullToRefreshAttacher().setRefreshComplete();
        }
    }
}