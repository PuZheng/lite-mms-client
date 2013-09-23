package com.jinheyu.lite_mms;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class ImageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        String url = getIntent().getStringExtra("imageUrl");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        //TODO
        new GetImageTask(imageView, url).execute();
    }
}
