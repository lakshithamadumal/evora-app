package com.udari.evora.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            }

            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        binding.btnNotification.setOnClickListener(v -> {
            replaceFragment(new NotificationFragment(), false);
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

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
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
}