package com.jinheyu.lite_mms.netutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import com.jakewharton.disklrucache.DiskLruCache;
import com.jinheyu.lite_mms.Utils;

import java.io.*;

/**
 * Created by abc549825@163.com(https://github.com/abc549825) at 09-26.
 */
public class ImageCache {
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private static final int VALUE_COUNT = 1;
    private static final int APP_VERSION = 1;
    private static final Object mDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    private static final String TAG = "DiskLruImageCache";
    private static ImageCache instance;
    private static DiskLruCache mDiskLruCache;
    private static boolean mDiskCacheStarting = true;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 100;

    private ImageCache(Context context) {
        File diskCacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(diskCacheDir);
    }

    public static ImageCache getInstance(Context context) {
        if (instance == null) {
            instance = new ImageCache(context);
        }
        return instance;
    }

    public static void initialize(Context context) {
        ImageCache.getInstance(context);
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        synchronized (mDiskCacheLock) {
            if (containsKey(key)) {
                return;
            }
            DiskLruCache.Editor editor = null;
            try {
                editor = mDiskLruCache.edit(key);
                if (editor == null) {
                    return;
                }
                if (writeBitmapToFile(bitmap, editor)) {
                    mDiskLruCache.flush();
                    editor.commit();
                } else {
                    editor.abort();
                }
            } catch (IOException e) {
                try {
                    if (editor != null) {
                        editor.abort();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    public boolean containsKey(String key) {
        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get(key);
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return contained;
    }

    public Bitmap getBitmapFromDiskCache(String key) throws IOException {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                return getBitmap(key);
            }
        }
        return null;
    }

    public File getCacheFolder() {
        return mDiskLruCache.getDirectory();
    }

    private Bitmap getBitmap(String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskLruCache.get(key);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(DISK_CACHE_INDEX);
            if (in != null) {
                final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return bitmap;

    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Utils.isExternalStorageRemovable() ?
                        Utils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(DISK_CACHE_INDEX), IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                if (mDiskCacheStarting) {
                    File cacheDir = params[0];
                    try {
                        mDiskLruCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, ImageCache.DISK_CACHE_SIZE);
                        mDiskCacheStarting = false; // Finished initialization
                        mDiskCacheLock.notifyAll(); // Wake any waiting threads
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
