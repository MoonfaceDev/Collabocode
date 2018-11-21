package com.moonface.collabocode;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private final Activity context;
    private List<Post> data;
    private OnItemLongClickListener onItemLongClickListener;
    private static final String CODE = "code";


    class ViewHolder extends RecyclerView.ViewHolder {
        View block;
        ImageView iconView;
        TextView nameView, dateView, titleView, contentView;
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
        }
    }

    PostsAdapter(List<Post> data, Activity context) {
        this.data = data;
        this.context = context;
    }

    void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
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
        holder.dateView.setText(DateFormat.getDateInstance().format(data.get(position).getDateSent().toDate()));
        holder.titleView.setText(data.get(position).getTitle());
        holder.contentView.setText(data.get(position).getContent());
        holder.codesView.setLayoutManager(new LinearLayoutManager(context));
        CodesAdapter adapter = new CodesAdapter(data.get(position).getCodes());
        adapter.setOnItemClickListener((code, position1) -> context.startActivity(new Intent().setClass(context, CodeViewerActivity.class).putExtra(CODE, code.getContent())));
        holder.codesView.setAdapter(adapter);

        holder.block.setOnLongClickListener(v -> onItemLongClickListener.onClick(holder.block, data.get(position), position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemLongClickListener {
        boolean onClick(View view, Post post, int position);
    }
}

