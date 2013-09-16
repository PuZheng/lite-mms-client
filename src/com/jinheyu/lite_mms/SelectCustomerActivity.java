package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
public class SelectCustomerActivity extends ListActivity {

    private EditText editTextAbbr;
    private GetCustomerListTask getCustomerListTask;
    private ProgressBar progressBar;


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
                getCustomerListTask.appearedCustomerList.clear();
                for (Customer customer : getCustomerListTask.customerList) {
                    if (customer.getAbbr().startsWith(needle)) {
                        Map<String, Object> row = new HashMap<String, Object>();
                        row.put("name", customer.getName());
                        row.put("id", customer.getId());
                        getCustomerListTask.appearedCustomerList.add(row);
                    }
                }
                ((SimpleAdapter) getListAdapter()).notifyDataSetChanged();
            }
        });

        getCustomerListTask = new GetCustomerListTask();
        getCustomerListTask.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        menuItem = menu.add(getString(R.string.refresh));
        menuItem.setIcon(R.drawable.navigation_refresh);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.refresh))) {
            editTextAbbr.setText("");
            getCustomerListTask = new GetCustomerListTask();
            getCustomerListTask.execute();
        }
        return super.onOptionsItemSelected(item);
    }


    class GetCustomerListTask extends AsyncTask<Void, Void, Boolean> {

        private Exception ex = null;
        private List<Customer> customerList = null;
        private List<Map<String, Object>> appearedCustomerList = null;

        public GetCustomerListTask() {
            customerList = new ArrayList<Customer>();
            appearedCustomerList = new ArrayList<Map<String, Object>>();
        }

        @Override
        protected void onPreExecute() {
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

            for (Customer customer : customerList) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("name", customer.getName());
                row.put("id", customer.getId());
                appearedCustomerList.add(row);
            }

            if (getListAdapter() == null) {
                setListAdapter(new SimpleAdapter(SelectCustomerActivity.this, appearedCustomerList, R.layout.simple_list_item,
                        new String[]{"name"}, new int[]{R.id.text1}));
            } else {
                ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
            }
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
        }
    }
}