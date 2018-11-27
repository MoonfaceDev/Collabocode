package com.moonface.collabocode;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.ViewHolder> {
    private final Activity context;
    private ArrayList<Team> data;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        View block;
        ImageView iconView, starView;
        TextView nameView, descriptionView;
        ViewHolder(View v) {
            super(v);
            block = v.findViewById(R.id.card);
            iconView = v.findViewById(R.id.icon_view);
            starView = v.findViewById(R.id.star_view);
            nameView = v.findViewById(R.id.title_view);
            descriptionView = v.findViewById(R.id.description_view);
        }
    }

    TeamsAdapter(ArrayList<Team> data, Activity context) {
        this.data = data;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public TeamsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.nameView.setText(data.get(position).getTitle());
        holder.descriptionView.setText(data.get(position).getDescription());
        if (data.get(position).getIconUrl() != null) {
            Glide.with(context)
                    .load(data.get(position).getIconUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.iconView);
        } else {
            holder.iconView.setImageResource(R.drawable.ic_group_colored_24dp);
        }
        holder.starView.setVisibility(View.INVISIBLE);
        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection("memberships").document(data.get(position).getId()).get().addOnCompleteListener(task -> {
            if (task.getResult() != null){
                if (Objects.requireNonNull(task.getResult().toObject(Member.class)).isStarred()){
                    holder.starView.setVisibility(View.VISIBLE);
                } else {
                    holder.starView.setVisibility(View.INVISIBLE);
                }
            }
        });
        holder.block.setOnClickListener(v -> onItemClickListener.onClick(data.get(position), position));
        holder.block.setOnLongClickListener(v -> onItemLongClickListener.onClick(holder, data.get(position), position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onClick(Team team, int position);
    }

    public interface OnItemLongClickListener {
        boolean onClick(ViewHolder holder, Team team, int position);
    }
}

