package com.moonface.collabocode;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final Activity context;
    private final boolean replies;
    private List<Post> data;
    private String teamId;
    private OnItemLongClickListener onItemLongClickListener;
    private static final String CODE = "code";


    class ViewHolder extends RecyclerView.ViewHolder {
        View block;
        ImageView iconView;
        TextView nameView, dateView, titleView, contentView, repliesView;
        RecyclerView codesView;
        ViewHolder(View v) {
            super(v);
            block = v.findViewById(R.id.card);
            iconView = v.findViewById(R.id.icon_view);
            nameView = v.findViewById(R.id.name_view);
            dateView = v.findViewById(R.id.date_view);
            titleView = v.findViewById(R.id.title_view);
            contentView = v.findViewById(R.id.content_view);
            codesView = v.findViewById(R.id.codes_view);
            repliesView = v.findViewById(R.id.replies_view);
        }
    }

    PostsAdapter(List<Post> data, boolean replies, String teamId, Activity context) {
        this.data = data;
        this.teamId = teamId;
        this.replies = replies;
        this.context = context;
    }

    void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(replies ? R.layout.reply_item : R.layout.post_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseFirestore.getInstance().collection("users").document(data.get(position).getUserId()).get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                User sender = task.getResult().toObject(User.class);
                holder.nameView.setText(sender.getName());
                Glide.with(context)
                        .load(sender.getProfilePicUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.iconView);
            }
        });
        holder.dateView.setText(DateFormat.getDateTimeInstance().format(data.get(position).getDateSent().toDate()));
        holder.titleView.setText(data.get(position).getTitle());
        holder.contentView.setText(data.get(position).getContent());
        holder.codesView.setLayoutManager(new LinearLayoutManager(context));
        CodesAdapter adapter = new CodesAdapter(data.get(position).getCodes());
        adapter.setOnItemClickListener((code, position1) -> context.startActivity(new Intent().setClass(context, CodeViewerActivity.class).putExtra(CODE, code.getContent())));
        holder.codesView.setAdapter(adapter);
        if (!replies) {
            holder.repliesView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(holder.block.getLayoutParams());
            params.setMargins(4, 4, 4, 4);
            holder.block.setLayoutParams(params);
            if(data.get(position).getId() != null) {
                FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("posts").document(data.get(position).getId()).collection("replies").get().addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        int size = task.getResult().toObjects(Post.class).size();
                        if (size > 0) {
                            holder.repliesView.setVisibility(View.VISIBLE);
                            if (size > 1) {
                                holder.repliesView.setText(context.getString(R.string.replies_label, size));
                            } else {
                                holder.repliesView.setText(context.getString(R.string.one_reply_label));
                            }
                            params.setMargins(4, 4, 4, 0);
                            holder.block.setLayoutParams(params);
                            holder.repliesView.setOnClickListener(v -> {
                                RepliesDialog repliesDialog = new RepliesDialog();
                                repliesDialog.setTeamId(teamId);
                                repliesDialog.setPostId(data.get(position).getId());
                                FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                                repliesDialog.show(ft, RepliesDialog.class.getName());

                            });
                        } else {
                            holder.repliesView.setVisibility(View.GONE);
                            params.setMargins(4, 4, 4, 4);
                            holder.block.setLayoutParams(params);
                        }
                    }
                });
            }
        }

        holder.block.setOnLongClickListener(v -> {
            if(onItemLongClickListener != null) {
                return onItemLongClickListener.onClick(holder.block, data.get(position), position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemLongClickListener {
        boolean onClick(View view, Post post, int position);
    }
}

