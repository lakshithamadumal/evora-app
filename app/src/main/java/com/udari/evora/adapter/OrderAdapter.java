package com.udari.evora.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.udari.evora.R;
import com.udari.evora.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) { this.orderList = orderList; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderId.setText("Order #" + order.getOrderId());
        holder.status.setText(order.getStatus());
        holder.totalAmount.setText("Rs. " + String.format("%.2f", order.getTotalAmount()));

        // --- Nested RecyclerView Logic ---
        SubProductAdapter subAdapter = new SubProductAdapter(order.getItems());
        holder.rvSubItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvSubItems.setAdapter(subAdapter);
    }

    @Override public int getItemCount() { return orderList.size(); }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, status, totalAmount;
        RecyclerView rvSubItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            status = itemView.findViewById(R.id.order_status);
            totalAmount = itemView.findViewById(R.id.order_total);
            rvSubItems = itemView.findViewById(R.id.rv_order_items);
        }
    }
}