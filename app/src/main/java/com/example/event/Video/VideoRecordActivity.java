package com.example.event.Video;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.DingTu.Base.PubVar;
import com.example.event.R;

import java.io.File;

public class VideoRecordActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File file=new File(PubVar.mPath);
        if(file.isDirectory()){
            File []files=file.listFiles();
            if(files.length==0){
                file.delete();
            }
        }
    }
}
