package com.example.optica.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.optica.R;
import com.example.optica.ui.adapter.AdminOrderAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrdersFragment extends Fragment implements AdminOrderAdapter.OnOrderClickListener {

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private final List<Map<String, Object>> orderList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        recyclerView = view.findViewById(R.id.rvAdminOrders);
        adapter = new AdminOrderAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnTestNotification).setOnClickListener(v -> simulateNewOrder());

        listenToOrders();
        subscribeToAdminNotifications();

        return view;
    }

    private void listenToOrders() {
        db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE_ADMIN", "Error escuchando órdenes", error);
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, Object> data = doc.getData();
                            data.put("docId", doc.getId()); // Guardar el ID del documento de Firestore
                            orderList.add(data);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void subscribeToAdminNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("admin_orders")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM_ADMIN", "Suscrito al tema de órdenes");
                    }
                });
    }

    private void simulateNewOrder() {
        Map<String, Object> testOrder = new HashMap<>();
        testOrder.put("userId", "TEST_USER_123");
        testOrder.put("productId", "MARCO_PRUEBA");
        testOrder.put("color", "Rojo");
        testOrder.put("size", "L");
        testOrder.put("lensType", "Filtro Azul");
        testOrder.put("prescriptionId", 0);
        testOrder.put("status", "PENDIENTE");
        testOrder.put("timestamp", System.currentTimeMillis());

        db.collection("orders").add(testOrder)
                .addOnSuccessListener(doc -> Toast.makeText(getContext(), 
                        "Pedido de prueba creado. ¡Espera la notificación!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), 
                        "Error al simular pedido", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onOrderClick(Map<String, Object> order) {
        // Navegación al detalle técnico del pedido
        AdminOrderDetailFragment detailFragment = AdminOrderDetailFragment.newInstance(order);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
