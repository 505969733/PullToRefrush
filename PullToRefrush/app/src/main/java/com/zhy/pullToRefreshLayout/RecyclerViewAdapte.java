package com.zhy.pullToRefreshLayout;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lq on 2016/2/5.
 * <p/>
 * RecyclerView  的adapter
 */
public class RecyclerViewAdapte extends RecyclerView.Adapter<RecyclerViewAdapte.ViewHolder> {

    private final OnItemClickListening listener;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> list;
    // 头部
    private final int TYPE_HEADER = 0;
    // 底部
    private final int TYPE_FOOTER = 1;
    private View mHeaderView;
    private View mFooterView;

    public RecyclerViewAdapte(Context context, List<String> _list, OnItemClickListening onItemClickListening) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        listener = onItemClickListening;
        list = _list;
    }

    public void refrushData(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 刷新单个item
     *
     * @param position
     */
    public void refrushData(int position) {
        notifyItemChanged(position);
    }

    public void refrushData(int position, String str) {
        list.set(position, str);
        notifyItemChanged(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFooter;
//        private TextView tvHeader;
        private TextView tv;

        public ViewHolder(View view, int type) {
            super(view);
            switch (type) {
                case TYPE_HEADER:
//                    tvHeader = (TextView) view.findViewById(R.id.tvHeader);
                    break;
                case TYPE_FOOTER:
                    tvFooter = (TextView) view.findViewById(R.id.tvHeader);
                    break;
                default:
                    tv = (TextView) view.findViewById(R.id.tv);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

        if (mHeaderView != null && type == TYPE_HEADER) {
            return new ViewHolder(mHeaderView, type);
        }

        if (mFooterView != null && type == TYPE_FOOTER) {
            return new ViewHolder(mFooterView, type);
        }

        View view = mInflater.inflate(R.layout.recycle_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view, type);
        return viewHolder;
    }

    /**
     * 处理  控件数据
     *
     * @param position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.e("onBindViewHolder","position="+position);
        if (getItemViewType(position) == TYPE_HEADER) {
//            holder.tvHeader.setText("我是头部?000000000000000000000000000000000000000000000000000000000000000000");
        } else if (getItemViewType(position) == TYPE_FOOTER) {
            holder.tvFooter.setText("我是底部？11111111111111111111111111111111111111111111111111111111111111111");
        } else {
            holder.tv.setText(list.get(position));
            holder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) return;
                    listener.onClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == list.size() - 1) {
            return TYPE_FOOTER;
        }
        return -1;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public void setFooter(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(list.size());
    }


    // 添加headerview  和footerview
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {  //  gridview
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (getItemViewType(position)) {
                        case TYPE_HEADER:
                        case TYPE_FOOTER:
                            return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }


    /**
     * 专为瀑布流处理header的问题
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == 0 && mHeaderView != null) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        } else if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && holder.getLayoutPosition() == list.size() - 1 && mFooterView != null) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }


    /*点击回调接口*/
    public interface OnItemClickListening {
        void onClick(View view, int position);

        /**
         * 查看更多
         */
        void onSeeMore();
    }
}

