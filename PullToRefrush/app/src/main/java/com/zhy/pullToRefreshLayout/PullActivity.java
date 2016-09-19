package com.zhy.pullToRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fullscreenlibs.SwipeBackActivity;
import com.zhy.pullToRefreshLayout.pullableview.PullToRefreshLayout;
import com.zhy.pullToRefreshLayout.pullableview.PullableRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 95 on 2016/4/9.
 */
public class PullActivity extends SwipeBackActivity {

    private PullActivity context;
    private PullableRecyclerView recyclerView;
    private List<String> listRecycle = new ArrayList<>();
    private RecyclerViewAdapte adapterRecycle;
    private PullToRefreshLayout layout;
    private Runnable runnable;
    private Handler hander = new Handler();
    private int currentItem;
    private MyViewPager pager;
    private LinearLayout linearTop;
    private int topHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_activity);
        context = this;
        initView();
        listRecycle.clear();
        initData(30);
    }

    private void initData(int size) {
        int n = listRecycle.size();
        for (int i = n; i < n + size; i++) {
            String str = "";
            if (i % 3 == 0) {
                str = "增加数字长度，RecyclerView 是Google推出的最新的 替代ListView、GridView的组件";
            } else if (i % 3 == 1) {
                str = "增加数字长度，RecyclerView 是Google推出的最新的 替代ListView、GridView的组件，" +
                        "RecyclerView是用来显示大量数据的容器，并通过有限数量的子View，来提高滚动时的性能。"
                ;
            }
            listRecycle.add("这是第" + i + "条数据" + str);
        }
        adapterRecycle.refrushData(listRecycle);
    }


    private void initView() {
        linearTop = (LinearLayout) findViewById(R.id.linearTop);
        ViewTreeObserver vto = linearTop.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                linearTop.getViewTreeObserver().removeOnPreDrawListener(this);
                topHeight = linearTop.getHeight();
                Toast.makeText(context, "topHeight=" + topHeight, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        /**
         * 设置头部和底部的颜色
         */
        View header = findViewById(R.id.header);
        header.setBackgroundColor(Color.parseColor("#ff4408"));
        View footer = findViewById(R.id.footer);
        footer.setBackgroundColor(Color.parseColor("#aa11ff"));
        layout = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
        // 是否需要  下拉刷新  true 需要   false 不需要
        layout.setOnRefresh(true);
        // 是否需要  上拉加载  true 需要   false 不需要
        layout.setOnLoadMore(true);

        // 自动刷新
        layout.setAutoRefresh(false);

        layout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                // 千万别忘了告诉控件刷新完毕了哦！
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (layout == null) return;
                        layout.finish(PullToRefreshLayout.FAIL, null);
                        Toast.makeText(context, "刷新完成", Toast.LENGTH_LONG).show();
                        listRecycle.clear();
                        initData(10);
                        layout.setOnLoadMore(true);
                    }
                }, 2500);
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (layout == null) return;
                        // 千万别忘了告诉控件刷新完毕了哦！
                        layout.finish(PullToRefreshLayout.SUCCEED, new PullToRefreshLayout.OnRefresFinish() {
                            @Override
                            public void onFinish() {
                                Toast.makeText(context, "下拉加载完成", Toast.LENGTH_LONG).show();
                                if (listRecycle.size() > 30) {
                                    layout.setOnLoadMore(false, "没有更多数据了");
                                } else {
                                    initData(10);
                                }
                            }
                        });
                    }
                }, 2500);
            }
        });

        recyclerView = (PullableRecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public int count = 0;

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //  doSomething
//                 doSomeThing();
                switch (recyclerView.getStyle()) {
                    case PullableRecyclerView.ListView:
                    case PullableRecyclerView.GridView:
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        count = lm.getItemCount();
                        if (count < 0) {
                            linearTop.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (lm.findViewByPosition(0) != null &&
                                lm.findViewByPosition(0).getVisibility() == View.VISIBLE) {
                            // 当前的item高度
                            int itmeHeight = lm.findViewByPosition(0).getHeight();
                            //当前 item到顶部的距离
                            int top = lm.findViewByPosition(0).getTop();
                            if (Math.abs(top) >= itmeHeight - topHeight) {
                                if (!linearTop.isShown()) {
                                    linearTop.setVisibility(View.VISIBLE);
                                    linearTop.setAlpha(1);
                                    linearTop.setClickable(true);
                                }
                            } else {
                                if (Math.abs(top) > 0) {
                                    linearTop.setVisibility(View.VISIBLE);
                                    linearTop.setClickable(false);
                                    linearTop.setAlpha((float) Math.abs(top)/(itmeHeight - topHeight));
                                } else {
                                    if (linearTop.isShown()) {
                                        linearTop.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                        break;
                    case PullableRecyclerView.Waterfall:
                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                        count = staggeredGridLayoutManager.getItemCount();
                        if (count < 0) {
                            linearTop.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (staggeredGridLayoutManager.findViewByPosition(0) != null &&
                                staggeredGridLayoutManager.findViewByPosition(0).getVisibility() == View.VISIBLE) {
                            // 当前的item高度
                            int itmeHeight = staggeredGridLayoutManager.findViewByPosition(0).getHeight();
                            //当前 item到顶部的距离
                            int top = staggeredGridLayoutManager.findViewByPosition(0).getTop();
                            if (Math.abs(top) >= itmeHeight - topHeight) {
                                if (!linearTop.isShown()) {
                                    linearTop.setVisibility(View.VISIBLE);
                                    linearTop.setAlpha(1);
                                    linearTop.setClickable(true);
                                }
                            } else {
                                if (Math.abs(top) > 0) {
                                    linearTop.setVisibility(View.VISIBLE);
                                    linearTop.setClickable(false);
                                    linearTop.setAlpha((float) Math.abs(top)/(itmeHeight - topHeight));
                                } else {
                                    if (linearTop.isShown()) {
                                        linearTop.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        // 设置显示样式 必不可少
        recyclerView.setStyle(PullableRecyclerView.Waterfall);
//        recyclerView.addItemDecoration(new RecycleViewDivider
//                (context, LinearLayoutManager.HORIZONTAL, 1,Color.parseColor("#ff0000")));
//        LinearLayoutManager 线性布局管理器      （ListView效果）
//        GridLayoutManager    网格布局管理器    （GridView效果）
//        StaggeredGridLayoutManager  瀑布流管理器
        RecyclerView.LayoutManager manager = null;
        switch (recyclerView.getStyle()) {
            case PullableRecyclerView.ListView:
                LinearLayoutManager manager1 = new LinearLayoutManager(context);
                manager1.setOrientation(LinearLayoutManager.VERTICAL);
                manager = manager1;
                break;
            case PullableRecyclerView.GridView:
                manager = new GridLayoutManager(context, 3);
                break;
            case PullableRecyclerView.Waterfall:
                //3列   方向垂直
                manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                break;
        }

        recyclerView.setLayoutManager(manager);
        adapterRecycle = new RecyclerViewAdapte(context, listRecycle, new RecyclerViewAdapte.OnItemClickListening() {
            @Override
            public void onClick(View view, int position) {
                String str = listRecycle.get(position);
                str = str + "修改";
                adapterRecycle.refrushData(position, str);
            }

            @Override
            public void onSeeMore() {

            }
        });
        View headerView = LayoutInflater.from(this).inflate(R.layout.header, null);
        recyclerView.setAdapter(adapterRecycle);
        View footerView = LayoutInflater.from(this).inflate(R.layout.footer, null);
        adapterRecycle.setHeaderView(headerView);
        adapterRecycle.setFooter(footerView);
        // viewpager
        pager = (MyViewPager) headerView.findViewById(R.id.viewpager);
        pager.setNestedpParent((ViewGroup) pager.getParent());
        final List<Integer> list = new ArrayList<>();
        list.add(R.mipmap.h);
        list.add(R.mipmap.i);
        list.add(R.mipmap.j);
        list.add(R.mipmap.k);
        list.add(R.mipmap.l);
        list.add(R.mipmap.m);
        ViewPagerImagesAdapter pagerAdapter = new ViewPagerImagesAdapter(context, list);
        pager.setAdapter(pagerAdapter);
        currentItem = 0;
        pager.setCurrentItem(currentItem);
        // 定时
        if (runnable != null && hander != null) {
            //停止Timer
            hander.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            public void run() {
                if (hander != null && runnable != null) {
                    currentItem++;
                    if (currentItem == list.size()) {
                        currentItem = 0;
                    }
                    pager.setCurrentItem(currentItem);
                    hander.postDelayed(runnable, 3000);
                }
            }
        };
        // 开始Timer
        hander.postDelayed(runnable, 3000);
    }

    @Override
    public void finish() {
        super.finish();
        if (runnable != null && hander != null) {
            //停止Timer
            hander.removeCallbacks(runnable);
        }
        runnable = null;
        hander = null;
    }
}
