package com.udari.evora.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.udari.evora.activity.SignInActivity;
import com.udari.evora.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "AppPrefs";

    // Permission Launchers
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> callPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(getContext(), "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                loadSettings(); // Switch එක ආපහු off කරන්න
            }
        });

        callPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                makeDirectCall();
            } else {
                Toast.makeText(getContext(), "Call permission denied!", Toast.LENGTH_SHORT).show();
            }
        });

        loadSettings();
        setupListeners();

        return binding.getRoot();
    }

    private void loadSettings() {
        binding.switchCalls.setChecked(sharedPreferences.getBoolean("calls", false));
        binding.switchNotifications.setChecked(sharedPreferences.getBoolean("notifications", false));
        binding.switchGps.setChecked(sharedPreferences.getBoolean("gps", false));
    }

    private void setupListeners() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkAndRequestPermission(Manifest.permission.POST_NOTIFICATIONS, "notifications", isChecked);
            } else {
                editor.putBoolean("notifications", isChecked).apply();
            }
        });

        // 2. GPS Switch
        binding.switchGps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestPermission(Manifest.permission.ACCESS_FINE_LOCATION, "gps", isChecked);
            } else {
                editor.putBoolean("gps", isChecked).apply();
            }
        });

        // 3. Allow Calls Switch (Permission to read phone state)
        binding.switchCalls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestPermission(Manifest.permission.READ_PHONE_STATE, "calls", isChecked);
            } else {
                editor.putBoolean("calls", isChecked).apply();
            }
        });

        // 4. Call Us Button - Auto Call
        binding.btnCallUs.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                makeDirectCall();
            } else {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            }
        });

        // 5. Logout Button
        binding.btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void checkAndRequestPermission(String permission, String prefKey, boolean isChecked) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            sharedPreferences.edit().putBoolean(prefKey, true).apply();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void makeDirectCall() {
        String phoneNumber = "0711099224";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}