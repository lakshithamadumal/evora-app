package com.udari.evora.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.udari.evora.activity.MapActivity;
import com.udari.evora.databinding.FragmentProfileBinding;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ActivityResultLauncher<Intent> mapResultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selected_address");
                        binding.etProfileAddress.setText(address);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        setupListeners();
        loadUserData();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnChooseAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            mapResultLauncher.launch(intent);
        });

        binding.btnSaveProfile.setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            binding.etProfileName.setText(documentSnapshot.getString("name"));
                            binding.etProfileEmail.setText(documentSnapshot.getString("email"));
                            binding.etProfilePhone.setText(documentSnapshot.getString("phoneNumber"));
                            binding.etProfileCity.setText(documentSnapshot.getString("city"));
                            binding.etProfileAddress.setText(documentSnapshot.getString("address"));
                        }
                    });
        }
    }

    private void updateProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", binding.etProfileName.getText().toString());
        updates.put("phoneNumber", binding.etProfilePhone.getText().toString());
        updates.put("city", binding.etProfileCity.getText().toString());
        updates.put("address", binding.etProfileAddress.getText().toString());

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroy();
        binding = null;
    }
}