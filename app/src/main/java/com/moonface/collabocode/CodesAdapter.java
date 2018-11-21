package com.moonface.collabocode;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CodesAdapter extends RecyclerView.Adapter<CodesAdapter.ViewHolder> {

    private List<Code> data;
    private OnItemClickListener onItemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        View block;
        ImageView iconView;
        TextView titleView, descriptionView;
        ViewHolder(View v) {
            super(v);
            block = v.findViewById(R.id.card);
            iconView = v.findViewById(R.id.icon_view);
            titleView = v.findViewById(R.id.title_view);
            descriptionView = v.findViewById(R.id.description_view);
        }
    }

    CodesAdapter(List<Code> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.code_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleView.setText(data.get(position).getTitle());
        holder.block.setOnClickListener(v -> {
            if(onItemClickListener != null){
                onItemClickListener.onClick(data.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onClick(Code code, int position);
    }
}

