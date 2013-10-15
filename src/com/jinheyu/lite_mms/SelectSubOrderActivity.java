package com.jinheyu.lite_mms;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.DeliverySession;
import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.StoreBill;
import com.jinheyu.lite_mms.data_structures.SubOrder;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by xc on 13-8-18.
 */
public class SelectSubOrderActivity extends FragmentActivity implements PullToRefreshAttacher.OnRefreshListener {

    private static final boolean BYPASS_ONE_SUB_ORDER = false;
    private DeliverySession deliverySession;
    private TextView textViewNoData;
    private LinearLayout linearLayoutOrders;
    private PullToRefreshAttacher mPullToRefreshAttacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sub_order);

        deliverySession = getIntent().getParcelableExtra("deliverySession");
        textViewNoData = (TextView) findViewById(R.id.textViewNoData);
        linearLayoutOrders = (LinearLayout) findViewById(R.id.linearLayoutOrders);
        TextView textViewStepName = (TextView) findViewById(R.id.textViewStepName);
        textViewStepName.setText(getString(R.string.step_n, "二") + ": " + getString(R.string.choose_order));

        mPullToRefreshAttacher = Utils.initPullToRereshAttacher(this, this);
        new GetDeliverySessionTask(this).execute(deliverySession.getId());
    }

    @Override
    public void onRefreshStarted(View view) {
        new GetDeliverySessionTask(this).execute(deliverySession.getId());
    }

    private class GetDeliverySessionTask extends AsyncTask<Integer, Void, Void> {
        private final Context context;
        private DeliverySession mDeliverySession;
        private Exception ex;

        public GetDeliverySessionTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... deliverySessionIds) {
            try {
                mDeliverySession = MyApp.getWebServieHandler().getDeliverySession(deliverySessionIds[0]);
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
        protected void onPostExecute(Void arg) {
            if (ex != null) {
                Utils.displayError(SelectSubOrderActivity.this, ex);
                return;
            }
            if (BYPASS_ONE_SUB_ORDER && mDeliverySession.getSubOrderCount() == 1) {
                Intent intent = new Intent(context, CreateDeliveryTaskActivity.class);
                Order order = mDeliverySession.getOrderList().get(0);
                SubOrder subOrder = order.getSubOrderList().get(0);
                intent.putExtra("subOrder", subOrder);
                intent.putExtra("customerOrderNumber", order.getCustomerOrderNumber());
                intent.putExtra("customer", order.getCustomerName());
                intent.putExtra("selectOrderBypassed", true);
                intent.putExtras(getIntent().getExtras());
                finish();
                startActivity(intent);
            } else {
                if (mDeliverySession.getSubOrderCount() == 0) {
                    textViewNoData.setVisibility(View.VISIBLE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                    linearLayoutOrders.removeAllViews();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    for (Order order : mDeliverySession.getOrderList()) {
                        ft.add(R.id.linearLayoutOrders, new OrderFragment(order), "ORDER");
                    }
                    ft.commit();
                }
                mPullToRefreshAttacher.setRefreshComplete();
            }

        }
    }

    class OrderFragment extends Fragment {
        private final Order order;

        public OrderFragment(Order order) {
            this.order = order;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_order,
                    container, false);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(String.format("订单%s(%s)", order.getCustomerOrderNumber(), order.getCustomerName()));
            ListView listView = (ListView) view.findViewById(R.id.listView);
            listView.setAdapter(new MyListAdapter(SelectSubOrderActivity.this, order));
            return view;
        }

    }

    class MyListAdapter extends BaseAdapter {
        private final Context context;
        private final Order order;

        public MyListAdapter(Context context, Order order) {
            this.context = context;
            this.order = order;
        }

        @Override
        public int getCount() {
            return order.getSubOrderList().size();
        }

        @Override
        public Object getItem(int i) {
            return order.getSubOrderList().get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.two_line_list_item, null);
                viewHolder = new ViewHolder((TextView) view.findViewById(android.R.id.text1),
                        (TextView) view.findViewById(android.R.id.text2));
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            final SubOrder subOrder = (SubOrder) getItem(i);
            List<String> storeBillIdList = new ArrayList<String>();
            for (StoreBill storeBill : subOrder.getStoreBillList()) {
                storeBillIdList.add(String.valueOf(storeBill.getId()));
            }
            viewHolder.textView1.setText("仓单号: " + Utils.join(storeBillIdList, ", "));
            viewHolder.textView2.setText("产品: " + subOrder.getWholeProductName());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CreateDeliveryTaskActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    intent.putExtra("subOrder", subOrder);
                    intent.putExtra("customerOrderNumber", order.getCustomerOrderNumber());
                    intent.putExtra("customer", order.getCustomerName());
                    startActivity(intent);
                }
            });
            return view;
        }

        ;

        class ViewHolder {
            TextView textView1;
            TextView textView2;

            public ViewHolder(TextView textView1, TextView textView2) {
                this.textView1 = textView1;
                this.textView2 = textView2;
            }
        }
    }
}