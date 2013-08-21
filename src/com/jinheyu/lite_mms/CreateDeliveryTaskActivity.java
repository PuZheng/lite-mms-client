package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.DeliverySession;
import com.jinheyu.lite_mms.data_structures.StoreBill;
import com.jinheyu.lite_mms.data_structures.SubOrder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xc on 13-8-17.
 */
public class CreateDeliveryTaskActivity extends FragmentActivity {

    private DeliverySession deliverySession;
    private SubOrder subOrder;
    private List<StoreBillFragment> storeBillFragmentList;
    private StoreBillFragment fragmentHalfDelivered;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handle_delivery_session);
        TextView textViewStepName = (TextView) findViewById(R.id.textViewStepName);
        textViewStepName.setText(getString(R.string.step_n, getIntent().getBooleanExtra("selectOrderBypassed", false) ? "二" : "三") + ": " +
                getString(R.string.handle_delivery_session));
        deliverySession = getIntent().getParcelableExtra("deliverySession");
        subOrder = getIntent().getParcelableExtra("subOrder");
        storeBillFragmentList = new ArrayList<StoreBillFragment>();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (StoreBill storeBill: subOrder.getStoreBillList()) {
            StoreBillFragment storeBillFragment = new StoreBillFragment(storeBill);
            storeBillFragmentList.add(storeBillFragment);
            ft.add(R.id.tableLayoutSubOrderList, storeBillFragment, "STORE_BILL");
        }
        ft.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("完全装货");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem = menu.add("部分装货");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem = menu.add("帮助");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        List<Pair<StoreBill, Boolean>> storeBillPairList = new ArrayList<Pair<StoreBill, Boolean>>();
        for (StoreBillFragment storeBillFragment: storeBillFragmentList) {
            if (storeBillFragment.getStatus() != StoreBillFragment.UNDELIVERED) {
                storeBillPairList.add(new Pair<StoreBill, Boolean>(storeBillFragment.getStoreBill(),
                        storeBillFragment.getStatus()==StoreBillFragment.DELIVERED));
            }
        }
        if ((item.getTitle().equals("完全装货") || item.getTitle().equals("部分装货")) && storeBillPairList.isEmpty()) {
            Toast.makeText(this, "请至少(部分)发货一个仓单!", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }

        if (item.getTitle().equals("完全装货")) {
            showNoticeDialog(storeBillPairList, true);
        } else if (item.getTitle().equals("部分装货")) {
            showNoticeDialog(storeBillPairList, false);
        } else if (item.getTitle().equals("帮助")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateDeliveryTaskActivity.this);
            builder.setPositiveButton(R.string.i_see, null);
            builder.setTitle("帮助");
            try {
                InputStream inputStream = getAssets().open("create_delivery_task_help.html");
                byte buf[] = new byte[inputStream.available()];
                inputStream.read(buf);
                builder.setMessage(Html.fromHtml(new String(buf)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNoticeDialog(List<Pair<StoreBill, Boolean>> storeBillPairList, boolean finished) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(
                "NOTICE_FRAGMENT");
        if (prev != null) {
            ft.remove(prev);
        }
        DialogFragment dialog = new NoticeDialogFragment(storeBillPairList, finished);
        dialog.show(ft, "NOTICE_FRAGMENT");
    }

    class StoreBillFragment extends Fragment {
        private static final int UNDELIVERED = 0;
        private static final int DELIVERED = 1;
        private static final int HALF_DELIVERED = 2;
        private static final int MAX_STATUS = 3;

        private int status;
        private final StoreBill storeBill;
        private TextView textViewStatus;
        private Map<Integer, Pair<String, Integer>> statusMap;
        private View view;

        public String getCurrentStatusText() {
            return statusMap.get(status).first;
        }

        public int getCurrentStatusBackgroundColor() {
            return statusMap.get(status).second;
        }

        public StoreBillFragment(StoreBill storeBill) {
            this.storeBill = storeBill;
            this.statusMap = new HashMap<Integer, Pair<String, Integer>>();
            this.status = UNDELIVERED;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            statusMap.put(UNDELIVERED, new Pair<String, Integer>("未发货", getResources().getColor(R.color.undelivered)));
            statusMap.put(DELIVERED, new Pair<String, Integer>("仓单发完", getResources().getColor(android.R.color.holo_green_light)));
            statusMap.put(HALF_DELIVERED, new Pair<String, Integer>("仓单剩余", getResources().getColor(android.R.color.holo_orange_light)));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_sub_order, container, false);
            textViewStatus = (TextView) view.findViewById(R.id.textViewStatus);
            TextView textViewId = (TextView) view.findViewById(R.id.textViewId);
            TextView textViewHarbor = (TextView) view.findViewById(R.id.textViewHarbor);
            TextView textViewWeight = (TextView) view.findViewById(R.id.textViewWeight);

            textViewStatus.setText(getCurrentStatusText());
            view.setBackgroundColor(getCurrentStatusBackgroundColor());

            textViewId.setText(String.valueOf(storeBill.getId()));
            textViewHarbor.setText(storeBill.getHarborName());
            textViewWeight.setText(String.valueOf(storeBill.getWeight()));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    status = (status + 1) % MAX_STATUS;
                    textViewStatus.setText(getCurrentStatusText());
                    view.setBackgroundColor(getCurrentStatusBackgroundColor());
                    if (status == HALF_DELIVERED) {
                        if (fragmentHalfDelivered != null && fragmentHalfDelivered != StoreBillFragment.this) {
                            fragmentHalfDelivered.setStatus(DELIVERED);
                            fragmentHalfDelivered.refresh();
                        }
                        fragmentHalfDelivered = StoreBillFragment.this;
                    }
                }
            });

            return view;
        }

        private void refresh() {
            textViewStatus.setText(getCurrentStatusText());
            view.setBackgroundColor(getCurrentStatusBackgroundColor());
        }

        void setStatus(int status) {
            this.status = status;
        }

        int getStatus() {
            return status;
        }

        StoreBill getStoreBill() {
            return storeBill;
        }
    }

    private class NoticeDialogFragment extends DialogFragment {
        private final List<Pair<StoreBill, Boolean>> storeBillPairList;
        private final boolean finished;

        public NoticeDialogFragment(List<Pair<StoreBill, Boolean>> storeBillPairList, boolean finished) {
            this.storeBillPairList = storeBillPairList;
            this.finished = finished;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("生成发货任务");
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_delivery_task, null);
            builder.setView(view);
            TextView textViewResult = (TextView) view.findViewById(R.id.textViewResult);
            TextView textViewCustomer = (TextView) view.findViewById(R.id.textViewCustomer);
            TextView textViewOrder = (TextView) view.findViewById(R.id.textViewOrder);
            TextView textViewSubOrder = (TextView) view.findViewById(R.id.textViewSubOrder);
            TextView textViewProduct = (TextView) view.findViewById(R.id.textViewProduct);
            TextView textViewStoreBillList = (TextView) view.findViewById(R.id.textViewStoreBillList);

            textViewResult.setText(finished? "完全装货": "部分装货");
            textViewCustomer.setText(getIntent().getStringExtra("customer"));
            textViewOrder.setText(getIntent().getStringExtra("customerOrderNumber"));
            SubOrder subOrder = getIntent().getParcelableExtra("subOrder");
            textViewSubOrder.setText(String.valueOf(subOrder.getId()));
            textViewProduct.setText(subOrder.getWholeProductName());
            List<String> strings = new ArrayList<String>();
            for (Pair<StoreBill, Boolean> pair: storeBillPairList) {
                strings.add(pair.first.getId() + (pair.second? "(完成)": "(部分完成)"));
            }
            textViewStoreBillList.setText(Utils.join(strings, ", "));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (anyStoreBillRemain()) {
                        askRemainingWeight();
                    } else {
                        createDeliveryTask(0);
                    }
                }
            });
            return builder.create();
        }

        private boolean anyStoreBillRemain() {
            for (Pair<StoreBill, Boolean> pair: storeBillPairList) {
                if (!pair.second) {
                    return true;
                }
            }
            return false;
        }

        private void createDeliveryTask(final int remainingWeight) {
            XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(CreateDeliveryTaskActivity.this);
            builder.msg("正在创建发货任务");
            builder.run(new XProgressableRunnable.XRunnable() {
                @Override
                public void run() throws Exception {
                    MyApp.getWebServieHandler().createDeliveryTask(deliverySession, finished, MyApp.getCurrentUser().getId(),
                            storeBillPairList, remainingWeight);
                }
            });
            builder.okMsg("创建成功");
            builder.after(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(CreateDeliveryTaskActivity.this, LoaderMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    CreateDeliveryTaskActivity.this.startActivity(intent);
                }
            });
            builder.create().start();
        }

        private void askRemainingWeight() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("请输入剩余重量");
            View view = CreateDeliveryTaskActivity.this.getLayoutInflater().inflate(R.layout.dialog_ask_remaing_weight, null);
            builder.setView(view);
            final EditText editText = (EditText) view.findViewById(R.id.editText);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (editText.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateDeliveryTaskActivity.this, "请输入剩余重量", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createDeliveryTask(Integer.valueOf(editText.getText().toString().trim()));
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
            dialog.show();
        }
    }

}