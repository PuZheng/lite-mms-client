package com.jinheyu.lite_mms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class ImageFragment extends Fragment {
    private ImageButton mImageButton;
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
        mImageButton = (ImageButton) rootView.findViewById(R.id.image);
        new GetImageTask(mImageButton, mImageFragmentListener.getFragmentPicUrl()).execute();
        return rootView;
    }

    public static interface ImageFragmentListener {
        public String getFragmentPicUrl();
    }


}
