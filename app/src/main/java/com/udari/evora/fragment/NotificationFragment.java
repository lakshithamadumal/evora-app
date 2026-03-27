package com.udari.evora.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.udari.evora.R;
import com.udari.evora.adapter.NotificationAdapter;
import com.udari.evora.model.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rv_notifications);

        setupRecyclerView();
        loadNotifications();

        return view;
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("Notifications")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        return;
                    }

                    if (value != null) {
                        notificationList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            NotificationModel model = doc.toObject(NotificationModel.class);
                            if (model != null) {
                                notificationList.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}