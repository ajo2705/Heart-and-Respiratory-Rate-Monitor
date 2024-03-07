/*
Author: Akhil Jose
UID: ajose12
 */
package com.example.ajohearresp.db;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ajohearresp.schema.HeartRespData;

// DATA ACCESS
@Dao
public interface HeartRespDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertHeartRespData(HeartRespData data);

    @Query("SELECT * FROM symptoms WHERE uid = :symptomsId")
    HeartRespData getHeartRespById(int symptomsId);

    @Query("SELECT * FROM symptoms")
    List<HeartRespData> getAllHeartRespData();
}
