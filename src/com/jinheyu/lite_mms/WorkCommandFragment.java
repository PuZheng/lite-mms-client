package com.jinheyu.lite_mms;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.Constants;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Created by xc on 13-10-6.
 */
public class WorkCommandFragment extends Fragment implements PullToRefreshAttacher.OnRefreshListener, UpdateWorkCommand {
    private final int workCommandId;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private View rootView;
    private WorkCommand mWorkCommand;

    public WorkCommandFragment(int workCommandId) {
        this.workCommandId = workCommandId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_work_command, container, false);
        this.mPullToRefreshAttacher = ((GetPullToRefreshAttacher) getActivity()).getPullToRefreshAttacher();
        final PullToRefreshLayout ptrLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);
        new GetWorkCommandAsyncTask(this).execute(this.workCommandId);
        return rootView;
    }

    @Override
    public void onRefreshStarted(View view) {
        new GetWorkCommandAsyncTask(this).execute(this.workCommandId);
    }

    public void mask() {
        View mask = rootView.findViewById(R.id.linearLayoutMask);
        mask.setVisibility(View.VISIBLE);
        View main = rootView.findViewById(R.id.scrollViewWorkCommand);
        main.setVisibility(View.GONE);
        View error = rootView.findViewById(R.id.linearyLayoutError);
        error.setVisibility(View.GONE);
    }

    @Override
    public void updateWorkCommand(WorkCommand workCommand) {
        this.mWorkCommand = workCommand;
        _initView();
        unmask();
    }


    @Override
    public void updateWorkCommandFailed(Exception ex) {
        View mask = rootView.findViewById(R.id.linearLayoutMask);
        mask.setVisibility(View.GONE);
        View main = rootView.findViewById(R.id.scrollViewWorkCommand);
        main.setVisibility(View.GONE);
        View error = rootView.findViewById(R.id.linearyLayoutError);
        error.setVisibility(View.VISIBLE);
        mPullToRefreshAttacher.setRefreshComplete();
    }

    @Override
    public void beforeUpdateWorkCommand() {
        mask();
    }

    public void unmask() {
        View mask = rootView.findViewById(R.id.linearLayoutMask);
        mask.setVisibility(View.GONE);
        View main = rootView.findViewById(R.id.scrollViewWorkCommand);
        main.setVisibility(View.VISIBLE);
        mPullToRefreshAttacher.setRefreshComplete();
    }

    private void _initView() {
        _setIdTextViewAndStatus();
        _setExtra();
        _setHandleType();
        _setOrderNumber();
        _setCustomer();
        _setProductAndSpecType();
        _setTechReq();
        _setProcedureAndPreviousProcedure();
        _setOrgWeightAndCnt();
        _setProcessedWeightAndCnt();
        _setPic();
        _setBackgroundColor();
    }

    private void _setBackgroundColor() {
        TableLayout layout = (TableLayout) rootView.findViewById(R.id.layout);
        boolean odd = true;
        for (int i = 0; i < layout.getChildCount(); i++) {
            View row = layout.getChildAt(i);
            if (row instanceof TableRow && row.getVisibility() == View.VISIBLE) {
                row.setBackgroundColor(Color.parseColor(odd ? "#F9F9F9" : "#FFFFFF"));
                odd = !odd;
            }
        }
    }

    private void _setCustomer() {
        TextView textView = (TextView) rootView.findViewById(R.id.customer_name);
        textView.setText(mWorkCommand.getCustomerName());
    }

    private void _setExtra() {
        List<String> extraMessages = new ArrayList<String>();
        if (mWorkCommand.isUrgent()) {
            extraMessages.add("加急");
        }
        if (mWorkCommand.isRejected()) {
            extraMessages.add("退镀");
        }
        TextView textView = (TextView) rootView.findViewById(R.id.extra);
        View extraView = rootView.findViewById(R.id.extra_row);
        if (extraMessages.isEmpty()) {
            extraView.setVisibility(View.GONE);
        } else {
            extraView.setVisibility(View.VISIBLE);
            textView.setText(Utils.join(extraMessages, " ,"));
        }
    }

    private void _setHandleType() {
        TextView textView = (TextView) rootView.findViewById(R.id.handleType);
        textView.setText(mWorkCommand.getHandleTypeString());
    }

    private void _setIdTextViewAndStatus() {
        TextView idTextView = (TextView) rootView.findViewById(R.id.work_command_id);
        idTextView.setText(String.format("%d(%s)", mWorkCommand.getId(), mWorkCommand.getStatusString()));
    }

    private void _setOrderNumber() {
        TextView textView = (TextView) rootView.findViewById(R.id.order_number);
        textView.setText(String.valueOf(mWorkCommand.getOrderNumber()));
    }

    private void _setOrgWeightAndCnt() {
        TextView textView = (TextView) rootView.findViewById(R.id.org_weight_and_cnt);
        TextView weightAndCntView = (TextView) rootView.findViewById(R.id.org_weight_and_cnt_view);
        if (mWorkCommand.measured_by_weight()) {
            weightAndCntView.setText(R.string.org_weight);
            textView.setText(String.format("%d 千克", mWorkCommand.getOrgWeight()));
        } else {
            weightAndCntView.setText(R.string.org_weight_and_cnt);
            textView.setText(String.format("%d千克/%d%s", mWorkCommand.getOrgWeight(), mWorkCommand.getOrgCnt(), mWorkCommand.getUnit()));
        }
    }

    private void _setPic() {
        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.image);
        new GetImageTask(imageButton, mWorkCommand.getPicPath()).execute();
    }

    private void _setProcedureAndPreviousProcedure() {
        TextView textView = (TextView) rootView.findViewById(R.id.procedure_and_previous);
        textView.setText(String.format("%s(%s)", mWorkCommand.getProcedure(),
                Utils.isEmptyString(mWorkCommand.getPreviousProcedure()) ? "  " : mWorkCommand.getPreviousProcedure()));
    }

    private void _setProcessedWeightAndCnt() {
        TextView textView = (TextView) rootView.findViewById(R.id.processed_weight_and_cnt);
        TextView weightAndCntView = (TextView) rootView.findViewById(R.id.processed_weight_and_cnt_row);
        if (mWorkCommand.measured_by_weight()) {
            weightAndCntView.setText(R.string.processed_weight);
            textView.setText(String.format("%d千克", mWorkCommand.getProcessedWeight()));
        } else {
            weightAndCntView.setText(R.string.processed_weight_and_cnt);
            textView.setText(String.format("%d千克/%d%s", mWorkCommand.getProcessedWeight(), mWorkCommand.getProcessedCnt(), mWorkCommand.getUnit()));
        }
    }

    private void _setProductAndSpecType() {
        TextView textView = (TextView) rootView.findViewById(R.id.product_name_and_spec_type);
        textView.setText(String.format("%s(%s-%s)", mWorkCommand.getProductName(),
                Utils.isEmptyString(mWorkCommand.getSpec()) ? "  " : mWorkCommand.getSpec(),
                Utils.isEmptyString(mWorkCommand.getType()) ? "  " : mWorkCommand.getType()));
    }

    private void _setTeamLeaderMenu(Menu menu, MenuInflater inflater) {
        if (mWorkCommand.getStatus() == Constants.STATUS_ENDING) {
            inflater.inflate(R.menu.team_leader_work_command_menu, menu);
        }
    }

    private void _setTechReq() {
        TextView textView = (TextView) rootView.findViewById(R.id.tech_req);
        textView.setText(mWorkCommand.getTechReq());
    }

}
