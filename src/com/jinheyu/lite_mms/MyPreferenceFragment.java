package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by xc on 13-8-16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyPreferenceFragment extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}