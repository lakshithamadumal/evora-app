package com.udari.evora.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
            replaceFragment(new HomeFragment());
        }

        initNavigation();
    }

    private void initNavigation() {

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_menu_trigger) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
                return false;
            }

            if (id == R.id.bottom_home) replaceFragment(new HomeFragment());
            else if (id == R.id.bottom_cart) replaceFragment(new CartFragment());
            else if (id == R.id.bottom_orders) replaceFragment(new OrdersFragment());
            else if (id == R.id.bottom_profile) replaceFragment(new ProfileFragment());

            return true;
        });

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.side_home) {
                replaceFragment(new HomeFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_home);
            } else if (id == R.id.side_cart) {
                replaceFragment(new CartFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_cart);
            } else if (id == R.id.side_orders) {
                replaceFragment(new OrdersFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_orders);
            } else if (id == R.id.side_profile) {
                replaceFragment(new ProfileFragment());
                binding.bottomNavigation.setSelectedItemId(R.id.bottom_profile);
            } else if (id == R.id.side_settings) {
                replaceFragment(new SettingsFragment());
            } else if (id == R.id.side_logout) {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            }

            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        binding.btnNotification.setOnClickListener(v -> replaceFragment(new NotificationFragment()));
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}