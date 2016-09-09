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
import com.miao.android.myzhihudaily.bean.LatestPost;
import com.miao.android.myzhihudaily.interfaces.OnRecyclerViewOnClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/29.
 */
public class LatestPostAdapter extends RecyclerView.Adapter<LatestPostAdapter.LatestItemViewHolder> {

    private final Context mContext;
    //private final LayoutInflater mInflater;
    private List<LatestPost> mList = new ArrayList<LatestPost>();
    private OnRecyclerViewOnClickListener listener;

    public LatestPostAdapter(Context context, List<LatestPost> list) {
        mContext = context;
        mList = list;
        //mInflater = LayoutInflater.from(context);
    }

    @Override
    public LatestItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.universal_item_layout, parent, false);
        return new LatestItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(LatestItemViewHolder holder, int position) {

        LatestPost item = mList.get(position);
        if (item.getFirstImg() == null) {
            holder.itemImg.setImageResource(R.drawable.no_img);
        }else {
            Glide.with(mContext)
                    .load(item.getFirstImg())
                    .error(R.drawable.no_img)
                    .centerCrop()
                    .into(holder.itemImg);
        }
        holder.tvLatestNewsTitle.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItemClickListener(OnRecyclerViewOnClickListener listener){
        this.listener = listener;
    }

    public class LatestItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tvLatestNewsTitle;
        private ImageView itemImg;
        private OnRecyclerViewOnClickListener mListener;

        public LatestItemViewHolder(View itemView, OnRecyclerViewOnClickListener listener) {
            super(itemView);
            mListener = listener;
            itemImg = (ImageView) itemView.findViewById(R.id.universal_item_iv);
            tvLatestNewsTitle = (TextView) itemView.findViewById(R.id.universal_item_tv_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.OnItemClick(view, getLayoutPosition());
            }
        }
    }
}
