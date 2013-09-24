package com.jinheyu.lite_mms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by <a href='https://github.com/abc549825'>abc549825@163.com</a> at 09-23.
 */
public class GetImageTask extends AsyncTask<Void, Void, Bitmap> {
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    private static final int cacheSize = maxMemory / 8;
    private static final LruCache<String, Bitmap> BITMAP_LRU_CACHE = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };
    private Exception ex;
    private String mKey;
    private ImageView mImageView;

    public GetImageTask(ImageView imageView, String key) {
        this.mImageView = imageView;
        this.mKey = key;
    }

    public void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            BITMAP_LRU_CACHE.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return BITMAP_LRU_CACHE.get(key);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        mImageView.setImageResource(android.R.drawable.progress_horizontal);
        try {
            Bitmap bitmap = getBitmapFromMemCache(mKey);
            if (bitmap == null) {
                bitmap = MyApp.getWebServieHandler().getImageFromUrl(mKey);
            }
            return bitmap;
        } catch (Exception e) {
            ex = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (ex == null) {
            addBitmapToMemCache(mKey, bitmap);
            mImageView.setImageBitmap(bitmap);
            if (mImageView instanceof ImageButton) {
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mKey == null) {
                            return;
                        }
                        Intent intent = new Intent(mImageView.getContext(), ImageActivity.class);
                        intent.putExtra("imageUrl", mKey);
                        mImageView.getContext().startActivity(intent);
                    }
                });
            }
        } else {
            Toast.makeText(mImageView.getContext(), R.string.load_failure, Toast.LENGTH_SHORT).show();
            mImageView.setImageResource(R.drawable.broken_image);
        }
    }
}
