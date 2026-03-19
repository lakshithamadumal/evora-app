package com.udari.evora.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.udari.evora.databinding.ActivitySignUpBinding;
import com.udari.evora.model.User;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupListeners();
    }

    private void setupListeners() {
        binding.signUpBtn.setOnClickListener(v -> {
            String name = binding.fullName.getText().toString().trim();
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(name, email, password);
            }
        });

        binding.signInText.setOnClickListener(v -> finish());
    }

    private void registerUser(String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = firebaseAuth.getCurrentUser().getUid();

                        User newUser = User.builder()
                                .uid(uid)
                                .name(name)
                                .email(email)
                                .profilePicUrl("https://i.ibb.co/Kx6Z9MkF/avatar.png")
                                .phoneNumber("")
                                .address("")
                                .city("")
                                .fcmToken("")
                                .build();

                        saveToFirestore(newUser);

                    } else {
                        Toast.makeText(this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveToFirestore(User user) {
        firestore.collection("Users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}