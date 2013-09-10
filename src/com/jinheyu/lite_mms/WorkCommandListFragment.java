package com.jinheyu.lite_mms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.IOException;
import java.util.List;

/**
 * Created by abc549825@163.com
 * 2013-09-06
 */
public class WorkCommandListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener {
    public static final String ARG_SECTION_NUMBER = "section_number";
    private boolean isActionModeStart;
    private TextView noDataView;
    private int teamId;
    private AsyncTask<Void, Void, List<WorkCommand>> task;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private ProgressDialog mProgressDialog;

    public static WorkCommandListFragment newInstance(int teamId) {
        WorkCommandListFragment mFragment = new WorkCommandListFragment();
        Bundle args = new Bundle();
        args.putInt(WorkCommandListFragment.ARG_SECTION_NUMBER, teamId);
        mFragment.setArguments(args);
        return mFragment;
    }

    public void loadWorkCommandList() {
        mProgressDialog = ProgressDialog.show(WorkCommandListFragment.this.getActivity(), getString(R.string.loading_data), getString(R.string.please_wait), true);
        task = new GetWorkCommandListTask(teamId);
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_work_command_list, container, false);
        final ListView listView = (ListView) rootView.findViewById(android.R.id.list);

        noDataView = (TextView) rootView.findViewById(android.R.id.empty);
        noDataView.setMovementMethod(new ScrollingMovementMethod());

        mPullToRefreshAttacher = ((LeaderMainActivity) getActivity()).getPullToRefreshAttacher();
        mPullToRefreshAttacher.addRefreshableView(listView, this);
        mPullToRefreshAttacher.addRefreshableView(noDataView, this);
        listView.setEmptyView(noDataView);
        teamId = getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 0;
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WorkCommandActivity.class);
                intent.putExtra("work_command", (WorkCommand) listView.getItemAtPosition(position));
                startActivity(intent);
            }
        });
        listView.setMultiChoiceModeListener(new LongTimeClickActionModeCallback());
        loadWorkCommandList();
    }

    @Override
    public void onRefreshStarted(View view) {
        loadWorkCommandList();
    }

    public class LongTimeClickActionModeCallback implements AbsListView.MultiChoiceModeListener {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            setSubTitle(mode);
        }

        private void setSubTitle(ActionMode mode) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle("未选择");
                    break;
                default:
                    mode.setSubtitle("已选中" + checkedCount + "项");
                    break;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            isActionModeStart = true;
            MenuInflater mInflater = mode.getMenuInflater();
            mInflater.inflate(R.menu.team_leader_work_command_list_menu, menu);
            mode.setTitle(getString(R.string.please_select));
            setSubTitle(mode);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isActionModeStart = false;
        }
    }

    public class ViewHolder {
        public TextView idTextView;
        public CheckBox checkBox;

        public ViewHolder(TextView idTextView, CheckBox checkBox) {
            this.idTextView = idTextView;
            this.checkBox = checkBox;
        }
    }

    class WorkCommandListAdapter extends ArrayAdapter<WorkCommand> {
        private final LayoutInflater mInflater;
        private int mResource;


        public WorkCommandListAdapter(Context context, int resourceId, List<WorkCommand> workCommandList) {
            super(context, resourceId, workCommandList);
            this.mResource = resourceId;
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final WorkCommand workCommand = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(mResource, null);
                viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.idTextView), (CheckBox) convertView.findViewById(R.id.check));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.checkBox.setVisibility(isActionModeStart ? View.VISIBLE : View.GONE);
            viewHolder.idTextView.setText(String.valueOf(workCommand.getId()));
            viewHolder.checkBox.setChecked(getListView().isItemChecked(position));
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    getListView().setItemChecked(position, isChecked);
                }
            });
            return convertView;
        }


    }

    class GetWorkCommandListTask extends AsyncTask<Void, Void, List<WorkCommand>> {
        private final int teamId;
        Exception ex;

        public GetWorkCommandListTask(int teamdId) {
            this.teamId = teamdId;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<WorkCommand> doInBackground(Void... params) {
            try {
                return MyApp.getWebServieHandler().getWorkCommandListByTeamId(teamId,
                        new int[]{Constants.STATUS_LOCKED, Constants.STATUS_ENDING});
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

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param workCommandList The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<WorkCommand> workCommandList) {
            if (ex != null) {
                Utils.displayError(WorkCommandListFragment.this.getActivity(), ex);
                return;
            }
            doUpdateView(workCommandList);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            mPullToRefreshAttacher.setRefreshComplete();
        }

        private void doUpdateView(List<WorkCommand> workCommandList) {
            setListAdapter(new WorkCommandListAdapter(WorkCommandListFragment.this.getActivity(),
                    R.layout.fragment_work_command, workCommandList));
        }
    }
}

