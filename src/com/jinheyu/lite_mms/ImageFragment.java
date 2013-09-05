package com.jinheyu.lite_mms;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

/**
 * Created by abc549825@163.com on 2013-09-03.
 */
public class ImageFragment extends Fragment {
    private LinearLayout mLayout;
    private ImageView mImageView;
    private ImageFragmentListener mImageFragmentListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mImageFragmentListener = (ImageFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ImageFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        mLayout = (LinearLayout) rootView.findViewById(R.id.loading_image);
        mImageView = (ImageView) rootView.findViewById(R.id.image);
        new GetImageTask().execute(mImageFragmentListener.getFragmentPicUrl());
        return rootView;
    }

    public static interface ImageFragmentListener {
        public String getFragmentPicUrl();
    }

    public class GetImageTask extends AsyncTask<String, Void, Bitmap> {
        private Exception ex;

        @Override
        protected Bitmap doInBackground(String... params) {
            if (params.length > 0) {
                String url = params[0];
                try {
                    return MyApp.getWebServieHandler().getImageFromUrl(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    ex = e;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (ex == null) {
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.broken_image));
            }
            mLayout.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

}
