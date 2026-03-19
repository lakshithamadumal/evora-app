package com.udari.evora.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.udari.evora.activity.ProductDetailsActivity;
import com.udari.evora.adapter.ProductAdapter;
import com.udari.evora.databinding.FragmentHomeBinding;
import com.udari.evora.model.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        setupRecyclerView();

        loadProductsFromFirestore();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(productList, product -> {
            Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void loadProductsFromFirestore() {
        db.collection("Products").whereEqualTo("status", true).addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            if (value != null) {
                productList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Product product = doc.toObject(Product.class);
                    productList.add(product);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}