package com.jinheyu.lite_mms;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.User;
import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;

import java.io.IOException;


public class WorkCommandActivity extends FragmentActivity {
    private WorkCommand mWorkCommand;

    @Override
    public Intent getParentActivityIntent() {
        return new Intent(this, MyApp.getCurrentUser().getDefaultActivity());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_command_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        new GetWorkCommandTask().execute(getIntent().getIntExtra("workCommandId", 0));
    }

    class GetWorkCommandTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                mWorkCommand = MyApp.getWebServieHandler().getWorkCommand(params[0]);
            } catch (BadRequest badRequest) {
                badRequest.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (mWorkCommand != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.layout, new WorkCommandFragment());
                transaction.commit();
            } else {
                Toast.makeText(WorkCommandActivity.this, "加载工单失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class WorkCommandFragment extends Fragment {
        private View rootView;

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            switch (MyApp.getCurrentUser().getGroupId()) {
                case User.TEAM_LEADER:
                    _setTeamLeaderMenu(menu, inflater);
                    break;
                case User.DEPARTMENT_LEADER:
                    _setDepartmentLeaderMenu(menu, inflater);
                default:
                    break;
            }
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_work_command_detail, null);
            setHasOptionsMenu(true);
            _initView();
            return rootView;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            MenuItemWrapper menuItemWrapper = new MenuItemWrapper(getActivity());
            switch (item.getItemId()) {
                case R.id.quick_carryForward:
                    menuItemWrapper.carryForwardQuickly(mWorkCommand);
                    break;
                case R.id.carry_forward:
                    menuItemWrapper.carryForward(mWorkCommand);
                    break;
                case R.id.end_work_command:
                    menuItemWrapper.endWorkCommand(mWorkCommand);
                    break;
                case R.id.add_weight:
                    menuItemWrapper.addWeight(mWorkCommand);
                    break;
                case R.id.action_dispatch:
                    menuItemWrapper.dispatch(mWorkCommand);
                    break;
                case R.id.action_refuse:
                    menuItemWrapper.refuse(mWorkCommand);
                    break;
                case R.id.action_confirm_retrieve:
                    menuItemWrapper.confirmRetrieve(mWorkCommand);
                    break;
                case R.id.action_deny_retrieve:
                    menuItemWrapper.denyRetrieve(mWorkCommand);
                    break;
            }
            return super.onOptionsItemSelected(item);
        }

        private void _initView() {
            _setIdTextView();
            _setOrderNumber();
            _setCustomer();
            _setOrgWeight();
            _setOrgCnt();
            _setPic();
        }

        private void _setCustomer() {
            TextView textView = (TextView) rootView.findViewById(R.id.customer_name);
            textView.setText(mWorkCommand.getCustomerName());
        }

        private void _setDepartmentLeaderMenu(Menu menu, MenuInflater inflater) {

            if (mWorkCommand.getStatus() != Constants.STATUS_LOCKED) {
                inflater.inflate(R.menu.department_leader_dispatch, menu);
            } else {
                inflater.inflate(R.menu.department_leader_locked, menu);
            }
        }

        private void _setIdTextView() {
            TextView idTextView = (TextView) rootView.findViewById(R.id.work_command_id);
            idTextView.setText(String.valueOf(mWorkCommand.getId()));
        }

        private void _setOrderNumber() {
            TextView textView = (TextView) rootView.findViewById(R.id.order_number);
            textView.setText(String.valueOf(mWorkCommand.getOrderNumber()));
        }

        private void _setOrgCnt() {
            TextView textView = (TextView) rootView.findViewById(R.id.org_cnt);
            textView.setText(mWorkCommand.getOrgCnt() + " " + mWorkCommand.getUnit());
        }

        private void _setOrgWeight() {
            TextView textView = (TextView) rootView.findViewById(R.id.org_weight);
            textView.setText(mWorkCommand.getOrgWeight() + " 千克");
        }

        private void _setPic() {
            ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.image);
            new GetImageTask(imageButton, mWorkCommand.getPicPath()).execute();
        }

        private void _setTeamLeaderMenu(Menu menu, MenuInflater inflater) {
            if (mWorkCommand.getStatus() == Constants.STATUS_ENDING) {
                inflater.inflate(R.menu.team_leader_work_command_menu, menu);
            }
        }

    }

}