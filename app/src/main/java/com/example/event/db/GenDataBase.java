package com.example.event.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.event.db.dao.RoundDao;
import com.example.event.db.dao.UserDao;
import com.example.event.db.entity.RoundEntity;
import com.example.event.db.entity.RoundEventEntity;
import com.example.event.db.entity.RoundExamineEntity;
import com.example.event.db.entity.TraceEntity;
import com.example.event.db.entity.UserEntity;

/**
 * Created by Dingtu2 on 2017/6/9.
 */

@Database(entities = {UserEntity.class, RoundEntity.class, TraceEntity.class, RoundEventEntity.class, RoundExamineEntity.class}, version = 11)
@TypeConverters(DateConverter.class)
public abstract class GenDataBase extends RoomDatabase {

    static final String DATABASE_NAME = "gendb";

    public abstract UserDao userDao();

    public abstract RoundDao roundDao();



}
