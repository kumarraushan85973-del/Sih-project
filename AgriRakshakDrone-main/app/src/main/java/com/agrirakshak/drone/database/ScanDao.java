package com.agrirakshak.drone.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScanDao {

    @Insert
    void insertScan(ScanEntity scan);

    @Query("SELECT * FROM scans ORDER BY id DESC")
    List<ScanEntity> getAllScans();

    @Query("DELETE FROM scans")
    void deleteAllScans();
}