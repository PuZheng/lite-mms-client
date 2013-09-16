package com.jinheyu.lite_mms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinheyu.lite_mms.data_structures.Customer;
import com.jinheyu.lite_mms.data_structures.Harbor;
import com.jinheyu.lite_mms.data_structures.UnloadSession;

import java.io.File;

/**
 * Created by xc on 13-8-14.
 */
public class TakeUnloadTaskPicActivity extends FragmentActivity {
    private ImageView imageView;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_unload_task_pic);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (new File(Utils.getUnloadTaskPicUri().getPath()).exists()) {
            Bitmap photo;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            photo = BitmapFactory.decodeFile(Utils.getUnloadTaskPicUri().getPath(), options);
            imageView.setImageBitmap(photo);
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri fileUri = Utils.getUnloadTaskPicUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_unload_done:
                showNoticeDialog(true);
                break;
            case R.id.action_unload_partly:
                showNoticeDialog(false);
                break;
            case R.id.action_take_pic:
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri fileUri = Utils.getUnloadTaskPicUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNoticeDialog(boolean done) {
        UnloadSession unloadSession = getIntent().getParcelableExtra("unloadSession");
        Harbor harbor = getIntent().getParcelableExtra("harbor");
        Customer customer = getIntent().getParcelableExtra("customer");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag(
                "NOTICE_FRAGMENT");
        if (prev != null) {
            ft.remove(prev);
        }

        DialogFragment dialog = new NoticeDialogFragment(unloadSession, harbor, customer, done);
        dialog.show(ft, "NOTICE_FRAGMENT");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap photo;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                photo = BitmapFactory.decodeFile(Utils.getUnloadTaskPicUri().getPath(), options);
                imageView.setImageBitmap(photo);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_unload_task_pic_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class NoticeDialogFragment extends DialogFragment {
        private final UnloadSession unloadSession;
        private final Harbor harbor;
        private final Customer customer;
        private boolean done;

        private NoticeDialogFragment(UnloadSession unloadSession, Harbor harbor, Customer customer, boolean done) {
            this.unloadSession = unloadSession;
            this.harbor = harbor;
            this.customer = customer;
            this.done = done;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("生成卸货任务");
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_create_unload_task, null);
            TextView textViewPlate = (TextView) view.findViewById(R.id.textViewPlate);
            TextView textViewHarbor = (TextView) view.findViewById(R.id.textViewHarbor);
            TextView textViewCustomer = (TextView) view.findViewById(R.id.textViewCustomer);
            TextView textViewResult = (TextView) view.findViewById(R.id.textViewResult);
            textViewPlate.setText(this.unloadSession.getPlate());
            textViewHarbor.setText(this.harbor.getName());
            textViewCustomer.setText(this.customer.getName());
            if (done) {
                textViewResult.setText(getString(R.string.unload_done));
            } else {
                textViewResult.setText("部分卸货");
            }
            builder.setView(view);
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    XProgressableRunnable.Builder builder = new XProgressableRunnable.Builder(TakeUnloadTaskPicActivity.this);
                    builder.msg("创建卸货任务");
                    builder.okMsg("恭喜您！卸货任务成功提交");
                    builder.run(new XProgressableRunnable.XRunnable() {
                        @Override
                        public void run() throws Exception {
                            MyApp.getWebServieHandler().createUnloadTask(unloadSession, harbor, customer, done, Utils.getUnloadTaskPicUri().getPath());
                        }
                    });
                    builder.after(new Runnable() {
                        @Override
                        public void run() {
                            new File(Utils.getUnloadTaskPicUri().getPath()).delete();
                            // ATTENTION, dialog has been CLOSED, so "getActivity()" will return null, and NoticeDialogFragment
                            // is Detached
                            Intent intent = new Intent(TakeUnloadTaskPicActivity.this, LoaderMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            TakeUnloadTaskPicActivity.this.startActivity(intent);
                        }
                    });
                    builder.create().start();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
    }

    private void finishTask() {
        Intent intent = new Intent(TakeUnloadTaskPicActivity.this, LoaderMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}