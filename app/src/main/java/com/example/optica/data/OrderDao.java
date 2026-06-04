package com.example.optica.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.optica.model.Order;
import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    long insert(Order order);

    @Update
    void update(Order order);

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY timestamp DESC")
    List<Order> getOrdersByUser(String userId);

    @Query("SELECT * FROM orders ORDER BY id DESC")
    List<Order> getAllOrders();
}
