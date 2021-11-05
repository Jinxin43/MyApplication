package com.example.event.Login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.event.AppSetting;
import com.example.event.MainActivity;
import com.example.event.R;
import com.example.event.db.xEntity.UserEntity;
import com.example.event.http.Httpmodel.HttpRegisterModel;
import com.example.event.http.Httpmodel.HttpUserModel;
import com.example.event.http.RetrofitHttp;
import com.example.event.manager.UserManager;
import com.example.event.utils.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.event.Login.LoginActivity.preferences;

public class RegisterActivity extends AppCompatActivity {

    private EditText mRegisterPhone, mRegisterUnit, mProjectName, mRegisterUserName, mRegisterPassword, mPasswordSure;
    private Dialog mLoginingDlg;
    private String mPhone,mPassword;
    private List<UserEntity> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initLoginingDlg();
        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_back, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(customView);
        ((TextView) customView.findViewById(R.id.tv_back)).setText("注册");
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

    private void initView() {
        mRegisterPhone = (EditText) findViewById(R.id.register_phone_num);
        mRegisterUnit = (EditText) findViewById(R.id.register_unit_name);
        mProjectName = (EditText) findViewById(R.id.project_name);
        mRegisterUserName = (EditText) findViewById(R.id.register_user_name);
        mRegisterPassword = (EditText) findViewById(R.id.register_password);
        ((Button) findViewById(R.id.btn_sumbit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhone = mRegisterPhone.getText().toString().trim();
                String Unit = mRegisterUnit.getText().toString().trim();
                String Project = mProjectName.getText().toString().trim();
                String UserName = mRegisterUserName.getText().toString().trim();
                mPassword = mRegisterPassword.getText().toString().trim();
                if (mPhone != null && TextUtils.isEmpty(mPhone)) {
                    Toast.makeText(getApplicationContext(),"手机号未填写!",Toast.LENGTH_SHORT).show();
                } else if (Unit != null && TextUtils.isEmpty(Unit)) {
                    Toast.makeText(getApplicationContext(),"单位名称未填写!",Toast.LENGTH_SHORT).show();
                } else if (UserName != null && TextUtils.isEmpty(UserName)) {
                    Toast.makeText(getApplicationContext(),"用户名未填写!",Toast.LENGTH_SHORT).show();
                } else if (mPassword != null && TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(getApplicationContext(),"密码未填写!",Toast.LENGTH_SHORT).show();
                } else{
                    showLoginingDlg();
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    HttpRegisterModel model = new HttpRegisterModel();
                    model.setPhoneNum(mPhone);
                    model.setUnitName(Unit);
                    model.setProject(Project);
                    model.setUserName(UserName);
                    model.setPassword(mPassword);
                    model.setRePassword(mPassword);
                    prompt(RetrofitHttp.getRetrofit(builder.build()).register("Register", model));
                }
            }
        });

    }

    private void prompt(Call<ResponseBody> register) {
        register.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> reg, Response<ResponseBody> response) {
                try {
                    if (response.body() == null) {
                        return;
                    }
                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {
                        closeLoginingDlg();
                        Toast.makeText(getApplicationContext(), "注册成功!", Toast.LENGTH_SHORT).show();
                        JSONObject data = new JSONObject(result.get("data").toString());
                        UserEntity user = new UserEntity(mPhone, mPassword);
                        String userID=data.getString("Id");
                        SharedPreferences preferences =getSharedPreferences("userID", Context.MODE_PRIVATE);
                        preferences.edit().putString("UserId",userID ).commit();
                        user.setUserName(data.get("Username") + "");
                        user.setLastLoginTime(new Date());
                        user.setMobilePhone(data.get("Phonenumber") + "");
                        if (data.get("Postdiscription") != null) {
                            user.setPostDiscription(data.get("Postdiscription") + "");
                        }

                        if (data.get("Groupid") != null) {
                            user.setDepartmentId(data.get("Groupid") + "");
                        }

                        if (!UserManager.getInstance().SaveLoginUser(user)) {
                            Log.e("save logined user", "can't save logined user");
                        }

                        AppSetting.curUserKey = mPhone;
                        AppSetting.curUser = user;
                        if (mUsers == null) {
                            mUsers = new ArrayList<UserEntity>();
                        }
                        mUsers.add(user);
                        preferences = getSharedPreferences(LoginActivity.Login, MODE_PRIVATE);
                        preferences.edit().putString(LoginActivity.LoginKey, mPhone).apply();
                        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        closeLoginingDlg();// 关闭对话框
                        Toast.makeText(getApplicationContext(), result.get("msg") + "", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception io) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> reg, Throwable t) {
                closeLoginingDlg();// 关闭对话框
                Toast.makeText(getApplicationContext(), "网络不给力", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
