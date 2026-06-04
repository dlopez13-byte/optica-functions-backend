package com.example.optica.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.optica.model.Appointment;
import java.util.List;

@Dao
public interface AppointmentDao {
    @Insert
    void insert(Appointment appointment);

    @Query("SELECT * FROM appointments ORDER BY id DESC")
    List<Appointment> getAllAppointments();
}
