package com.jinheyu.lite_mms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Created by abc549825@163.com on 2013-09-04.
 */
public class DialogFragmentProxy extends DialogFragment {
    DialogProxyListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogProxyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogProxyListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogFragmentProxy.this.getDialog().cancel();
            }
        }).setNeutralButton(mListener.getNeutralButtonId(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogNeutralClick(DialogFragmentProxy.this);
            }
        }).setPositiveButton(mListener.getPositiveButtonId(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogPositiveClick(DialogFragmentProxy.this);
            }
        }).setView(mListener.getDefaultFragmentView()).setTitle(R.string.add_weight);
        return builder.create();
    }


    public interface DialogProxyListener {
        public void onDialogPositiveClick(DialogFragment dialogFragment);

        public void onDialogNeutralClick(DialogFragment dialogFragment);

        public View getDefaultFragmentView();

        public int getNeutralButtonId();

        public int getPositiveButtonId();


    }
}
