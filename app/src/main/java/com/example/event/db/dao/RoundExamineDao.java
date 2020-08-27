package com.example.event.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.event.db.entity.RoundExamineEntity;

@Dao
public interface RoundExamineDao {
    @Query("SELECT * FROM roundExamine")
    LiveData<RoundExamineEntity> getRoundExamineEntity();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoundExamine(RoundExamineEntity event);
}
