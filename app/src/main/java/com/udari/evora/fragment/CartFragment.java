package com.udari.evora.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.udari.evora.adapter.CartAdapter;
import com.udari.evora.databinding.FragmentCartBinding;
import com.udari.evora.model.CartItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import lk.payhere.androidsdk.PHConfigs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {

    private FragmentCartBinding binding;
    private FirebaseFirestore db;
    private CartAdapter adapter;
    private List<CartItem> cartItemList;
    private final double SHIPPING_FEE = 350.00;
    private double currentTotal = 0;
    private final int PAYHERE_REQUEST = 11001;

    private String uName, uPhone, uAddress, uCity, uEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        cartItemList = new ArrayList<>();

        setupRecyclerView();
        loadCartItems();

        // Checkout Button Click
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
                    if (error != null || binding == null) return;
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
        currentTotal = subtotal > 0 ? subtotal + SHIPPING_FEE : 0;
        binding.tvSubtotal.setText("Rs. " + String.format("%.2f", subtotal));
        binding.tvShipping.setText("Rs. " + String.format("%.2f", SHIPPING_FEE));
        binding.tvTotal.setText("Rs. " + String.format("%.2f", currentTotal));
        binding.btnCheckout.setEnabled(subtotal > 0);
    }

    private void checkUserProfileAndProceed() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        binding.btnCheckout.setEnabled(false);

        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                uName = doc.getString("name");
                uPhone = doc.getString("phoneNumber");
                uAddress = doc.getString("address");
                uCity = doc.getString("city");
                uEmail = doc.getString("email");

                if (isEmpty(uName) || isEmpty(uPhone) || isEmpty(uAddress)) {
                    Toast.makeText(getContext(), "Please complete your profile (Name, Phone, Address) first!", Toast.LENGTH_LONG).show();
                    binding.btnCheckout.setEnabled(true);
                } else {
                    startPaymentGateway();
                }
            }
        }).addOnFailureListener(e -> binding.btnCheckout.setEnabled(true));
    }

    private void startPaymentGateway() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1226955");
        req.setMerchantSecret("MzA1ODA3MDQ0MDM0NzQzMjM5NzUyNzk3MTU5Nzc5MTI4OTEwNjg1Ng==");
        req.setCurrency("LKR");
        req.setAmount(currentTotal);
        req.setOrderId("ORD-" + System.currentTimeMillis());
        req.setItemsDescription("Evora Product Order");
        req.setSandBox(true);

        req.getCustomer().setFirstName(uName);
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(uEmail != null ? uEmail : "customer@example.com");
        req.getCustomer().setPhone(uPhone);
        req.getCustomer().getAddress().setAddress(uAddress);
        req.getCustomer().getAddress().setCity(uCity != null ? uCity : "Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(getActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.btnCheckout.setEnabled(true);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK && response != null && response.isSuccess()) {
                saveOrderToFirestore();
            } else {
                Toast.makeText(getContext(), "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        String orderId = "ORD-" + System.currentTimeMillis();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", uid);
        order.put("customerName", uName);
        order.put("customerPhone", uPhone);
        order.put("deliveryAddress", uAddress);
        order.put("deliveryCity", uCity);
        order.put("totalAmount", currentTotal);
        order.put("status", "Paid & Processing");
        order.put("timestamp", FieldValue.serverTimestamp());
        order.put("items", cartItemList);

        WriteBatch batch = db.batch();
        batch.set(db.collection("Orders").document(orderId), order);

        for (CartItem item : cartItemList) {
            batch.delete(db.collection("Users").document(uid).collection("Cart").document(item.getProductId()));
            batch.update(db.collection("Products").document(item.getProductId()),
                    "stockCount", FieldValue.increment(-item.getQuantity()));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Order Placed Successfully!", Toast.LENGTH_LONG).show();

                // --- තත්පර 3 ක Delay එක මෙතන ---
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        sendOrderConfirmationNotification();
                    }
                }, 3000);
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOrderConfirmationNotification() {
        Context context = getContext();
        if (context == null) return;

        String channelId = "order_status_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo (Android 8) සහ ඉහළ සඳහා Channel එකක් අනිවාර්යයි
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH // වැදගත්: මේක HIGH වෙන්න ඕනේ
            );
            channel.setDescription("Channel for order status updates");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Notification එක හදනවා
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // පරීක්ෂා කිරීමට Default icon එකක්
                .setContentTitle("Order Confirmed! 🎉")
                .setContentText("Your order has been placed successfully. Thank you!")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // මේක HIGH දාන්න
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // සද්දය සහ Vibration එකට
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private boolean isEmpty(String value) { return value == null || value.trim().isEmpty(); }

    @Override public void onQuantityChange(CartItem item, int newQty) {
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("Users").document(uid).collection("Cart").document(item.getProductId())
                .update("quantity", newQty, "totalPrice", (item.getUnitPrice() * newQty));
    }

    @Override public void onDeleteItem(CartItem item) {
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("Users").document(uid).collection("Cart").document(item.getProductId()).delete();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}