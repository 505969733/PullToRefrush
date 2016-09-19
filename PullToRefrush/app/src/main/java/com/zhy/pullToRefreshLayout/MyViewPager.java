package com.zhy.pullToRefreshLayout;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/6/2.
 */
public class MyViewPager extends ViewPager {

    private ViewGroup parent;
    private float mDownX;
    private float mDownY;

    public MyViewPager(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNestedpParent(ViewGroup parent) {
        this.parent = parent;
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(true);
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent arg0) {
//        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(true);
//        }
//        return super.onInterceptTouchEvent(arg0);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent arg0) {
//        if (parent != null) {
//            parent.requestDisallowInterceptTouchEvent(true);
//        }
//        return super.onTouchEvent(arg0);
//    }


    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = Math.abs(event.getX()-mDownX);
                float y =Math.abs(event.getY()-mDownY);
                if (x>y){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchGenericMotionEvent(event);
    }
}