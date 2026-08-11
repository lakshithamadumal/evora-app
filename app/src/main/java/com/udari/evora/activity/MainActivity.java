package com.udari.evora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.udari.evora.R;
import com.udari.evora.databinding.ActivityMainBinding;
import com.udari.evora.fragment.CartFragment;
import com.udari.evora.fragment.HomeFragment;
import com.udari.evora.fragment.NotificationFragment;
import com.udari.evora.fragment.OrdersFragment;
import com.udari.evora.fragment.ProfileFragment;
import com.udari.evora.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.navView.getHeaderView(0);
        ImageView headerImg = headerView.findViewById(R.id.nav_header_img);
        TextView headerName = headerView.findViewById(R.id.nav_header_name);
        TextView headerEmail = headerView.findViewById(R.id.nav_header_email);

        loadHeaderData(headerImg, headerName, headerEmail);

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), true);
        }

        initNavigation();
        updateFCMToken();
    }

    private void initNavigation() {

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_menu_trigger) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
                return false;
            }

            if (id == R.id.bottom_home) replaceFragment(new HomeFragment(), true);
            else if (id == R.id.bottom_cart) replaceFragment(new CartFragment(), true);
            else if (id == R.id.bottom_orders) replaceFragment(new OrdersFragment(), true);
            else if (id == R.id.bottom_profile) replaceFragment(new ProfileFragment(), true);

            return true;
        });

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.side_home) {
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_home);
            } else if (id == R.id.side_cart) {
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_cart);
            } else if (id == R.id.side_orders) {
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_orders);
            } else if (id == R.id.side_profile) {
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_profile);
            } else if (id == R.id.side_settings) {
                replaceFragment(new SettingsFragment(), false);
            } else if (id == R.id.side_logout) {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                logoutUser();
            }

            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });


    }

    private void replaceFragment(Fragment fragment, boolean shouldKeepBottomNavHighlight) {

        if (!shouldKeepBottomNavHighlight) {
            binding.bottomNavigation.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < binding.bottomNavigation.getMenu().size(); i++) {
                binding.bottomNavigation.getMenu().getItem(i).setChecked(false);
            }
            binding.bottomNavigation.getMenu().setGroupCheckable(0, true, true);

            if (binding.navView.getCheckedItem() != null) {
                binding.navView.getCheckedItem().setChecked(false);
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


    private void updateFCMToken() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }

                String token = task.getResult();

                FirebaseFirestore.getInstance().collection("Users").document(uid).update("fcmToken", token).addOnSuccessListener(aVoid -> {
                }).addOnFailureListener(e -> {
                });
            });
        }
    }

    private void loadHeaderData(ImageView img, TextView name, TextView email) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .addSnapshotListener((value, error) -> {
                        if (value != null && value.exists()) {
                            String userName = value.getString("name");
                            String userEmail = value.getString("email");
                            String userImg = value.getString("profilePicUrl");

                            name.setText(userName != null ? userName : "User");
                            email.setText(userEmail != null ? userEmail : "");

                            if (userImg != null && !userImg.isEmpty()) {
                                Glide.with(this)
                                        .load(userImg)
                                        .circleCrop()
                                        .placeholder(R.drawable.profile)
                                        .into(img);
                            }
                        }
                    });
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, com.udari.evora.activity.SignInActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}