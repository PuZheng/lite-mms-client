package com.jinheyu.lite_mms;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class XProgressableRunnable {

    private final static String ERROR = "错误";
    private final static String UNKNOWN_ERROR = "未知错误";
    private final static String I_SEE = "我知道了";

    public interface XRunnable {
        public void run() throws Exception;
    }

    public interface ExceptionHandler {
        public void run(Exception e);
    }

    private XRunnable mRunnable = null;
    private CharSequence mMsg = null;
    private Runnable mAfter = null;
    private ExceptionHandler mExceptionHandler = null;
    private Context mContext = null;
    private Exception mException;
    private MessageGenerator messageGenerator;

    public interface MessageGenerator {
        public String genMessage();
    }

    static public class Builder {


        private CharSequence _mMsg = null;
        private String _mOkMsg;
        private Runnable _mAfter;
        private Context _mContext;
        private XRunnable _mRunnable;
        private ExceptionHandler _mExceptionHandler;
        private MessageGenerator messageGenerator;

        public Builder(Context context) {
            _mContext = context;
            messageGenerator = new MessageGenerator() {

                @Override
                public String genMessage() {
                    return _mOkMsg;
                }
            };
        }

        public Builder run(XRunnable runnable) {
            _mRunnable = runnable;
            return this;
        }

        public Builder msg(String msg) {
            _mMsg = msg;
            return this;
        }

        public Builder okMsg(String okMsg) {
            _mOkMsg = okMsg;
            return this;
        }

        public Builder after(Runnable after) {
            _mAfter = after;
            return this;
        }

        public Builder exceptionHandler(ExceptionHandler exceptionHandler) {
            _mExceptionHandler = exceptionHandler;
            return this;
        }

        public Builder setOkMessageGenerator(MessageGenerator messageGenerator) {
            this.messageGenerator = messageGenerator;
            return this;
        }

        public XProgressableRunnable create() {
            return new XProgressableRunnable(_mRunnable, _mMsg, this.messageGenerator, _mAfter,
                    _mExceptionHandler, _mContext);
        }

    }

    XProgressableRunnable(XRunnable runnable, CharSequence msg, MessageGenerator messageGenerator, Runnable after,
                          ExceptionHandler exceptionHandler, Context context) {
        mRunnable = runnable;
        mMsg = msg;
        this.messageGenerator = messageGenerator;
        mContext = context;
        mAfter = after;
        mExceptionHandler = exceptionHandler;
    }

    public void start() {

        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                new Thread() {

                    public void run() {
                        try {
                            mRunnable.run();
                        } catch (Exception e) {
                            mException = e;
                        }
                        pd.cancel();
                    }
                }.start();
            }
        });
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                final AlertDialog ad = new AlertDialog.Builder(mContext).create();
                if (mException == null) { // success
                    ad.setMessage(XProgressableRunnable.this.messageGenerator.genMessage());
                    ad.setOnShowListener(new DialogInterface.OnShowListener() {

                        public void onShow(DialogInterface dialog) {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    ad.cancel();
                                }

                            }, 1000);
                        }
                    });
                    ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            if (mAfter != null) {
                                mAfter.run();
                            }
                        }
                    });
                } else {
                    String errMsg = mException.getMessage();
                    if (errMsg.isEmpty()) {
                        errMsg = UNKNOWN_ERROR;
                    }
                    ad.setMessage(errMsg);
                    ad.setTitle(ERROR);
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, I_SEE, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ad.dismiss();
                            if (mExceptionHandler != null) {
                                mExceptionHandler.run(mException);
                            }
                        }
                    });
                }
                ad.show();
            }
        });
        pd.setMessage(mMsg);
        pd.show();
    }
}
