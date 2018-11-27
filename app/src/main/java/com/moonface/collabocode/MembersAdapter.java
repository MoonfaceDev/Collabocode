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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    private final Activity context;
    private List<Member> data;
    private OnItemLongClickListener listener;

    class ViewHolder extends RecyclerView.ViewHolder {
        View block;
        ImageView profileView;
        TextView nameView;
        ViewHolder(View v) {
            super(v);
            block = v.findViewById(R.id.block);
            profileView = v.findViewById(R.id.icon_view);
            nameView = v.findViewById(R.id.name_view);
        }
    }

    MembersAdapter(List<Member> data, Activity context, OnItemLongClickListener listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MembersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseFirestore.getInstance().collection("users").document(data.get(position).getUserId()).get().addOnCompleteListener(command -> {
            User user = Objects.requireNonNull(command.getResult()).toObject(User.class);
            if (user != null) {
                holder.nameView.setText(user.getName());
                Glide.with(context)
                        .load(user.getProfilePicUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.profileView);
            }
        });
        holder.block.setOnLongClickListener(v -> listener.onClick(holder.block, data.get(position),position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemLongClickListener {
        boolean onClick(View view, Member member, int position);
    }

}

