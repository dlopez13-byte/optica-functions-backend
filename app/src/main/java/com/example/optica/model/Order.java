package com.example.optica.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    private int id;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("productId")
    private String productId;
    
    @SerializedName("frameColor")
    private String frameColor;
    
    @SerializedName("frameSize")
    private String frameSize;
    
    @SerializedName("lensType")
    private String lensType;
    
    @SerializedName("prescriptionId")
    private int prescriptionId;
    
    @SerializedName("status")
    private String status; // PENDING, PROCESSED, SHIPPED
    
    @SerializedName("timestamp")
    private long timestamp;

    public Order(String userId, String productId, String frameColor, String frameSize, String lensType, int prescriptionId) {
        this.userId = userId;
        this.productId = productId;
        this.frameColor = frameColor;
        this.frameSize = frameSize;
        this.lensType = lensType;
        this.prescriptionId = prescriptionId;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public String getFrameColor() { return frameColor; }
    public String getFrameSize() { return frameSize; }
    public String getLensType() { return lensType; }
    public int getPrescriptionId() { return prescriptionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
