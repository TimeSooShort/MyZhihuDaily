package com.miao.android.myzhihudaily.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.miao.android.myzhihudaily.R;
import com.miao.android.myzhihudaily.bean.HotPost;
import com.miao.android.myzhihudaily.interfaces.OnRecyclerViewOnClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/9.
 */
public class HotPostAdapter extends RecyclerView.Adapter<HotPostAdapter.HotPostViewHolder> {

    private List<HotPost> mList = new ArrayList<HotPost>();
    private final Context mContext;
    private final LayoutInflater mInflater;

    private OnRecyclerViewOnClickListener mListener;

    public HotPostAdapter(Context context, List<HotPost> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public HotPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.universal_item_layout, parent, false);
        return new HotPostViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(HotPostViewHolder holder, int position) {
        HotPost hotPost = mList.get(position);
        if (hotPost.getThumbnail() == null) {
            holder.ivThumbnail.setImageResource(R.drawable.no_img);
        }else {
            Glide.with(mContext)
                    .load(hotPost.getThumbnail())
                    .centerCrop()
                    .into(holder.ivThumbnail);
        }
        holder.tvTitle.setText(hotPost.getTitle());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItemClickListener(OnRecyclerViewOnClickListener listener) {
        //注意看清这里，将对象传递给开头声明的那个mListener
        this.mListener = listener;
    }

    public class HotPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView ivThumbnail;
        private TextView tvTitle;
        private OnRecyclerViewOnClickListener listener;

        public HotPostViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
            super(itemView);
            this.listener = listener;
            ivThumbnail = (ImageView) itemView.findViewById(R.id.universal_item_iv);
            tvTitle = (TextView) itemView.findViewById(R.id.universal_item_tv_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.OnItemClick(view, getLayoutPosition());
            }
        }
    }
}
