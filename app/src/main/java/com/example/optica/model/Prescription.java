package com.example.optica.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "prescriptions")
public class Prescription {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String date;
    
    // Ojo Derecho (OD)
    private String odSphere;
    private String odCylinder;
    private String odAxis;
    private String odAddition;

    // Ojo Izquierdo (OI)
    private String oiSphere;
    private String oiCylinder;
    private String oiAxis;
    private String oiAddition;

    private String imageUrl; // URL de la foto de la receta física
    private boolean isSynced;

    public Prescription(String date, String odSphere, String odCylinder, String odAxis, String odAddition,
                        String oiSphere, String oiCylinder, String oiAxis, String oiAddition) {
        this.date = date;
        this.odSphere = odSphere;
        this.odCylinder = odCylinder;
        this.odAxis = odAxis;
        this.odAddition = odAddition;
        this.oiSphere = oiSphere;
        this.oiCylinder = oiCylinder;
        this.oiAxis = oiAxis;
        this.oiAddition = oiAddition;
        this.isSynced = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public String getOdSphere() { return odSphere; }
    public String getOdCylinder() { return odCylinder; }
    public String getOdAxis() { return odAxis; }
    public String getOdAddition() { return odAddition; }
    public String getOiSphere() { return oiSphere; }
    public String getOiCylinder() { return oiCylinder; }
    public String getOiAxis() { return oiAxis; }
    public String getOiAddition() { return oiAddition; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
