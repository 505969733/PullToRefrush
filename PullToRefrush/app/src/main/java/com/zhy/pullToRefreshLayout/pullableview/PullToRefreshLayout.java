package com.zhy.pullToRefreshLayout.pullableview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.zhy.pullToRefreshLayout.R;

/**
 * 自定义的布局，用来管理三个子控件，其中一个是下拉头，一个是包含内容的pullableView（可以是实现Pullable接口的的任何View），
 */
public class PullToRefreshLayout extends RelativeLayout {
    public static final String TAG = "PullToRefreshLayout";
    /**
     * last y
     */
    private int mLastMotionY;
    /**
     * last x
     */
    private int mLastMotionX;
    // 初始状态
    public static final int INIT = 0;
    // 释放刷新
    public static final int RELEASE_TO_REFRESH = 1;
    // 正在刷新
    public static final int REFRESHING = 2;
    // 释放加载
    public static final int RELEASE_TO_LOAD = 3;
    // 正在加载
    public static final int LOADING = 4;
    // 操作完毕
    public static final int DONE = 5;
    // 当前状态
    private int state = INIT;
    // 刷新回调接口
    private OnRefreshListener mListener;
    // 刷新成功
    public static final int SUCCEED = 0;
    // 刷新失败
    public static final int FAIL = 1;
    // 按下Y坐标，上一个事件点Y坐标
    private float downY, lastY;

    // 下拉的距离。注意：pullDownY和pullUpY不可能同时不为0
    public float pullDownY = 0;
    // 上拉的距离
    private float pullUpY = 0;

    // 释放刷新的距离
    private float refreshDist = 200;
    // 释放加载的距离
    private float loadmoreDist = 200;

    // 回滚速度
    public float MOVE_SPEED = 8;
    // 第一次执行布局
    private boolean isLayout = false;
    // 在刷新过程中滑动操作
    private boolean isTouch = false;
    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    private float radio = 2;

    // 下拉箭头的转180°动画
    private RotateAnimation rotateAnimation;
    // 均匀旋转动画
    private RotateAnimation refreshingAnimation;

    // 下拉布局
    private View rela_head;
    // 下拉头
    private View refreshView;
    // 下拉的箭头
    private View pullView;
    // 正在刷新的图标
    private View refreshingView;
    // 刷新结果图标
    private View refreshStateImageView;
    // 刷新结果：成功或失败
    private TextView refreshStateTextView;


    // 下拉布局
    private View rela_foot;
    // 上拉头
    private View loadmoreView;
    // 上拉的箭头
    private View pullUpView;
    // 正在加载的图标
    private View loadingView;
    // 加载结果图标
    private View loadStateImageView;
    // 加载结果：成功或失败
    private TextView loadStateTextView;

    // 实现了Pullable接口的View
    private View pullableView;
    // 过滤多点触碰
    private int mEvents;
    // 这两个变量用来控制pull的方向，如果不加控制，当情况满足可上拉又可下拉时没法下拉
    private boolean canPullDown = true;
    private boolean canPullUp = true;

    /**
     * 是否可以下拉刷新
     */
    private boolean canRefrsh = true;
    /**
     * 是否可以加载更多
     */
    private boolean canLoadMore = true;
    /**
     * 自动下拉刷新  默认false
     */
    private boolean autoRefresh = false;
    // 下拉 上啦 偏移一点
    private int offsete = 0;
    private boolean isFrist = false;


    /**
     * 是否显示没有更多加载文字描述
     */
    private boolean isShowLoadMoreMessage = false;
    private String showLoadMoreMessage = "没有更多数据了";
    private TextView tvNomore;
    private View linear;
    /**
     * 判断上啦 下拉的类型  true 下拉刷新   false 上啦加载更多   默认 true
     */
    private boolean TYPE = true;


