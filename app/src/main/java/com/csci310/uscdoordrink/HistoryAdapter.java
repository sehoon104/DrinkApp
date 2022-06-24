package com.csci310.uscdoordrink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Hist> {

    List<HistoryRow> histories;

    public HistoryAdapter(List<HistoryRow> list) {
        histories = list;
    }

    @NonNull
    @Override
    public Hist onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.historyrow, parent, false);
        return new Hist(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Hist holder, int position) {
        HistoryRow history = histories.get(position);
        holder.datetxt.setText(history.getOrdertime());
        holder.addresstxt.setText(history.getAddress());
        holder.descriptiontxt.setText(history.getDescription());

        boolean isExpandable = histories.get(position).isExpandable();
        holder.relativeLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public class Hist extends RecyclerView.ViewHolder {
        TextView datetxt, addresstxt, descriptiontxt;
        LinearLayout linearLayout;
        RelativeLayout relativeLayout;
        public Hist(@NonNull View itemView) {
            super(itemView);
            datetxt = itemView.findViewById(R.id.ordertime);
            addresstxt = itemView.findViewById(R.id.historyaddress);
            descriptiontxt = itemView.findViewById(R.id.historydescription);
            linearLayout = itemView.findViewById(R.id.linlay);
            relativeLayout = itemView.findViewById(R.id.expandhistory);

            linearLayout.setOnClickListener(view -> {
                HistoryRow row = histories.get(getAdapterPosition());
                row.setExpandable(!row.isExpandable());
                notifyItemChanged(getAdapterPosition());
            });

        }
    }
}
