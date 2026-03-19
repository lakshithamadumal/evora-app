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
import com.udari.evora.adapter.CartAdapter;
import com.udari.evora.databinding.FragmentCartBinding;
import com.udari.evora.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {

    private FragmentCartBinding binding;
    private FirebaseFirestore db;
    private CartAdapter adapter;
    private List<CartItem> cartItemList;
    private final double SHIPPING_FEE = 350.00;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        cartItemList = new ArrayList<>();

        setupRecyclerView();
        loadCartItems();

        binding.btnCheckout.setOnClickListener(v -> checkUserProfileAndProceed());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItemList, this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCart.setAdapter(adapter);
    }

    private void loadCartItems() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).collection("Cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        cartItemList.clear();
                        double subtotal = 0;

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) {
                                cartItemList.add(item);
                                subtotal += item.getTotalPrice();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        calculateBill(subtotal);
                    }
                });
    }

    private void calculateBill(double subtotal) {
        double total = subtotal > 0 ? subtotal + SHIPPING_FEE : 0;

        binding.tvSubtotal.setText("Rs. " + String.format("%.2f", subtotal));
        binding.tvShipping.setText("Rs. " + String.format("%.2f", SHIPPING_FEE));
        binding.tvTotal.setText("Rs. " + String.format("%.2f", total));

        binding.btnCheckout.setEnabled(subtotal > 0);
    }

    @Override
    public void onQuantityChange(CartItem item, int newQty) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        double newTotalPrice = item.getUnitPrice() * newQty;

        db.collection("Users").document(uid).collection("Cart")
                .document(item.getProductId())
                .update("quantity", newQty, "totalPrice", newTotalPrice);
    }

    @Override
    public void onDeleteItem(CartItem item) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("Users").document(uid).collection("Cart")
                .document(item.getProductId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void checkUserProfileAndProceed() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        binding.btnCheckout.setEnabled(false);

        db.collection("Users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String phone = documentSnapshot.getString("phoneNumber");
                String address = documentSnapshot.getString("address");
                String city = documentSnapshot.getString("city");

                if (isEmpty(name) || isEmpty(phone) || isEmpty(address) || isEmpty(city)) {
                    Toast.makeText(getContext(), "Please complete your profile details (Name, Phone, Address, City) before checkout!", Toast.LENGTH_LONG).show();
                    binding.btnCheckout.setEnabled(true);
                } else {
                    Toast.makeText(getContext(), "Payment Gateway Loading...", Toast.LENGTH_SHORT).show();
                    /// To Payment
                }
            }
        }).addOnFailureListener(e -> {
            binding.btnCheckout.setEnabled(true);
            Toast.makeText(getContext(), "Error checking profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}