package com.example.event.View;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.event.R;
import com.example.event.utils.Utils;

public class GuanHuZhiDuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guan_hu_zhi_du);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("政策法规");
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView permissionText = (TextView) findViewById(R.id.content_text);
        String filename = "XunHuBanFa.html";
        String content = Utils.getStringFromHtmlFile(GuanHuZhiDuActivity.this, filename);
        permissionText.setText(Html.fromHtml(content));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
