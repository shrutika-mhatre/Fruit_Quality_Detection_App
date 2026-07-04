package com.example.appledisease;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "detection_history")
public class DetectionHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String imagePath;
    public String diseaseName;
    public float  confidence;
    public String dateTime;

    public DetectionHistory(String imagePath, String diseaseName, float confidence, String dateTime) {
        this.imagePath   = imagePath;
        this.diseaseName = diseaseName;
        this.confidence  = confidence;
        this.dateTime    = dateTime;
    }
}
