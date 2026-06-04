package com.example.optica.data;

import com.example.optica.model.Order;
import com.example.optica.model.Product;
import com.example.optica.model.User;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    
    @POST("login")
    Call<User> login(@Body Map<String, String> body);

    @GET("productos")
    Call<List<Product>> getProducts();

    @POST("pedidos")
    Call<Void> createOrder(@Body Order order);

    @GET("pedidos/{userId}")
    Call<List<Order>> getOrdersByUser(@Path("userId") String userId);
}
