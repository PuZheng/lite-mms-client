package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.DeliverySession;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by xc on 13-8-17.
 */
public class SelectDeliverySessionActivity extends ListActivity {

    private TextView textViewNoData;
    private Toast backToast;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_delivery_session);
        textViewNoData = (TextView) findViewById(R.id.textViewNoData);
        new GetDeliverySessionTask().execute();
    }

    @Override
    public void onBackPressed() {
        if(backToast !=null&& backToast.getView().getWindowToken()!=null) {
            finish();
            backToast.cancel();
        } else {
            backToast = Toast.makeText(this, "再按一次返回将取消本次任务", Toast.LENGTH_SHORT);
            backToast.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(getString(R.string.refresh));
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setIcon(R.drawable.navigation_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.refresh))) {
            new GetDeliverySessionTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }
    private class GetDeliverySessionTask extends AsyncTask<Void, Void, List<DeliverySession>> {

        private Exception ex;

        @Override
        protected List<DeliverySession> doInBackground(Void... voids) {
            try {
                return MyApp.getWebServieHandler().getDeliverySessionList();
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
        protected void onPostExecute(List<DeliverySession> deliverySessionList) {
            if (ex != null) {
                Utils.displayError(SelectDeliverySessionActivity.this, ex);
                return;
            }
            if (deliverySessionList.isEmpty()) {
                getListView().setVisibility(View.GONE);
                textViewNoData.setVisibility(View.VISIBLE);
            } else {
                textViewNoData.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
                setListAdapter(new MyListAdapter(SelectDeliverySessionActivity.this, deliverySessionList));
            }

            super.onPostExecute(deliverySessionList);
        }
    }

    private class MyListAdapter extends BaseAdapter {
        private final Context context;
        private final List<DeliverySession> deliverySessionList;
        private final LayoutInflater layoutInflater;

        public MyListAdapter(Context context, List<DeliverySession> deliverySessionList) {
            this.context = context;
            this.deliverySessionList = deliverySessionList;
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return deliverySessionList.size();
        }

        @Override
        public Object getItem(int i) {
            return deliverySessionList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return deliverySessionList.get(i).getId();
        }

        class ViewHolder {
            TextView textView;
            ImageView imageView;

            public ViewHolder(TextView textView, ImageView imageView) {
                this.textView = textView;
                this.imageView = imageView;
            }
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (view == null) {
                view = layoutInflater.inflate(R.layout.session_list_item, null);
                viewHolder = new ViewHolder((TextView) view.findViewById(R.id.textViewPlate),
                        (ImageView) view.findViewById(R.id.imageViewLocked));
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            final DeliverySession deliverySession = (DeliverySession) getItem(i);
            viewHolder.textView.setText(deliverySession.getPlate());
            viewHolder.textView.setTextColor(deliverySession.isLocked()?
                    getResources().getColor(android.R.color.darker_gray):
                    getResources().getColor(android.R.color.primary_text_light));
            viewHolder.imageView.setVisibility(deliverySession.isLocked()? View.VISIBLE: View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deliverySession.isLocked()) {
                        Toast.makeText(context, getString(R.string.vehicle_weighing), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(SelectDeliverySessionActivity.this,
                                SelectSubOrderActivity.class);
                        intent.putExtra("deliverySession", deliverySession);
                        startActivity(intent);
                    }
                }
            });
            return view;
        }
    }
}