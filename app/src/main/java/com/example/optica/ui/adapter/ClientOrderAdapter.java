package com.example.optica.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.optica.R;
import com.example.optica.model.Order;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientOrderAdapter extends RecyclerView.Adapter<ClientOrderAdapter.OrderViewHolder> {

    private List<Order> orders;

    public ClientOrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        holder.tvTitle.setText("Producto: " + order.getProductId());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvInfo.setText("Pedido #" + order.getId() + " | " + sdf.format(new Date(order.getTimestamp())));
        
        holder.tvState.setText("Estado: " + order.getStatus());
        
        // Estilo dinámico según estado
        if ("PROCESADO".equals(order.getStatus())) {
            holder.tvState.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            // Usar color secundario o de acento para pendiente
            holder.tvState.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvState;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvOrderTitle);
            tvInfo = itemView.findViewById(R.id.tvOrderInfo);
            tvState = itemView.findViewById(R.id.tvOrderState);
        }
    }
}
