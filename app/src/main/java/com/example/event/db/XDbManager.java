package com.example.event.db;

import android.util.Log;

import com.DingTu.Base.PubVar;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.model.RoundExamine;

import org.xutils.DbManager;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;

/**
 * Created by Dingtu2 on 2018/4/12.
 */

public class XDbManager {

    private static DbManager mDbManager;

    public static DbManager getDb() {
        if (mDbManager == null) {
            synchronized (XDbManager.class) {

                File zbDBFile = new File(PubVar.m_SysAbsolutePath + PubVar.m_SysDictionaryName + "/");
                Log.d("TAG", PubVar.m_SysAbsolutePath + "/" + PubVar.m_SysDictionaryName + "/");
                final DbManager.DaoConfig dbConfig = new DbManager.DaoConfig().setDbDir(zbDBFile);
                dbConfig.setDbVersion(12);
                dbConfig.setDbName("patrol.dbx");
                dbConfig.setDbUpgradeListener(new DbManager.DbUpgradeListener() {

                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        if (oldVersion > 9) {
                            try {
                                db.execNonQuery("alter table roundExamine alter column treeHight double not null");
                                db.execNonQuery("alter table roundExamine alter column XiongJin double not null");
                                db.execNonQuery("alter table roundExamine alter column zhiHight double not null");
                                db.execNonQuery("alter table roundExamine alter column Xuji double not null");
//                                try {
                                    db.addColumn(RoundExamineEntity.class,"videoList");//新增的字段
                                    db.addColumn(RoundExamine.class,"videoThumList");
//                                } catch (DbException e) {
//                                    e.printStackTrace();
//                                }

                            } catch (DbException ex) {
                                ex.printStackTrace();
                            }

                        }

                    }
                });

                mDbManager = x.getDb(dbConfig);
                Log.d("DBpath", "patrol.dbx created");
            }

        }
        return mDbManager;
    }


}
