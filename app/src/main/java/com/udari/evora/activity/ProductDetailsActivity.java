package com.udari.evora.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.udari.evora.databinding.ActivityProductDetailsBinding;
import com.udari.evora.model.CartItem;
import com.udari.evora.model.Product;

public class ProductDetailsActivity extends AppCompatActivity {

    private ActivityProductDetailsBinding binding;
    private int quantity = 1;
    private Product product;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            displayProductDetails();
            setupQuantityButtons();
            setupAddToCart();
        }
    }

    private void displayProductDetails() {
        binding.detailProductName.setText(product.getTitle());
        binding.detailProductPrice.setText("Rs. " + String.format("%.2f", product.getPrice()));
        binding.detailProductDesc.setText(product.getDescription());
        binding.detailProductStock.setText(product.getStockCount() + " Available");

        Glide.with(this).load(product.getImageUrl()).into(binding.detailProductImg);

        if (product.getStockCount() <= 0) {
            binding.btnAddToCartFinal.setEnabled(false);
            binding.btnAddToCartFinal.setText("Out of Stock");
        }
    }

    private void setupQuantityButtons() {
        binding.btnPlus.setOnClickListener(v -> {
            if (quantity < product.getStockCount()) {
                quantity++;
                binding.tvQty.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Maximum available stock reached", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQty.setText(String.valueOf(quantity));
            }
        });
    }

    private void setupAddToCart() {
        binding.btnAddToCartFinal.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;

            var cartDocRef = db.collection("Users").document(uid).collection("Cart").document(product.getProductId());

            CartItem cartItem = CartItem.builder().cartItemId(cartDocRef.getId()).productId(product.getProductId()).productTitle(product.getTitle()).productImage(product.getImageUrl()).unitPrice(product.getPrice()).quantity(quantity).totalPrice(product.getPrice() * quantity).build();

            cartDocRef.set(cartItem).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}