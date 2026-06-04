package com.example.optica.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.optica.R;
import com.example.optica.data.ProductRepository;
import com.example.optica.model.Product;
import com.example.optica.ui.adapter.ProductAdapter;
import com.example.optica.utils.CacheManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class CatalogFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private ShimmerFrameLayout shimmerView;
    private final ProductRepository productRepository = new ProductRepository();
    private CacheManager cacheManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        recyclerView = view.findViewById(R.id.catalogRecyclerView);
        shimmerView = view.findViewById(R.id.shimmerView);
        ChipGroup chipGroup = view.findViewById(R.id.filterChipGroup);
        cacheManager = new CacheManager(requireContext());

        // UI Inicial
        adapter = new ProductAdapter(new ArrayList<>(allProducts));
        recyclerView.setAdapter(adapter);

        // 1. ESTRATEGIA DE CACHÉ (Resiliencia): Carga inmediata desde local
        loadCachedProducts();

        // 2. MANEJO DE RENDIMIENTO: Petición a Render en paralelo
        loadRemoteProducts();

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) filterProducts(null);
            else if (checkedId == R.id.chipMen) filterProducts("Hombre");
            else if (checkedId == R.id.chipWomen) filterProducts("Mujer");
            else if (checkedId == R.id.chipSun) filterProducts("Sol");
            else if (checkedId == R.id.chipRest) filterProducts("Descanso");
        });

        return view;
    }

    private void loadCachedProducts() {
        List<Product> cached = cacheManager.getCache(CacheManager.KEY_CATALOG, CacheManager.getProductListType());
        if (!cached.isEmpty()) {
            allProducts = cached;
            adapter = new ProductAdapter(new ArrayList<>(allProducts));
            recyclerView.setAdapter(adapter);
            shimmerView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            // Si no hay caché, mostrar Shimmer
            shimmerView.startShimmer();
            shimmerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadRemoteProducts() {
        productRepository.fetchProducts(new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isAdded()) {
                    allProducts = products;
                    // Guardar en caché para la próxima vez (Estrategia DevOps)
                    cacheManager.saveCache(CacheManager.KEY_CATALOG, products);
                    
                    adapter = new ProductAdapter(new ArrayList<>(allProducts));
                    recyclerView.setAdapter(adapter);

                    shimmerView.stopShimmer();
                    shimmerView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (isAdded()) {
                    shimmerView.stopShimmer();
                    shimmerView.setVisibility(View.GONE);
                    if (allProducts.isEmpty()) {
                        Toast.makeText(getContext(), "Error: Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
                    } else {
                        // 3. RESILIENCIA ANTE FALLOS: Mantener caché y avisar
                        Toast.makeText(getContext(), "Mostrando datos guardados. Revisa tu conexión.", Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void filterProducts(String category) {
        List<Product> filteredList = new ArrayList<>();
        if (category == null) {
            filteredList.addAll(allProducts);
        } else {
            for (Product p : allProducts) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(p);
                }
            }
        }
        adapter = new ProductAdapter(filteredList);
        recyclerView.setAdapter(adapter);
    }
}
