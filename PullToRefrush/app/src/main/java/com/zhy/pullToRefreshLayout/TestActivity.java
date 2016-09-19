package com.zhy.pullToRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fullscreenlibs.SwipeBackActivity;
import com.fullscreenlibs.SwipeBackLayout;
import com.zhy.pullToRefreshLayout.pullableview.PullToRefreshLayout;

/**
 * Created by 95 on 2016/4/9.
 */
public class TestActivity extends SwipeBackActivity {

    private RadioGroup mTrackingModeGroup;
    private SwipeBackLayout mSwipeBackLayout;
    private TestActivity context;
    private TextView tvKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test1_activity);
        context = this;
        initView();
    }


    private void initView() {


        PullToRefreshLayout layout = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
        // 是否需要  下拉刷新  true 需要   false 不需要
        layout.setOnRefresh(false);
        // 是否需要  上拉加载  true 需要   false 不需要
        layout.setOnLoadMore(false);


        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                Log.e("TestActivity", "TestActivity ：：：state=" + state + ",scrollPercent=" + scrollPercent);
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {

            }

            @Override
            public void onScrollOverThreshold() {

            }
        });

        tvKey = (TextView) findViewById(R.id.key);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, TestActivity.class));
            }
        });
        findViewById(R.id.btn_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToFinishActivity();
            }
        });
        mTrackingModeGroup = (RadioGroup) findViewById(R.id.tracking_mode);
        mTrackingModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int edgeFlag = SwipeBackLayout.EDGE_LEFT;
                switch (checkedId) {
                    case R.id.mode_left:
                        edgeFlag = SwipeBackLayout.EDGE_LEFT;
                        break;
                    case R.id.mode_right:
                        edgeFlag = SwipeBackLayout.EDGE_RIGHT;
                        break;
                    case R.id.mode_bottom:
                        edgeFlag = SwipeBackLayout.EDGE_BOTTOM;
                        break;
                    case R.id.mode_all:
                        edgeFlag = SwipeBackLayout.EDGE_ALL;
                        break;
                    default:
                        edgeFlag = SwipeBackLayout.EDGE_LEFT;
                }
                mSwipeBackLayout.setEdgeTrackingEnabled(edgeFlag);
                mSwipeBackLayout.setEdgeSize(20);
                mSwipeBackLayout.setEnableGesture(true);
            }
        });
    }
}
