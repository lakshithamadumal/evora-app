package com.udari.evora.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.udari.evora.R;
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
    private List<Product> allProducts;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getContext(), "Please allow notifications to get order updates!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        productList = new ArrayList<>();
        allProducts = new ArrayList<>();

        setupRecyclerView();
        loadProductsFromFirestore();
        setupSearch();


        askNotificationPermission();

        binding.btnNotifications.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotificationFragment())
                    .addToBackStack(null)
                    .commit();
        });
        askNotificationPermission();

        return binding.getRoot();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
            if (error != null) return;

            if (value != null) {
                allProducts.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Product product = doc.toObject(Product.class);
                    allProducts.add(product);
                }
                filter("");
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : allProducts) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        productList.clear();
        productList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}