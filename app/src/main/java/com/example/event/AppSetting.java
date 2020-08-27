package com.example.event;

import android.app.Application;

import com.example.event.db.xEntity.PatrolEntity;
import com.example.event.db.xEntity.UserEntity;

import org.xutils.DbManager;

import java.util.HashMap;

/**
 * Created by Dingtu2 on 2017/6/9.
 */

public class AppSetting {
//    public static final String baseUrl = "http://111.20.63.66:8001";//周保
    public static final String baseUrl = "http://114.115.255.125:8001";//华为云
    public static final String serverUrl = "http://114.115.255.125:8011";//上传
    public static UserEntity curUser;
    public static PatrolEntity curRound;
    public static String curUserKey;
    public static String SpecialPatrolPolyLayerId = "T1D11AE4BC2F2497D9EED62FCABBE1F8C";
    public static String SpecialPatrolLineLayerId = "T44289A081F084633931C95343DF06949";
    public static String SpecialPatrolPointLayerId = "T1348D267F62E497E8550A18C52327C55";
    public static Application applicaton;
    public static String photoPath;
    public static String smallPhotoPath;
//    public static final String baseUrl = "http://192.168.1.104:8001";
    public static Boolean isReUpload = false;
    public static HashMap<String, String> myDutyArea = new HashMap<String, String>();
    private static Object mLock = new Object();
    private static DbManager.DaoConfig daoConfig;
    public static Boolean mIsRounding=false;


}
