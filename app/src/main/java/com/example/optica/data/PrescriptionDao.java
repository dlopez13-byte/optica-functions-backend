package com.example.optica.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.optica.model.Prescription;
import java.util.List;

@Dao
public interface PrescriptionDao {
    @Insert
    long insert(Prescription prescription);

    @Update
    void update(Prescription prescription);

    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    List<Prescription> getAllPrescriptions();
}
