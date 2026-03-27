package com.udari.evora.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.udari.evora.R;
import com.udari.evora.model.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> list;

    public NotificationAdapter(List<NotificationModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel model = list.get(position);
        holder.title.setText(model.getTitle());
        holder.body.setText(model.getBody());

        try {
            long timeInMillis = Long.parseLong(model.getTimestamp());
            Date date = new Date(timeInMillis);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());
            holder.time.setText(sdf.format(date));
        } catch (Exception e) {
            holder.time.setText("Just now");
        }

        holder.unreadDot.setVisibility(model.isRead() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (!model.isRead()) {
                updateNotificationStatus(model, position);
            }
        });
    }

    private void updateNotificationStatus(NotificationModel model, int position) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("Notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("timestamp", model.getTimestamp())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().update("read", true);
                    }
                    model.setRead(true);
                    notifyItemChanged(position);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, time;
        View unreadDot;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noti_title);
            body = itemView.findViewById(R.id.noti_body);
            time = itemView.findViewById(R.id.noti_time);
            unreadDot = itemView.findViewById(R.id.unread_dot);
        }
    }
}