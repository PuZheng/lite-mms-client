/*
 * Copyright (c) 2013, abc549825@163.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jinheyu.lite_mms;

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
    private String pirUrl;
    private LinearLayout mLayout;
    private ImageView mImageView;

    public ImageFragment() {
    }

    public ImageFragment(String pirUrl) {
        this.pirUrl = pirUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Utils.isEmptyString(pirUrl)) {
            pirUrl = ImageFragment.this.getActivity().getIntent().getStringExtra("picUrl");
        }
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        mLayout = (LinearLayout) rootView.findViewById(R.id.loading_image);
        mImageView = (ImageView) rootView.findViewById(R.id.image);
        new GetImageTask().execute(pirUrl);
        return rootView;
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
