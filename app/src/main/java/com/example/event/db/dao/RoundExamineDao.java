package com.example.event.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.event.db.entity.RoundExamineEntity;

@Dao
public interface RoundExamineDao {
    @Query("SELECT * FROM roundExamine")
    LiveData<RoundExamineEntity> getRoundExamineEntity();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoundExamine(RoundExamineEntity event);
}
