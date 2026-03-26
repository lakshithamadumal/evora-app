package com.udari.evora.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.udari.evora.adapter.OrderAdapter;
import com.udari.evora.databinding.FragmentOrdersBinding;
import com.udari.evora.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private FirebaseFirestore db;
    private OrderAdapter adapter;
    private List<Order> orderList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();

        setupRecyclerView();
        loadOrderHistory();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(orderList);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadOrderHistory() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;


        db.collection("Orders")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
//                    binding.progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }

                        if (orderList.isEmpty()) {
//                            binding.tvNoOrders.setVisibility(View.VISIBLE); // ඕඩර්ස් නැත්නම් පෙන්වන TextView එක
                        } else {
//                            binding.tvNoOrders.setVisibility(View.GONE);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}