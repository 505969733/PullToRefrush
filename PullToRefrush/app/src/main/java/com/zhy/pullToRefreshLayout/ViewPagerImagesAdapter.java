package com.zhy.pullToRefreshLayout;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

/***
 * banner 的 滑动控件adapter
 */
public class ViewPagerImagesAdapter extends PagerAdapter {

    private List<Integer> list;
    private Context context;
    private String title;

    public ViewPagerImagesAdapter(Context context, List<Integer> list) {
        this.list = list;
        this.context = context;
    }

    public ViewPagerImagesAdapter(Context context, List<Integer> list, String title) {
        this.list = list;
        this.context = context;
        this.title = title;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final int banner = list.get(position);
        ImageView imageView = new ImageView(context);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"图片="+position,Toast.LENGTH_SHORT).show();
            }
        });
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(banner);
        container.addView(imageView);
        return imageView;
    }

}