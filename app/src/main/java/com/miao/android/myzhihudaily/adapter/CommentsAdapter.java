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
import com.miao.android.myzhihudaily.bean.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/9/4.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private final Context mContext;
    private List<Comment> mCommentList;
    private final LayoutInflater inflater;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public CommentsAdapter(Context context, List<Comment> list) {
        mContext = context;
        mCommentList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        Comment comment = mCommentList.get(position);

        Glide.with(mContext)
                .load(comment.getAvatarUrl())
                .asBitmap()
                .into(holder.ivAvatar);

        holder.tvComment.setText(comment.getComment());
        holder.tvAuthor.setText(comment.getAuthor());

        Date date = new Date(Long.valueOf(comment.getTime())*1000);
        holder.tvTime.setText(format.format(date));
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView tvAuthor;
        private TextView tvComment;
        private TextView tvTime;


        public CommentsViewHolder(View itemView) {
            super(itemView);

            ivAvatar = (ImageView) itemView.findViewById(R.id.avatar);
            tvAuthor = (TextView) itemView.findViewById(R.id.tv_author);
            tvComment = (TextView) itemView.findViewById(R.id.tv_comment);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
        }
    }
}
