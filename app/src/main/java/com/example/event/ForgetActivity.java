package com.example.event;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.event.Login.LoginActivity;
import com.example.event.http.Httpmodel.HttpModifyModel;
import com.example.event.http.RetrofitHttp;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetActivity extends AppCompatActivity {

    private EditText mOldPassword,mNewPassword,mRegister;
    private Dialog mLoginingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        initLoginingDlg();
        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_back, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(customView);
        ((TextView) customView.findViewById(R.id.tv_back)).setText("修改密码");
        ((ImageView) customView.findViewById(R.id.iv_tools)).setVisibility(View.GONE);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((ImageView) customView.findViewById(R.id.iv_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initView();
        
    }

    private void initLoginingDlg() {
        mLoginingDlg = new Dialog(this, R.style.loginingDlg);
        mLoginingDlg.setContentView(R.layout.logindlg);
        ((TextView)mLoginingDlg.findViewById(R.id.tv_loading_show)).setText("注册中...");
        Window window = mLoginingDlg.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        // 获取和mLoginingDlg关联的当前窗口的属性，从而设置它在屏幕中显示的位置

        // 获取屏幕的高宽
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int cxScreen = dm.widthPixels;
        int cyScreen = dm.heightPixels;

        int height = (int) getResources().getDimension(
                R.dimen.loginingdlg_height);// 高42dp
        int lrMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_lr_margin); // 左右边沿10dp
        int topMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_top_margin); // 上沿20dp

        params.y = (-(cyScreen - height) / 2) + topMargin; // -199
        /* 对话框默认位置在屏幕中心,所以x,y表示此控件到"屏幕中心"的偏移量 */

        params.width = cxScreen;
        params.height = height;
        // width,height表示mLoginingDlg的实际大小

        mLoginingDlg.setCanceledOnTouchOutside(true); // 设置点击Dialog外部任意区域关闭Dialog
    }

    private void initView() {
        mRegister =(EditText)findViewById(R.id.register_user_name);
       mOldPassword =(EditText)findViewById(R.id.register_old_password);
       mNewPassword=(EditText)findViewById(R.id.register_new_password);
        ((Button) findViewById(R.id.btn_sumbit)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String phone=mRegister.getText().toString();
                String oldpassword=mOldPassword.getText().toString();
                String newPassword=mNewPassword.getText().toString();
                if (phone != null && TextUtils.isEmpty(phone)) {
                    Toast.makeText(getApplicationContext(),"手机号未填写!",Toast.LENGTH_SHORT).show();
                } else if (oldpassword != null && TextUtils.isEmpty(oldpassword)) {
                    Toast.makeText(getApplicationContext(),"旧密码未填写!",Toast.LENGTH_SHORT).show();
                } else if (newPassword != null && TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(getApplicationContext(),"新密码未填写!",Toast.LENGTH_SHORT).show();
                } else{
                    showLoginingDlg();
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    HttpModifyModel model = new HttpModifyModel();
                    model.setAccount(phone);
                    model.setOldPassword(oldpassword);
                    model.setNewPassword(newPassword);
                    prompt(RetrofitHttp.getRetrofit(builder.build()).modify("ModifyPassword", model));
                }
            }
        });
    }

    private void prompt(Call<ResponseBody> modify) {
        modify.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() == null) {
                    return;
                }
                try {
                    JSONObject result = new JSONObject(response.body().string());
                    closeLoginingDlg();
                    if (result.get("success").equals(Boolean.TRUE)) {
                        Toast.makeText(getApplicationContext(), "修改成功!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), result.get("msg") + "", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                closeLoginingDlg();// 关闭对话框
                Toast.makeText(getApplicationContext(), "网络不给力", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /* 显示正在登录对话框 */
    private void showLoginingDlg() {
        if (mLoginingDlg != null)
            mLoginingDlg.show();
    }

    /* 关闭正在登录对话框 */
    private void closeLoginingDlg() {
        if (mLoginingDlg != null && mLoginingDlg.isShowing())
            mLoginingDlg.dismiss();
    }
}
