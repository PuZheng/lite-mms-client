package com.jinheyu.lite_mms;

import android.app.ListActivity;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Created by xc on 13-9-30.
 */
abstract public class PtrListActivity extends ListActivity implements PullToRefreshAttacher.OnRefreshListener {

    private PullToRefreshAttacher mPullToRefreshAttacher;

    protected void pullToRefreshInit() {
        PullToRefreshAttacher.Options options = new PullToRefreshAttacher.Options();
        options.refreshScrollDistance = 0.2f;
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this, options);
        PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        pullToRefreshLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);
    }

    protected PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }
}
