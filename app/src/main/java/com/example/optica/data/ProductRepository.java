package com.example.optica.data;

import com.example.optica.model.Product;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    private final ApiService apiService;

    public ProductRepository() {
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(Throwable t);
    }

    public void fetchProducts(ProductCallback callback) {
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Error en la respuesta del servidor"));
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
