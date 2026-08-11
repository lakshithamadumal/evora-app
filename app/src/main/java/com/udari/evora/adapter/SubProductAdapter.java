package com.udari.evora.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.udari.evora.R;
import com.udari.evora.model.CartItem;

import java.util.List;

public class SubProductAdapter extends RecyclerView.Adapter<SubProductAdapter.ViewHolder> {
    private List<CartItem> items;

    public SubProductAdapter(List<CartItem> items) { this.items = items; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_sub_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.name.setText("• " + item.getProductTitle() + " x " + item.getQuantity());
    }

    @Override public int getItemCount() { return items != null ? items.size() : 0; }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.sub_product_name);
        }
    }
}
