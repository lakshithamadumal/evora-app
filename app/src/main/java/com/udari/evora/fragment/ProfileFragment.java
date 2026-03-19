package com.udari.evora.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.udari.evora.activity.MapActivity;
import com.udari.evora.databinding.FragmentProfileBinding;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ActivityResultLauncher<Intent> mapResultLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private String uploadedImageUrl = "";
    private final String IMGBB_API_KEY = "5cf3fbf76d4e5552e63ff0b168bcca79";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String address = result.getData().getStringExtra("selected_address");
                binding.etProfileAddress.setText(address);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                uploadImageToImgBB(imageUri);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        binding.etProfileAddress.setFocusable(false);
        binding.etProfileAddress.setClickable(true);

        setupListeners();
        loadUserData();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.profileImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        binding.btnChooseAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            mapResultLauncher.launch(intent);
        });

        binding.btnSaveProfile.setOnClickListener(v -> updateProfile());
    }

    private void uploadImageToImgBB(Uri imageUri) {
        Toast.makeText(getContext(), "Uploading Image...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                inputStream.close();

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "profile.jpg", RequestBody.create(bytes, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);

                    String finalUrl = jsonObject.getJSONObject("data").getString("url");
                    uploadedImageUrl = finalUrl;

                    requireActivity().runOnUiThread(() -> {
                        Glide.with(this).load(finalUrl).circleCrop().into(binding.profileImg);

                        updateProfileImageInFirestore(finalUrl);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateProfileImageInFirestore(String url) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .update("profilePicUrl", url) // ඔයාගේ Model එකේ නම මෙතනට දාන්න
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Picture Saved!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Firestore Save Failed!", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    binding.etProfileName.setText(doc.getString("name"));
                    binding.etProfileEmail.setText(doc.getString("email"));
                    binding.etProfilePhone.setText(doc.getString("phoneNumber"));
                    binding.etProfileCity.setText(doc.getString("city"));
                    binding.etProfileAddress.setText(doc.getString("address"));

                    String imgUrl = doc.getString("profilePicUrl");
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        uploadedImageUrl = imgUrl;
                        Glide.with(this).load(imgUrl).circleCrop().into(binding.profileImg);
                    }
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

        updates.put("profilePicUrl", uploadedImageUrl);

        FirebaseFirestore.getInstance().collection("Users").document(uid).update(updates).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(getContext(), "Update Failed!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}