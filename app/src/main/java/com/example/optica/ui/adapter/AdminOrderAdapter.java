package com.example.optica.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.optica.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<Map<String, Object>> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Map<String, Object> order);
    }

    public AdminOrderAdapter(List<Map<String, Object>> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Map<String, Object> order = orders.get(position);
        
        String userId = String.valueOf(order.get("userId"));
        String status = String.valueOf(order.get("status"));
        long timestamp = 0;
        if (order.get("timestamp") != null) {
            timestamp = (long) order.get("timestamp");
        }

        holder.tvClientName.setText("Usuario ID: " + userId.substring(0, Math.min(userId.length(), 8)) + "...");
        holder.tvStatus.setText("Estado: " + status);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText("Fecha: " + sdf.format(new Date(timestamp)));

        holder.btnViewDetail.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvStatus, tvDate;
        View btnViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvOrderClientName);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            btnViewDetail = itemView.findViewById(R.id.btnViewOrderDetail);
        }
    }
}
