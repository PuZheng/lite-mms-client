package com.jinheyu.lite_mms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.jinheyu.lite_mms.netutils.ImageCache;

/**
 * Created by <a href='https://github.com/abc549825'>abc549825@163.com</a> at 09-23.
 */
public class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

    private Exception ex;
    private String mKey;
    private String mUrl;
    private boolean mShowToast;
    private ImageView mImageView;
    private ImageCache mImageCache;


    public GetImageTask(ImageView imageView, String url) {
        this(imageView, url, true);
    }

    public GetImageTask(ImageView imageView, String url, boolean showToast) {
        this.mImageView = imageView;
        this.mUrl = url;
        this.mKey = generateKeyFromPicUrl(url);
        this.mShowToast = showToast;
        mImageCache = ImageCache.getInstance(mImageView.getContext());
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (Utils.isEmptyString(mUrl)) {
            return null;
        }
        try {
            Bitmap bitmap = mImageCache.getBitmapFromDiskCache(mKey);
            if (bitmap == null) {
                bitmap = MyApp.getWebServieHandler().getImageFromUrl(mUrl);
            }
            return bitmap;
        } catch (Exception e) {
            ex = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (ex == null && bitmap != null) {
            mImageCache.addBitmapToCache(mKey, bitmap);
            mImageView.setImageBitmap(bitmap);
            if (mImageView instanceof ImageButton) {
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mImageView.getContext(), ImageActivity.class);
                        intent.putExtra("imageUrl", mKey);
                        mImageView.getContext().startActivity(intent);
                    }
                });
            }
        } else {
            if (!Utils.isEmptyString(mKey)) {
                mImageView.setImageResource(R.drawable.broken_image);
                if (mShowToast) {
                    Toast.makeText(mImageView.getContext(), R.string.load_failure, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String generateKeyFromPicUrl(String url) {
        if (Utils.isEmptyString(url)) {
            return url;
        }
        String[] strs = url.split("/");
        String key = strs[strs.length - 1];
        if (key.contains(".")) {
            key = key.replace(".", "_");
        }
        return key;
    }
}