    private void relauoutRefresh(float y, final OnRefresFinish refreshFinish) {
        float end = 0f;
        if (!isTouch) {
            // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
            if (state == REFRESHING && y > refreshDist) {
                end = refreshDist;
            } else if (state == DONE && y == refreshDist) {
                end = 0;
            }
        }

        long time = 1000;
        if (Math.abs(y) > refreshDist) {
            time = 500;
        } else {
            time = 500;
        }
        ValueAnimator animation = ValueAnimator.ofFloat(y, end);
        animation.setDuration(time);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue());
                if (pullDownY > 0) {
                    pullDownY = value;
                }
                if (pullDownY <= 0) {
                    // 已完成回弹
                    pullDownY = 0;
                    pullView.clearAnimation();
                    // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                    if (state != REFRESHING && state != LOADING) {
                        changeState(INIT);
                    }
                    if (refreshFinish != null) {
                        refreshFinish.onFinish();
                    }
                }
                requestLayout();
            }
        });
        animation.start();
    }

    private void relauoutLoadmore(float y, final OnRefresFinish refreshFinish) {
        float end = 0f;
        if (!isTouch) {
            // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
            if (state == LOADING && -y > loadmoreDist) {
                end = -loadmoreDist;
            } else if (state == DONE) {
                end = 0f;
            }
        }
        long time = 1000;
        if (Math.abs(y) > loadmoreDist) {
            time = 500;
        } else {
            time = 500;
        }
        ValueAnimator animation = ValueAnimator.ofFloat(y, end);
        animation.setDuration(time);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue());
                if (pullUpY < 0) {
                    pullUpY = value;
                }
                if (pullUpY >= 0) {
                    // 已完成回弹
                    pullUpY = 0;
                    pullUpView.clearAnimation();
                    // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                    if (state != REFRESHING && state != LOADING) {
                        changeState(INIT);
                    }
                    if (refreshFinish != null) {
                        refreshFinish.onFinish();
                    }
                }
                requestLayout();
            }
        });
        animation.start();
    }


    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public PullToRefreshLayout(Context context) {
        super(context);
        initView(context);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
//        timer = new MyTimer(updateHandler);
        rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                context, R.anim.reverse_anim);
        refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                context, R.anim.rotating);
        // 添加匀速转动动画
        LinearInterpolator lir = new LinearInterpolator();
        rotateAnimation.setInterpolator(lir);
        refreshingAnimation.setInterpolator(lir);
    }

    private void hide(OnRefresFinish refreshFinish) {
        if (pullDownY > 0) {
            relauoutRefresh(pullDownY, refreshFinish);
        } else if (pullUpY < 0) {
            relauoutLoadmore(pullUpY, refreshFinish);
        }
//        if (pullDownY != 0 || pullUpY != 0) {
//            timer.schedule(per);
//        }
    }

    /**
     * 完成刷新操作，显示刷新结果。注意：刷新完成后一定要调用这个方法
     */
    /**
     * @param refreshResult PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败
     */
    public void refreshFinish(int refreshResult) {
        refreshingView.clearAnimation();
        refreshingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 刷新成功
                refreshStateImageView.setVisibility(View.VISIBLE);
                refreshStateTextView.setText(R.string.refresh_succeed);
                refreshStateImageView
                        .setBackgroundResource(R.mipmap.allview_refresh_succeed);
                break;
            case FAIL:
            default:
                // 刷新失败
                refreshStateImageView.setVisibility(View.VISIBLE);
                refreshStateTextView.setText(R.string.refresh_fail);
                refreshStateImageView.setBackgroundResource(R.mipmap.allview_refresh_failed);
                break;
        }
        // 刷新结果停留1秒

        postDelayed(new Runnable() {
            @Override
            public void run() {
                changeState(DONE);
                hide(null);
            }
        }, 1000);
    }


    /**
     * 完成刷新操作，显示刷新结果。注意：刷新完成后一定要调用这个方法
     */
    /**
     * @param refreshResult PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败
     */
    public void refreshFinish(int refreshResult, final OnRefresFinish refreshFinish) {
        refreshingView.clearAnimation();
        refreshingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 刷新成功
                refreshStateImageView.setVisibility(View.VISIBLE);
                refreshStateTextView.setText(R.string.refresh_succeed);
                refreshStateImageView
                        .setBackgroundResource(R.mipmap.allview_refresh_succeed);
                break;
            case FAIL:
            default:
                // 刷新失败
                refreshStateImageView.setVisibility(View.VISIBLE);
                refreshStateTextView.setText(R.string.refresh_fail);
                refreshStateImageView.setBackgroundResource(R.mipmap.allview_refresh_failed);
                break;
        }
        // 刷新结果停留1秒

        postDelayed(new Runnable() {
            @Override
            public void run() {
                changeState(DONE);
                hide(refreshFinish);
            }
        }, 1000);
    }


    /**
     * 加载完毕，显示加载结果。注意：加载完成后一定要调用这个方法
     *
     * @param refreshResult PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败
     */
    public void loadmoreFinish(int refreshResult) {
        loadingView.clearAnimation();
        loadingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 加载成功
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_succeed);
                loadStateImageView
                        .setBackgroundResource(R.mipmap.allview_load_succeed);
                break;
            case FAIL:
            default:
                // 加载失败
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_fail);
                loadStateImageView
                        .setBackgroundResource(R.mipmap.allview_load_failed);
                break;
        }
        // 刷新结果停留1秒
        postDelayed(new Runnable() {
            @Override
            public void run() {
                changeState(DONE);
                hide(null);
            }
        }, 1000);
    }

    /**
     * 加载更多立即消失
     *
     * @param refreshResult
     * @param refreshFinish
     */
    public void loadmoreFinish(int refreshResult, final OnRefresFinish refreshFinish) {
        loadingView.clearAnimation();
        loadingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 加载成功
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_succeed);
                loadStateImageView.setBackgroundResource(R.mipmap.allview_load_succeed);
                break;
            case FAIL:
            default:
                // 加载失败
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_fail);
                loadStateImageView.setBackgroundResource(R.mipmap.allview_load_failed);
                break;
        }
        // 刷新结果停留1秒
        postDelayed(new Runnable() {
            @Override
            public void run() {
                changeState(DONE);
                hide(refreshFinish);
            }
        }, 1000);
    }


    /**
     * 兼容上啦下拉
     *
     * @param refreshResult
     * @param refreshFinish
     */
    public void finish(int refreshResult, final OnRefresFinish refreshFinish) {
        if (!TYPE) {   // 上啦
            loadingView.clearAnimation();
            loadingView.setVisibility(View.GONE);
            switch (refreshResult) {
                case SUCCEED:
                    // 加载成功
                    loadStateImageView.setVisibility(View.VISIBLE);
                    loadStateTextView.setText(R.string.load_succeed);
                    loadStateImageView.setBackgroundResource(R.mipmap.allview_load_succeed);
                    break;
                case FAIL:
                default:
                    // 加载失败
                    loadStateImageView.setVisibility(View.VISIBLE);
                    loadStateTextView.setText(R.string.load_fail);
                    loadStateImageView.setBackgroundResource(R.mipmap.allview_load_failed);
                    break;
            }
            // 刷新结果停留1秒
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeState(DONE);
                    hide(refreshFinish);
                }
            }, 1000);
        } else {  // 下拉
            refreshingView.clearAnimation();
            refreshingView.setVisibility(View.GONE);
            switch (refreshResult) {
                case SUCCEED:
                    // 刷新成功
                    refreshStateImageView.setVisibility(View.VISIBLE);
                    refreshStateTextView.setText(R.string.refresh_succeed);
                    refreshStateImageView
                            .setBackgroundResource(R.mipmap.allview_refresh_succeed);
                    break;
                case FAIL:
                default:
                    // 刷新失败
                    refreshStateImageView.setVisibility(View.VISIBLE);
                    refreshStateTextView.setText(R.string.refresh_fail);
                    refreshStateImageView.setBackgroundResource(R.mipmap.allview_refresh_failed);
                    break;
            }
            // 刷新结果停留1秒

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeState(DONE);
                    hide(refreshFinish);
                }
            }, 1000);
        }

    }


    /**
     * 加载更多立即消失
     *
     * @param refreshResult
     * @param isShow
     */
    public void loadmoreFinish(int refreshResult, boolean isShow) {
        loadingView.clearAnimation();
        loadingView.setVisibility(View.GONE);
        switch (refreshResult) {
            case SUCCEED:
                // 加载成功
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_succeed);
                loadStateImageView.setBackgroundResource(R.mipmap.allview_load_succeed);
                if (isShow) {
                    changeState(DONE);
                    isShow = true;
                }
                break;
            case FAIL:
            default:
                // 加载失败
                loadStateImageView.setVisibility(View.VISIBLE);
                loadStateTextView.setText(R.string.load_fail);
                loadStateImageView.setBackgroundResource(R.mipmap.allview_load_failed);
                isShow = false;
                break;
        }
        if (isShow) {
            pullUpY = 0;
            requestFocus();
        } else {
            // 刷新结果停留1秒
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeState(DONE);
                    hide(null);
                }
            }, 1000);
        }
    }

    private void changeState(int to) {
        linear.setVisibility(VISIBLE);
        tvNomore.setVisibility(GONE);
        state = to;
        switch (state) {
            case INIT:
                // 下拉布局初始状态
                refreshStateImageView.setVisibility(View.GONE);
                refreshingView.setVisibility(View.GONE);
                refreshingView.clearAnimation();
                refreshStateTextView.setText(R.string.pull_to_refresh);
                pullView.clearAnimation();
                pullView.setVisibility(View.VISIBLE);
                // 上拉布局初始状态
                loadStateImageView.setVisibility(View.GONE);
                loadingView.setVisibility(View.GONE);
                loadingView.clearAnimation();
                loadStateTextView.setText(R.string.pullup_to_load);
                pullUpView.clearAnimation();
                pullUpView.setVisibility(View.VISIBLE);
                break;
            case RELEASE_TO_REFRESH:
                // 释放刷新状态
                refreshStateTextView.setText(R.string.release_to_refresh);
                pullView.startAnimation(rotateAnimation);
                break;
            case REFRESHING:
                // 正在刷新状态
                pullView.clearAnimation();
                refreshingView.setVisibility(View.VISIBLE);
                pullView.setVisibility(View.INVISIBLE);
                refreshingView.startAnimation(refreshingAnimation);
                refreshStateTextView.setText(R.string.refreshing);
                break;
            case RELEASE_TO_LOAD:
                // 释放加载状态
                loadStateTextView.setText(R.string.release_to_load);
                pullUpView.startAnimation(rotateAnimation);
                break;
            case LOADING:
                // 正在加载状态
                pullUpView.clearAnimation();
                loadingView.setVisibility(View.VISIBLE);
                pullUpView.setVisibility(View.INVISIBLE);
                loadingView.startAnimation(refreshingAnimation);
                loadStateTextView.setText(R.string.loading);
                break;
            case DONE:
                // 刷新或加载完毕，啥都不做
                break;
        }
    }

    /**
     * 不限制上拉或下拉
     */
    private void releasePull() {
        canPullDown = true;
        canPullUp = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int y = (int) e.getRawY();
        int x = (int) e.getRawX();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 首先拦截down事件,记录y坐标
                mLastMotionY = y;
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 是向下运动< 0是向上运动
                int deltaY = y - mLastMotionY;
                int deltaX = x - mLastMotionX;
                if (Math.abs(deltaX * 3) > Math.abs(deltaY)) {
                    return false;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
    }


    // 滑动距离及坐标
    private float xDistance, yDistance, xLast, yLast;
    private boolean isMove;

    /*
     * （非 Javadoc）由父控件决定是否分发事件，防止事件冲突
     *
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (canRefrsh) {
            rela_head.setVisibility(View.VISIBLE);
        } else {
            rela_head.setVisibility(View.GONE);
        }

        if (canLoadMore) {
            rela_foot.setVisibility(View.VISIBLE);
        } else {
            if (isShowLoadMoreMessage) {
                linear.setVisibility(GONE);
                tvNomore.setVisibility(VISIBLE);
                tvNomore.setText(showLoadMoreMessage);
            } else {
                rela_foot.setVisibility(View.GONE);
            }
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                downY = ev.getY();
                lastY = downY;
                mEvents = 0;
                releasePull();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                // 过滤多点触碰
                mEvents = -1;
                break;
            case MotionEvent.ACTION_MOVE:
     /*防止水平和横向滑动冲突 ///////////////////////////////*/
                float curX = ev.getX();
                float curY = ev.getY();
                float yAdd = Math.abs(curY - yLast);
                float xAdd = Math.abs(curX - xLast);
                xDistance += xAdd;
                yDistance += yAdd;
                xLast = curX;
                yLast = curY;
                if (xAdd > yAdd) {
                    // 事件分发交给父类
                    super.dispatchTouchEvent(ev);
                    return true;
                }
     /*防止水平和横向滑动冲突 ///////////////////////////////*/
                if (mEvents == 0) {
                    if (((Pullable) pullableView).canPullDown() && canPullDown
                            && state != LOADING) {
                        // 可以下拉，正在加载时不能下拉
                        // 对实际滑动距离做缩小，造成用力拉的感觉
                        pullDownY = pullDownY + (ev.getY() - lastY) / radio;
                        if (pullDownY < 0) {
                            pullDownY = 0;
                            canPullDown = false;
                            canPullUp = true;
                        }
                        if (pullDownY > getMeasuredHeight())
                            pullDownY = getMeasuredHeight();
                        if (state == REFRESHING) {
                            // 正在刷新的时候触摸移动
                            isTouch = true;
                        }
                    } else if (((Pullable) pullableView).canPullUp() && canPullUp
                            && state != REFRESHING) {
                        // 可以上拉，正在刷新时不能上拉
                        pullUpY = pullUpY + (ev.getY() - lastY) / radio;
                        if (pullUpY > 0) {
                            pullUpY = 0;
                            canPullDown = true;
                            canPullUp = false;
                        }
                        if (pullUpY < -getMeasuredHeight())
                            pullUpY = -getMeasuredHeight();
                        if (state == LOADING) {
                            // 正在加载的时候触摸移动
                            isTouch = true;
                        }
                    } else
                        releasePull();
                } else
                    mEvents = 0;
                lastY = ev.getY();
                // 根据下拉距离改变比例
                radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight()
                        * (pullDownY + Math.abs(pullUpY))));
                requestLayout();
                if (pullDownY <= refreshDist && state == RELEASE_TO_REFRESH) {
                    // 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
                    if (canRefrsh) {
                        changeState(INIT);
                    }
                }
                if (pullDownY >= refreshDist && state == INIT) {
                    // 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新
                    if (canRefrsh) {
                        changeState(RELEASE_TO_REFRESH);
                    }
                }
                // 下面是判断上拉加载的，同上，注意pullUpY是负值
                if (-pullUpY <= loadmoreDist && state == RELEASE_TO_LOAD) {
                    if (canLoadMore) {
                        changeState(INIT);
                    }
                }
                if (-pullUpY >= loadmoreDist && state == INIT) {
                    if (canLoadMore) {
                        changeState(RELEASE_TO_LOAD);
                    }
                }
                // 因为刷新和加载操作不能同时进行，所以pullDownY和pullUpY不会同时不为0，因此这里用(pullDownY +
                // Math.abs(pullUpY))就可以不对当前状态作区分了
                if ((pullDownY + Math.abs(pullUpY)) > 8) {
                    // 防止下拉过程中误触发长按事件和点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                isMove = false;
                if (pullDownY > refreshDist || -pullUpY > loadmoreDist)
                    // 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏
                    isTouch = false;
                if (state == RELEASE_TO_REFRESH) {
                    if (canRefrsh) {
                        changeState(REFRESHING);
                    }
                    // 刷新操作
                    if (mListener != null) {
                        if (canRefrsh) {
                            TYPE = true;
                            mListener.onRefresh(this);
                        }
                    }
                } else if (state == RELEASE_TO_LOAD) {
                    if (canLoadMore) {
                        changeState(LOADING);
                    }
                    // 加载操作
                    if (mListener != null)
                        if (canLoadMore) {
                            TYPE = false;
                            mListener.onLoadMore(this);
                        }
                }
                hide(null);
            default:
                break;
        }
        // 事件分发交给父类
        super.dispatchTouchEvent(ev);
        return true;
    }


    private void initView() {
        // 初始化下拉布局
        rela_head = refreshView.findViewById(R.id.rela_head);
        pullView = refreshView.findViewById(R.id.pull_icon);
        refreshStateTextView = (TextView) refreshView
                .findViewById(R.id.state_tv);
        refreshingView = refreshView.findViewById(R.id.refreshing_icon);
        refreshStateImageView = refreshView.findViewById(R.id.state_iv);
        // 初始化上拉布局
        rela_foot = loadmoreView.findViewById(R.id.rela_foot);
        pullUpView = loadmoreView.findViewById(R.id.pullup_icon);
        loadStateTextView = (TextView) loadmoreView
                .findViewById(R.id.loadstate_tv);
        loadingView = loadmoreView.findViewById(R.id.loading_icon);
        loadStateImageView = loadmoreView.findViewById(R.id.loadstate_iv);
        tvNomore = (TextView) loadmoreView.findViewById(R.id.tvNomore);
        linear = loadmoreView.findViewById(R.id.linear);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!isLayout) {
            // 这里是第一次进来的时候做一些初始化
            refreshView = getChildAt(0);
            pullableView = getChildAt(1);
            loadmoreView = getChildAt(2);
            isLayout = true;
            initView();
            refreshDist = ((ViewGroup) refreshView).getChildAt(0)
                    .getMeasuredHeight() + offsete;
            loadmoreDist = ((ViewGroup) loadmoreView).getChildAt(0)
                    .getMeasuredHeight() + offsete;
        }
        // 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分
        if (canRefrsh && autoRefresh && !isFrist) {
            pullDownY = refreshDist;
            changeState(REFRESHING);
            isFrist = true;
            // 刷新操作
            if (mListener != null) {
                if (canRefrsh && autoRefresh) {
                    mListener.onRefresh(this);
                }
            }
        }
        refreshView.layout(0,
                (int) (pullDownY + pullUpY) - refreshView.getMeasuredHeight(),
                refreshView.getMeasuredWidth(), (int) (pullDownY + pullUpY));
        pullableView.layout(0, (int) (pullDownY + pullUpY),
                pullableView.getMeasuredWidth(), (int) (pullDownY + pullUpY)
                        + pullableView.getMeasuredHeight());
        loadmoreView.layout(0,
                (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight(),
                loadmoreView.getMeasuredWidth(),
                (int) (pullDownY + pullUpY) + pullableView.getMeasuredHeight()
                        + loadmoreView.getMeasuredHeight());
    }

    /***
     * 是否允许下拉刷新
     *
     * @param abc
     */
    public void setOnRefresh(boolean abc) {
        this.canRefrsh = abc;
    }

    /***
     * 是否允许 上拉加载更多
     *
     * @param abcz
     */
    public void setOnLoadMore(boolean abcz) {
        this.canLoadMore = abcz;
    }

    /**
     * 是否允许 上拉加载更多
     *
     * @param canLoadMore
     * @param str
     */
    public void setOnLoadMore(boolean canLoadMore, String str) {
        this.canLoadMore = canLoadMore;
        if (canLoadMore) {
            isShowLoadMoreMessage = !canLoadMore;
        } else {
            isShowLoadMoreMessage = !canLoadMore;
            showLoadMoreMessage = !TextUtils.isEmpty(str) ? str : showLoadMoreMessage;
        }
    }


    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        if (canRefrsh && autoRefresh) {
            requestLayout();
        }
    }

    /**
     * 刷新加载回调接口
     *
     * @author chenjing
     */
    public interface OnRefreshListener {
        /**
         * 刷新操作
         */
        void onRefresh(PullToRefreshLayout pullToRefreshLayout);

        /**
         * 加载操作
         */
        void onLoadMore(PullToRefreshLayout pullToRefreshLayout);
    }

    /**
     * 加载完成
     */
    public interface OnRefresFinish {

        /**
         * 加载操作
         */
        void onFinish();
    }

}
