package com.example.optica.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.optica.R;
import com.example.optica.data.ApiService;
import com.example.optica.data.AppDatabase;
import com.example.optica.data.RetrofitClient;
import com.example.optica.model.Order;
import com.example.optica.ui.adapter.ClientOrderAdapter;
import com.example.optica.ui.catalog.CatalogFragment;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClientOrderAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerView;
    private View emptyState;
    private final List<Order> orderList = new ArrayList<>();
    private final ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_orders, container, false);

        recyclerView = view.findViewById(R.id.rvClientOrders);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshOrders);
        shimmerView = view.findViewById(R.id.shimmerOrders);
        emptyState = view.findViewById(R.id.layoutOrdersEmpty);

        adapter = new ClientOrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnExploreFrames).setOnClickListener(v -> 
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CatalogFragment())
                    .commit()
        );

        // DevOps Strategy: Carga inmediata desde Room antes de disparar la red
        loadLocalOrders();
        
        loadOrders(); // Disparar actualización desde Render

        swipeRefreshLayout.setOnRefreshListener(this::loadOrders);

        // Analytics: Registro de consulta
        FirebaseAnalytics.getInstance(requireContext()).logEvent("revisar_historial", null);

        return view;
    }

    private void loadOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Si la lista está vacía (primera vez), mostrar Shimmer
        if (orderList.isEmpty()) {
            shimmerView.setVisibility(View.VISIBLE);
            shimmerView.startShimmer();
            swipeRefreshLayout.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }

        apiService.getOrdersByUser(user.getUid()).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (isAdded()) {
                    stopLoadingEffects();
                    if (response.isSuccessful() && response.body() != null) {
                        orderList.clear();
                        orderList.addAll(response.body());
                        
                        // Actualizar Room con los datos frescos del servidor (Consistencia)
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (Order remoteOrder : response.body()) {
                                AppDatabase.getDatabase(requireContext()).orderDao().insert(remoteOrder);
                            }
                        });

                        updateUI();
                    } else {
                        // Resiliencia ante error del servidor
                        loadLocalOrders();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    stopLoadingEffects();
                    loadLocalOrders();
                    // QA Feedback
                    Toast.makeText(getContext(), "Sin conexión. Mostrando historial guardado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void stopLoadingEffects() {
        shimmerView.stopShimmer();
        shimmerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI() {
        if (orderList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void loadLocalOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Order> locales = AppDatabase.getDatabase(requireContext()).orderDao().getOrdersByUser(user.getUid());
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (!locales.isEmpty()) {
                        orderList.clear();
                        orderList.addAll(locales);
                        updateUI();
                    }
                });
            }
        });
    }
}
