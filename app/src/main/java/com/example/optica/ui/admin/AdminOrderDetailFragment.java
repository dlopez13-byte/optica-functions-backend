package com.example.optica.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.optica.R;
import com.example.optica.utils.CryptoUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminOrderDetailFragment extends Fragment {

    private Map<String, Object> orderData;
    private String orderIdFromNotification;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView tvUser, tvStatus, tvConfig;
    private ImageView ivPrescription;

    public static AdminOrderDetailFragment newInstance(Map<String, Object> order) {
        AdminOrderDetailFragment fragment = new AdminOrderDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("order_data", new HashMap<>(order));
        fragment.setArguments(args);
        return fragment;
    }

    public static AdminOrderDetailFragment newInstanceFromId(String orderId) {
        AdminOrderDetailFragment fragment = new AdminOrderDetailFragment();
        Bundle args = new Bundle();
        args.putString("order_id", orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("order_data")) {
                orderData = (Map<String, Object>) getArguments().getSerializable("order_data");
            } else if (getArguments().containsKey("order_id")) {
                orderIdFromNotification = getArguments().getString("order_id");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_order_detail, container, false);

        tvUser = view.findViewById(R.id.tvAdminDetailUser);
        tvStatus = view.findViewById(R.id.tvAdminDetailStatus);
        tvConfig = view.findViewById(R.id.tvAdminDetailConfig);
        ivPrescription = view.findViewById(R.id.ivAdminPrescriptionPhoto);
        View btnUpdate = view.findViewById(R.id.btnAdminUpdateStatus);

        if (orderData != null) {
            displayOrderDetails();
        } else if (orderIdFromNotification != null) {
            fetchOrderById(orderIdFromNotification);
        }

        btnUpdate.setOnClickListener(v -> updateOrderStatus());

        return view;
    }

    private void fetchOrderById(String orderId) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        orderData = documentSnapshot.getData();
                        if (orderData != null) {
                            orderData.put("docId", documentSnapshot.getId());
                            displayOrderDetails();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ADMIN_DETAIL", "Error fetching order", e));
    }

    private void displayOrderDetails() {
        String userId = String.valueOf(orderData.get("userId"));
        String status = String.valueOf(orderData.get("status"));
        String color = String.valueOf(orderData.get("color"));
        String size = String.valueOf(orderData.get("size"));
        String lens = String.valueOf(orderData.get("lensType"));
        Object prescriptionId = orderData.get("prescriptionId");

        tvUser.setText("Cliente ID: " + userId);
        tvStatus.setText("Estado: " + status);
        tvConfig.setText("Marco: " + orderData.get("productId") + "\nColor: " + color + "\nTamaño: " + size + "\nCristal: " + lens);

        fetchPrescriptionImage(userId, prescriptionId);
    }

    private void fetchPrescriptionImage(String userId, Object prescriptionId) {
        // Asegurar que prescriptionId se maneje correctamente como número
        long pId = 0;
        if (prescriptionId instanceof Number) {
            pId = ((Number) prescriptionId).longValue();
        }

        db.collection("recetas")
                .whereEqualTo("userId", userId)
                .whereEqualTo("idLocal", pId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(0).getData();
                        if (data != null) {
                            String imageUrl = (String) data.get("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                if (isAdded()) {
                                    Glide.with(this).load(imageUrl).into(ivPrescription);
                                }
                            }

                            // QA Security: Desencriptar datos para el óptico
                            String formulaTecnica = String.format(
                                "OD: Esf: %s, Cil: %s, Eje: %s, Add: %s\n" +
                                "OI: Esf: %s, Cil: %s, Eje: %s, Add: %s",
                                CryptoUtils.decrypt((String) data.get("odEsfera")),
                                CryptoUtils.decrypt((String) data.get("odCilindro")),
                                CryptoUtils.decrypt((String) data.get("odEje")),
                                CryptoUtils.decrypt((String) data.get("odAdicion")),
                                CryptoUtils.decrypt((String) data.get("oiEsfera")),
                                CryptoUtils.decrypt((String) data.get("oiCilindro")),
                                CryptoUtils.decrypt((String) data.get("oiEje")),
                                CryptoUtils.decrypt((String) data.get("oiAdicion"))
                            );
                            
                            tvConfig.append("\n\nFÓRMULA MÉDICA:\n" + formulaTecnica);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ADMIN_DETAIL", "Error buscando receta", e));
    }

    private void updateOrderStatus() {
        String docId = String.valueOf(orderData.get("docId"));
        db.collection("orders").document(docId)
                .update("status", "PROCESADO")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Pedido actualizado con éxito", Toast.LENGTH_SHORT).show();
                    if (isAdded()) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show());
    }
}
