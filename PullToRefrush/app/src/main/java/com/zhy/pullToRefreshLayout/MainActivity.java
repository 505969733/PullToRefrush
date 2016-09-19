package com.zhy.pullToRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fullscreenlibs.AndroidBug5497Workaround;
import com.fullscreenlibs.SwipeBackActivity;
import com.fullscreenlibs.SwipeBackLayout;
import com.zhy.pullToRefreshLayout.pullableview.PullToRefreshLayout;

import java.util.ArrayList;

public class MainActivity extends SwipeBackActivity implements View.OnClickListener, AndroidBug5497Workaround.OnInputMethodManagerLinstener {

    private ListView listView;
    private Button btn;
    private DrawerLayout root;
    private TextView tvKey;
    private MainActivity context;
    private Button btn_send;
    private ArrayList<String> items;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        context = this;
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEnableGesture(false);
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                Log.e("TestActivity", "MainActivity ：：state=" + state + ",scrollPercent=" + scrollPercent);
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
            }

            @Override
            public void onScrollOverThreshold() {
            }
        });
    }

    @Override
    public void inputMethodCallBack(int inputHeight, boolean isShow) {
        super.inputMethodCallBack(inputHeight, isShow);
        Log.e("inputHeight", "inputHeight=" + inputHeight + ",isShow=" + isShow);
        if (isShow) {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPosition(Integer.MAX_VALUE, 100);
                }
            });
        }
    }


    private void initView() {

        PullToRefreshLayout layout = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
        // 是否需要  下拉刷新  true 需要   false 不需要
        layout.setOnRefresh(false);
        // 是否需要  上拉加载  true 需要   false 不需要
        layout.setOnLoadMore(false);
        // 自动刷新
        layout.setAutoRefresh(false);

        root = (DrawerLayout) findViewById(R.id.root);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.e("onScrollChange", "scrollY=" + firstVisibleItem);
            }
        });
        items = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            items.add("这里是item " + i);
        }
        adapter = new MyAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, "点击=" + position, 100).show();
            }
        });
        tvKey = (TextView) findViewById(R.id.key);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                startActivity(new Intent(MainActivity.this, PullActivity.class));
                break;
            case R.id.btn_send:
                items.add("这里是item " + items.size());
                adapter.notifyDataSetChanged();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.smoothScrollToPosition(Integer.MAX_VALUE, 100);
                    }
                });
                break;
        }
    }
}
