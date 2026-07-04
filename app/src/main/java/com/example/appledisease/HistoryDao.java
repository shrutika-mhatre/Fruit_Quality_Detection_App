package com.example.appledisease;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert
    void insert(DetectionHistory history);

    @Query("SELECT * FROM detection_history ORDER BY id DESC")
    List<DetectionHistory> getAllHistory();

    @Delete
    void delete(DetectionHistory history);

    @Query("DELETE FROM detection_history")
    void deleteAll();
}
