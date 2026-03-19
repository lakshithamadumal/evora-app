package com.udari.evora.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.udari.evora.databinding.ItemCartBinding;
import com.udari.evora.model.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChange(CartItem item, int newQty);
        void onDeleteItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.binding.cartProductName.setText(item.getProductTitle());
        holder.binding.cartProductPrice.setText("Rs. " + String.format("%.2f", item.getTotalPrice()));
        holder.binding.tvCartQty.setText(String.valueOf(item.getQuantity()));

        Glide.with(holder.itemView.getContext()).load(item.getProductImage()).into(holder.binding.cartProductImg);

        holder.binding.btnCartPlus.setOnClickListener(v -> {
            listener.onQuantityChange(item, item.getQuantity() + 1);
        });

        holder.binding.btnCartMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChange(item, item.getQuantity() - 1);
            }
        });

        holder.binding.btnRemoveItem.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;
        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}