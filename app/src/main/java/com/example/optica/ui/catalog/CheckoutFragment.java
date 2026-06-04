package com.example.optica.ui.catalog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.optica.R;
import com.example.optica.data.ApiService;
import com.example.optica.data.AppDatabase;
import com.example.optica.data.RetrofitClient;
import com.example.optica.model.Order;
import com.example.optica.model.Prescription;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private String productId, productName, productPrice, imageUrl, color, size, lens;
    private int selectedPrescriptionId = -1;
    private List<Prescription> prescriptionList = new ArrayList<>();
    private final ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    public static CheckoutFragment newInstance(String id, String name, String price, String img, String color, String size, String lens) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("price", price);
        args.putString("img", img);
        args.putString("color", color);
        args.putString("size", size);
        args.putString("lens", lens);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("id");
            productName = getArguments().getString("name");
            productPrice = getArguments().getString("price");
            imageUrl = getArguments().getString("img");
            color = getArguments().getString("color");
            size = getArguments().getString("size");
            lens = getArguments().getString("lens");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        ImageView ivProduct = view.findViewById(R.id.checkoutProductImage);
        TextView tvName = view.findViewById(R.id.checkoutProductName);
        TextView tvConfig = view.findViewById(R.id.checkoutProductConfig);
        TextView tvPrice = view.findViewById(R.id.checkoutProductPrice);
        TextView tvLens = view.findViewById(R.id.checkoutLensType);
        AutoCompleteTextView prescriptionDropdown = view.findViewById(R.id.prescriptionDropdown);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmOrder);

        tvName.setText(productName);
        tvConfig.setText("Color: " + color + " | Tamaño: " + size);
        tvPrice.setText(productPrice);
        tvLens.setText(lens);
        Glide.with(this).load(imageUrl).centerCrop().into(ivProduct);

        loadPrescriptions(prescriptionDropdown);

        btnConfirm.setOnClickListener(v -> finalizeOrder());

        return view;
    }

    private void loadPrescriptions(AutoCompleteTextView dropdown) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            prescriptionList = AppDatabase.getDatabase(requireContext()).prescriptionDao().getAllPrescriptions();
            List<String> displayNames = new ArrayList<>();
            for (Prescription p : prescriptionList) {
                displayNames.add("Receta del " + p.getDate() + " (Esf: " + p.getOdSphere() + ")");
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                            android.R.layout.simple_dropdown_item_1line, displayNames);
                    dropdown.setAdapter(adapter);
                    dropdown.setOnItemClickListener((parent, view, position, id) -> {
                        selectedPrescriptionId = prescriptionList.get(position).getId();
                    });
                });
            }
        });
    }

    private void finalizeOrder() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || selectedPrescriptionId == -1) {
            Toast.makeText(getContext(), "Selecciona una receta para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(user.getUid(), productId, color, size, lens, selectedPrescriptionId);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Guardar localmente para historial offline
            AppDatabase.getDatabase(requireContext()).orderDao().insert(order);
            
            // 2. Enviar al backend de Render (él se encarga de Firestore y Notificaciones)
            enviarPedidoAlBackend(order);

            // Analytics: Conversión exitosa
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, productId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, productName);
            bundle.putString(FirebaseAnalytics.Param.PRICE, productPrice);
            FirebaseAnalytics.getInstance(requireContext()).logEvent("realizar_pedido", bundle);
        });
    }

    private void enviarPedidoAlBackend(Order order) {
        apiService.createOrder(order).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "¡Pedido realizado y enviado al backend!", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("API_ERROR", "Código de error: " + response.code());
                            Toast.makeText(getContext(), "Pedido guardado, pero error al informar al backend", Toast.LENGTH_SHORT).show();
                        }
                        getParentFragmentManager().popBackStack(null, 0);
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e("API_FAILURE", t.getMessage(), t);
                        Toast.makeText(getContext(), "Pedido guardado localmente (Sin conexión al backend)", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack(null, 0);
                    });
                }
            }
        });
    }
}
