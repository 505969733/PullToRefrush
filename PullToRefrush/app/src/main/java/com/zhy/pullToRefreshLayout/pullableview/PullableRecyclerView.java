package com.zhy.pullToRefreshLayout.pullableview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

public class PullableRecyclerView extends RecyclerView implements Pullable {

    // 当作listView 使用
    public static final int ListView = 0;
    // GridView 使用
    public static final int GridView = 1;
    // 瀑布流 使用
    public static final int Waterfall = 2;
    private int Style = ListView;

    public PullableRecyclerView(Context context) {
        super(context);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setStyle(int style) {
        Style = style;
    }

    public int getStyle() {
        return Style;
    }

    @Override
    public boolean canPullDown() {  // 是否允许下拉刷新
        int count = 0;
        switch (Style) {
            case ListView:
            case GridView:
                LinearLayoutManager lm = (LinearLayoutManager) getLayoutManager();
                count = lm.getItemCount();
                if (count == 0) {
                    // 没有item的时候也可以下拉刷新
                    return true;
                } else if (lm.findFirstVisibleItemPosition() > 0) {
                    return false;
                } else if (lm.findViewByPosition(0) != null &&
                        lm.findViewByPosition(0).getVisibility() == View.VISIBLE &&
                        lm.findViewByPosition(0).getTop() >= 0) {
                    // 滑到ListView的顶部了
                    return true;
                } else
                    return false;
            case Waterfall:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) getLayoutManager();
                count = staggeredGridLayoutManager.getItemCount();
                if (count == 0) {
                    // 没有item的时候也可以下拉刷新
                    return true;
                } else if (staggeredGridLayoutManager.findViewByPosition(0) != null &&
                        staggeredGridLayoutManager.findViewByPosition(0).getVisibility() == View.VISIBLE &&
                        staggeredGridLayoutManager.findViewByPosition(0).getTop() >= 0) {
                    // 滑到ListView的顶部了
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }

    @Override
    public boolean canPullUp() {  // 是否允许上拉刷新
        int count = 0;
        switch (Style) {
            case ListView:
            case GridView:
                LinearLayoutManager lm = (LinearLayoutManager) getLayoutManager();
                count = lm.getItemCount();
                if (count <= 0) {
                    // 没有item的时候也可以上拉加载
                    return true;
                } else if (lm.findLastVisibleItemPosition() < count - 1) {
                    // 当前可见不是最后一个,没滑到底部了
                    return false;
                } else if (lm.findViewByPosition(count - 1) != null &&
                        lm.findViewByPosition(count - 1).getVisibility() == View.VISIBLE) {
                    int a = lm.findViewByPosition(count - 1).getBottom();
                    int b = lm.findViewByPosition(count - 1).getHeight();
                    int c = lm.getHeight();
                    if (c >= a) {
                        // 滑到底部了
                        return true;
                    } else {
                        return false;
                    }
                }
            case Waterfall:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) getLayoutManager();
                count = staggeredGridLayoutManager.getItemCount();
                if (count <= 0) {
                    // 没有item的时候也可以上拉加载
                    return true;
                } else if (staggeredGridLayoutManager.findViewByPosition(count - 1) != null &&
                        staggeredGridLayoutManager.findViewByPosition(count - 1).getVisibility() == View.VISIBLE) {
                    // 获取到最后一个item
                    int spanCount = staggeredGridLayoutManager.getSpanCount();
                    boolean isBottom = false;
                    for (int i = 0; i < spanCount; i++) {
                        isBottom = checkPositionIsBottom(staggeredGridLayoutManager, count - 1 - i);
                        if (!isBottom) {
                            return isBottom;
                        }
                    }
                    return isBottom;
                }
        }
        return false;
    }

    private boolean checkPositionIsBottom(StaggeredGridLayoutManager manager, int postion) {
        View view = manager.findViewByPosition(postion);
        if (view==null)return true;
        int a = view.getBottom();
        int b = view.getHeight();
        int c = manager.getHeight();
        if (c >= a) {
            // 滑到底部了
            return true;
        } else {
            return false;
        }
    }
}
