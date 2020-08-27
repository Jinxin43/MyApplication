package com.example.event.utils;

import com.example.event.manager.PhotoManager;

/**
 * Created by Dingtu2 on 2019/3/25.
 */

public class LogHelper {

    private  static  LogHelper instance;
    private LogHelper() {

    }

    public LogHelper getInstance()
    {
        synchronized (PhotoManager.class){
            if(instance == null)
            {
                instance = new LogHelper();
            }

            return instance;
        }
    }


}
