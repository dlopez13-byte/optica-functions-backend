package com.example.optica.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appointments")
public class Appointment {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String serviceType;
    private String date;
    private String time;

    public Appointment(String serviceType, String date, String time) {
        this.serviceType = serviceType;
        this.date = date;
        this.time = time;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getServiceType() { return serviceType; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}
