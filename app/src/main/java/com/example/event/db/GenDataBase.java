package com.example.event.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.event.db.dao.RoundDao;
import com.example.event.db.dao.RoundEventDao;
import com.example.event.db.dao.RoundExamineDao;
import com.example.event.db.dao.TraceDao;
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

    public abstract TraceDao traceDao();

    public abstract RoundEventDao roundEventDao();

    public abstract RoundExamineDao roundExamineDao();


}
