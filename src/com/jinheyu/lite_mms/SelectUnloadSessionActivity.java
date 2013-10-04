package com.jinheyu.lite_mms;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jinheyu.lite_mms.data_structures.UnloadSession;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by xc on 13-8-13.
 */
public class SelectUnloadSessionActivity extends PtrListActivity {
    private static final String TAG = "SelectUnloadSessionActivity";
    private Toast backToast;
    private TextView textViewNoData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_unload_session);
        textViewNoData = (TextView) findViewById(R.id.textViewNoData);
        pullToRefreshInit();
        new GetUnloadSessionListTask().execute();
    }

    @Override
    public void onRefreshStarted(View view) {
        new GetUnloadSessionListTask().execute();
    }

    class GetUnloadSessionListTask extends AsyncTask<Void, Void, List<UnloadSession>> {
        Exception ex = null;

        @Override
        protected List<UnloadSession> doInBackground(Void... voids) {


            try {
                List<UnloadSession> unloadSessionList = MyApp.getWebServieHandler().getUnloadSessionList();
                return unloadSessionList;

            } catch (JSONException e) {
                e.printStackTrace();
                ex = e;
            } catch (IOException e) {
                e.printStackTrace();
                ex = e;
            } catch (BadRequest badRequest) {
                badRequest.printStackTrace();
                ex = badRequest;
            }


            return null;
        }

        @Override
        protected void onPostExecute(List<UnloadSession> unloadSessionList) {
            if (ex != null) {
                Utils.displayError(SelectUnloadSessionActivity.this, ex);
                return;
            }
            if (unloadSessionList.isEmpty()) {
                textViewNoData.setVisibility(View.VISIBLE);
                getListView().setVisibility(View.GONE);
            } else {
                textViewNoData.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
                setListAdapter(new MyListAdapter(SelectUnloadSessionActivity.this, unloadSessionList));
            }
            getPullToRefreshAttacher().setRefreshComplete();
        }
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

    class MyListAdapter extends BaseAdapter {

        private final Context context;
        private final List<UnloadSession> unloadSessionList;
        private final LayoutInflater layoutInflater;

        public MyListAdapter(Context context, List<UnloadSession> unloadSessionList) {
            this.context = context;
            this.unloadSessionList = unloadSessionList;
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return unloadSessionList.size();
        }

        @Override
        public Object getItem(int i) {
            return unloadSessionList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return unloadSessionList.get(i).getId();
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
            final UnloadSession unloadSession = (UnloadSession) getItem(i);
            viewHolder.textView.setText(unloadSession.getPlate());
            if (unloadSession.isLocked()) {
                viewHolder.imageView.setVisibility(View.VISIBLE);
                viewHolder.textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                viewHolder.imageView.setVisibility(View.GONE);
                viewHolder.textView.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (unloadSession.isLocked()) {
                        Toast.makeText(context, getString(R.string.vehicle_weighing_or_work_flow_processing), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(SelectUnloadSessionActivity.this,
                                SelectHarborActivity.class);
                        intent.putExtra("unloadSession", unloadSession);
                        startActivity(intent);
                    }
                }
            });
            return view;
        }

    }
}