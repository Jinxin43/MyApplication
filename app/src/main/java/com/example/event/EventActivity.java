package com.example.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_back, new RelativeLayout(this), false);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setCustomView(customView);
         ((TextView)customView.findViewById(R.id.tv_back)).setText("调查记录");
        ((ImageView)customView.findViewById(R.id.iv_tools)).setVisibility(View.GONE);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((ImageView)customView.findViewById(R.id.iv_back)).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  finish();
              }
          });
    }


}
