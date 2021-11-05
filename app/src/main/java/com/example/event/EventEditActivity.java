package com.example.event;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PCallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.DingTu.Enum.lkGpsFixMode;
import com.example.event.Video.VideoRecordActivity;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.db.xEntity.UploadEntity;
import com.example.event.manager.PatrolManager;
import com.example.event.manager.UploadMananger;
import com.example.event.model.EditBean;
import com.example.event.model.ImgTime;
import com.example.event.utils.PhotoCamera;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EventEditActivity extends AppCompatActivity implements View.OnClickListener {

    public static ICallback photoCallBack = null;
    private TextView mLat, mLon, mHigh, mId, mTvSheng, mTvXian, mTvDiaochaZhe, mTvdate, mTianBiaoRen, mXujiResult, mTvNumber, mTakePerson, mEtAddress, mTakeDate, mShuCnName, mKeCnName, mShuLaName, mKeLaName;
    private EditText mEtXiong, mEtHigh, mBeiZhu;
    private Spinner mSpPoxiang, mSpPodu, mSpPowei, mSpTuTYpe, mZHongCnName, mZHongLaName;
    private EditText mEtGuanFu, mEtZhiHigh, mEtDanZHudes;
    private GridView mGriview, mVideoGridview;
    private Button mBtnphoto, mDelatePhoto, mTextSumbit, mTextBack;
    private RoundExamineEntity mBean;
    private ArrayList<String> mPhotoNameList = new ArrayList<String>();
    private ArrayList<ImgTime> mPhotoTimeList = new ArrayList<>();
    private List<String> mVideoNameList = new ArrayList<String>();
    private List<String> mThumNameList = new ArrayList<String>();
    private String photoPath = PubVar.m_SysAbsolutePath + "/Photo";
    private String smallPhotoPath = photoPath + "/samllPhoto";
    private String tempPhotoName = photoPath + "/TempPhoto.jpg";
    private Dialog mSavingingDlg;
    private List<String> mFilename;
    private List<String> mBigfilename;
    private Button mCaculate;
    private Button mAddPhoto;
    private Button mVideo;
    private Button mDeleteVideo;
    private String mVideoPath = PubVar.m_SysAbsolutePath + "/Video";
    public static PCallback videoCallBack;
    private List<String> delVideos;
    private boolean hasPhoto;
    private String Id;
    private String[] zhongType;
    private String[] laType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        mBean = (RoundExamineEntity) getIntent().getSerializableExtra("StateBean");
        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_back, new RelativeLayout(this), false);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(customView);
        ((TextView) customView.findViewById(R.id.tv_back)).setText("调查编辑");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((ImageView) customView.findViewById(R.id.iv_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initSavingDlg();
        mLat = (TextView) findViewById(R.id.tvLat);
        mLat.setText(mBean.getLatitude() + "");
        mLon = (TextView) findViewById(R.id.tvLon);
        mLon.setText(mBean.getLongtitude() + "");
        mHigh = (TextView) findViewById(R.id.tvHigh);
        mHigh.setText(mBean.getHight() + "");
        mId = (TextView) findViewById(R.id.tv_Id);
        mId.setText(mBean.getOrderNumber());
        mTvSheng = (TextView) findViewById(R.id.tv_sheng);
        mTvSheng.setText(mBean.getSheng());
        mTvXian = (TextView) findViewById(R.id.tv_xian);
        mTvXian.setText(mBean.getXian());
        mTvDiaochaZhe = (TextView) findViewById(R.id.tv_diaocha_zhe);
        mTvDiaochaZhe.setText(mBean.getExmainPerson());
        mTianBiaoRen = (TextView) findViewById(R.id.tv_tianbiao_ren);
        mTianBiaoRen.setText(mBean.getFillPerson());
        mTvdate = (TextView) findViewById(R.id.tv_date);
        mTvdate.setText(mBean.getExamineDate());
        mEtAddress = (TextView) findViewById(R.id.et_address);
        mEtAddress.setText(mBean.getAddress());
        mZHongCnName = (Spinner) findViewById(R.id.sp_zhong_cn_name);

        String zhong = mBean.getZhongCName();
        if(zhong.contains("七叶树")||zhong.contains("天师栗")) {
            zhongType = "浙江七叶树,大果七叶树,长柄七叶树,大叶七叶树,欧洲七叶树,日本七叶树,云南七叶树,天师栗,小果七叶树,澜沧七叶树,多脉七叶树".split(",");
        }else{ 
            zhongType = "欧洲红豆杉,太平洋紫衫,加拿大红豆杉,南方红豆杉,中国红豆杉,东北红豆杉,佛罗里达红豆杉,墨西哥红豆杉,南洋红豆杉,西藏红豆杉,云南红豆杉".split(",");
        }
        ArrayAdapter<String> zhongTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,android.R.layout.simple_spinner_item,zhongType);
        zhongTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mZHongCnName.setAdapter(zhongTypeAdapter);
        for (int i = 0; i < zhongType.length; i++) {
            if (zhong.equals(zhongType[i])) {
                mZHongCnName.setSelection(i, true);
            }
        }

        mShuCnName = (TextView) findViewById(R.id.et_shu_cn_name);
        if (mBean.getShuCName() != null) {
            mShuCnName.setText(mBean.getShuCName());
        }
        mKeCnName = (TextView) findViewById(R.id.et_ke_cn_name);
        if (mBean.getKeCName() != null) {
            mKeCnName.setText(mBean.getKeCName());
        }
        mZHongLaName = (Spinner) findViewById(R.id.sp_zhong_la_name);
        String laname = mBean.getZhongLaName();
        if(zhong.contains("七叶树")||zhong.contains("天师栗")){
            laType = "Aesculus chinensis Bunge var.chekiangensis (Hu et Fang)Fang,Aesculus chuniana Hu et Fang,Aesculus assamica Griff.,Aesculus megaphylla Hu et Fang,Aesculus hippocastanum L.,Aesculus turbinata Bl.,Aesculus wangii Hu,Aesculus wilsonii Rehd.,Aesculus tsiangii,Aesculus lantsangensis Hu & W. P. Fang,Aesculus polyneura Hu et Fang".split(",");
        }else{
            laType="Taxus baccata L.,Taxus brevifolia,Taxus canadensis Marshall,Taxus wallichiana var. mairei (Leme & H. Lv.) L. K. Fu & Nan Li,Taxus chinensis（Pilger）Rehd,Taxus cuspidata Siebold & Zucc.,Taxus floridana Nutt. ex Chapm.,Taxus globosa Schltdl.,Taxus sumatrana,Taxus wallichiana Zucc.,Taxus yunnanensis Cheng et L. K. Fu".split(",");;
        }

        ArrayAdapter<String> laTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context, android.R.layout.simple_spinner_item, laType);
        laTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mZHongLaName.setAdapter(laTypeAdapter);
        for (int i = 0; i < laType.length; i++) {
            if (laname.equals(laType[i])) {
                mZHongLaName.setSelection(i, true);
            }
        }
        mZHongCnName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mZHongLaName.setSelection(0, true);
                } else if (position == 1) {
                    mZHongLaName.setSelection(1, true);
                } else if (position == 2) {
                    mZHongLaName.setSelection(2, true);
                } else if (position == 3) {
                    mZHongLaName.setSelection(3, true);
                } else if (position == 4) {
                    mZHongLaName.setSelection(4, true);
                } else if (position == 5) {
                    mZHongLaName.setSelection(5, true);
                } else if (position == 6) {
                    mZHongLaName.setSelection(6, true);
                } else if (position == 7) {
                    mZHongLaName.setSelection(7, true);
                } else if (position == 8) {
                    mZHongLaName.setSelection(8, true);
                } else if (position == 9) {
                    mZHongLaName.setSelection(9, true);
                } else if (position == 10) {
                    mZHongLaName.setSelection(10, true);
                } else if (position == 11) {
                    mZHongLaName.setSelection(11, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mZHongLaName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    mZHongCnName.setSelection(0, true);
                } else if (position == 1) {
                    mZHongCnName.setSelection(1, true);
                } else if (position == 2) {
                    mZHongCnName.setSelection(2, true);
                } else if (position == 3) {
                    mZHongCnName.setSelection(3, true);
                } else if (position == 4) {
                    mZHongCnName.setSelection(4, true);
                } else if (position == 5) {
                    mZHongCnName.setSelection(5, true);
                } else if (position == 6) {
                    mZHongCnName.setSelection(6, true);
                } else if (position == 7) {
                    mZHongCnName.setSelection(7, true);
                } else if (position == 8) {
                    mZHongCnName.setSelection(8, true);
                } else if (position == 9) {
                    mZHongCnName.setSelection(9, true);
                } else if (position == 10) {
                    mZHongCnName.setSelection(10, true);
                } else if (position == 11) {
                    mZHongCnName.setSelection(11, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mShuLaName = (TextView) findViewById(R.id.et_shu_la_name);
        if (mBean.getShuLaName() != null) {
            mShuLaName.setText(mBean.getShuLaName());
        }
        mKeLaName = (TextView) findViewById(R.id.et_ke_la_name);
        if (mBean.getKeLaName() != null) {
            mKeLaName.setText(mBean.getKeLaName());
        }

        mSpPoxiang = (Spinner) findViewById(R.id.sp_po_xiang);
        mSpPodu = (Spinner) findViewById(R.id.sp_po_du);
        mSpPowei = (Spinner) findViewById(R.id.sp_po_wei);

        String poxiang = mBean.getPoXiang();
        String[] poXiangType = "东,西,南,北,东南,东北,西南,西北,无".split(",");
        ArrayAdapter<String> poXiangTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                poXiangType);
        poXiangTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPoxiang.setAdapter(poXiangTypeAdapter);
        for (int i = 0; i < poXiangType.length; i++) {
            if (poxiang.equals(poXiangType[i])) {
                mSpPoxiang.setSelection(i, true);
            }
        }

        String poWei = mBean.getPoWei();
        String[] poWeiType = "山脊,上坡,中坡,下坡,山谷,平地".split(",");
        ArrayAdapter<String> poWeiTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                poWeiType);
        poWeiTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPowei.setAdapter(poWeiTypeAdapter);

        for (int i = 0; i < poWeiType.length; i++) {
            if (poWei.equals(poWeiType[i])) {
                mSpPowei.setSelection(i, true);
            }
        }

        String poDu = mBean.getPoDu();
        String[] poDuType = "平(0°-5°),缓(6°-15°）,斜(16°-25°）,陡(26°-35°）,急(36°-45°）,险(>46°)".split(",");
        ArrayAdapter<String> poDuTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                poDuType);
        poWeiTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPodu.setAdapter(poDuTypeAdapter);

        for (int i = 0; i < poDuType.length; i++) {
            if (poDu.equals(poDuType[i])) {
                mSpPodu.setSelection(i, true);
            }
        }

        mEtHigh = (EditText) findViewById(R.id.et_tree_high);
        mEtHigh.setText(mBean.getTreeHight() + "");
        mEtXiong = (EditText) findViewById(R.id.et_xiong_jin);
        mEtXiong.setText(mBean.getXiongJin() + "");
        mEtGuanFu = (EditText) findViewById(R.id.et_guan_fu);
        mEtGuanFu.setText(mBean.getGuanFu() + "");
        mEtZhiHigh = (EditText) findViewById(R.id.et_zhi_hight);
        mEtZhiHigh.setText(mBean.getZhiHight() + "");
        mXujiResult = (TextView) findViewById(R.id.tv_xuji_result);
        mXujiResult.setText(mBean.getXuji() + "m³");
        mSpTuTYpe = (Spinner) findViewById(R.id.sp_tu_type);
        String tutype = mBean.getTuType();
        String[] tuType = "砖红壤,赤红壤,红壤,黄壤,黄红壤,黄棕壤,棕壤,暗棕壤,灰黑土,漂灰土,燥红土,褐土,塿土,灰褐土,黑垆土,黑钙土,栗钙土,棕钙土,灰钙土,灰漠土,灰棕漠土,棕漠土,沼泽土,水稻土,黑土,白浆土,潮土,砂姜黑土,灌淤土,绿洲土,草甸土,盐土,碱土,紫色土,石灰土,磷质石灰土,黄绵土,风沙土,火山灰土,山地草甸土,亚高山草甸土,高山草甸土,亚高山草原土,高山草原土,亚高山漠土,高山漠土,高山寒冰土,洪积冲积土,石骨土,耕作土".split(",");
        ArrayAdapter<String> tuTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                tuType);
        poWeiTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpTuTYpe.setAdapter(tuTypeAdapter);
        for (int i = 0; i < tuType.length; i++) {
            if (tutype.equals(tuType[i])) {
                mSpTuTYpe.setSelection(i, true);
            }
        }
        mEtDanZHudes = (EditText) findViewById(R.id.et_danzhu_descibe);
        mEtDanZHudes.setText(mBean.getImportDescribe());
        mTvNumber = (TextView) findViewById(R.id.tv_number);
        mTvNumber.setText(mBean.getPhotoOrderNum());
        mTakePerson = (TextView) findViewById(R.id.tv_take_person);
        mTakePerson.setText(mBean.getTakePerson());
        mTakeDate = (TextView) findViewById(R.id.et_take_date);
        mTakeDate.setText(mBean.getTakeDate());
        mBeiZhu = (EditText) findViewById(R.id.et_bei_zhu);
        mBeiZhu.setText(mBean.getStateDescribe());
        mGriview = (GridView) findViewById(R.id.gvList);
        mVideoGridview = (GridView) findViewById(R.id.video_List);
        mBtnphoto = (Button) findViewById(R.id.btn_photo);
        mBtnphoto.setOnClickListener(this);
        mDelatePhoto = (Button) findViewById(R.id.btn_deletephoto);
        mDelatePhoto.setOnClickListener(this);
        mVideo = (Button) findViewById(R.id.btn_video);
        mVideo.setOnClickListener(this);
        mDeleteVideo = (Button) findViewById(R.id.btn_delete_video);
        mDeleteVideo.setOnClickListener(this);
        mTextSumbit = (Button) findViewById(R.id.text_submit);
        mTextSumbit.setOnClickListener(this);
        mTextBack = (Button) findViewById(R.id.text_back);
        mTextBack.setOnClickListener(this);
        mCaculate = (Button) findViewById(R.id.btn_cacuate);
        mCaculate.setOnClickListener(this);
        mAddPhoto = (Button) findViewById(R.id.btn_add_photo);
        mAddPhoto.setOnClickListener(this);
        photoCallBack = new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                photoCallbackResult(Str, ExtraStr);
            }
        };
        videoCallBack = new PCallback() {
            @Override
            public void OnClick(String Str, String ThumPath, String VideoPath) {
//                VideoCallbackResult(Str, ThumPath, VideoPath);
            }
        };
        showSavePhotos();
//        showSaveVideo();

    }

//    private void VideoCallbackResult(String result, String thumPath, String videoPath) {
//        if (result.equals("ok")) {
//            mVideoNameList.add(0, videoPath);
//            mThumNameList.add(0, thumPath);
//        }
//        showVideo();
//    }

//    private void showSaveVideo() {
//        if (mBean.getVideoList() != null) {
//            String[] mVideoList = mBean.getVideoList().split(",");
//            if (mVideoList != null && mVideoList.length > 0) {
//                for (int i = 0; i < mVideoList.length; i++) {
//                    mVideoNameList.add(mVideoList[i]);
//                }
//            }
//        }
//        if (mBean.getVideoThumList() != null) {
//            String[] mThumList = mBean.getVideoThumList().split(",");
//            if (mThumList != null && mThumList.length > 0) {
//                for (int i = 0; i < mThumList.length; i++) {
//                    mThumNameList.add(mThumList[i]);
//                }
//
//            }
//        }
//
//        String[] from = {"image", "text", "check"};
//        int[] to = {R.id.iv_video_image, R.id.tv_video_info, R.id.cb_video_select};
//        ArrayList<HashMap<String, Object>> data_list = new ArrayList<HashMap<String, Object>>();
//        if(mVideoNameList!=null) {
//            for (int i = 0; i < mVideoNameList.size(); i++) {
//                HashMap<String, Object> map = new HashMap<String, Object>();
//                map.put("image", mThumNameList.get(i));
//                map.put("text", mVideoNameList.get(i));
//                map.put("check", false);
//                data_list.add(map);
//            }
//        }
//        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.video_list_item, from, to);
//        mVideoGridview.setAdapter(sim_adapter);
//        mVideoGridview.setOnItemClickListener(new videoItemClickListener());
//        mVideoGridview.invalidate();
//
//    }

    private class videoItemClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //在本例中arg2=arg3
            HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
            //显示所选Item的ItemText
            String truePath = item.get("text").toString();
            File file = new File(truePath);
            Intent it = new Intent(Intent.ACTION_VIEW);
            Uri mUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mUri = FileProvider.getUriForFile(PubVar.m_DoEvent.m_Context, PubVar.m_DoEvent.m_Context.getPackageName() + ".fileprovider", file);
                it.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                mUri = Uri.parse("file://" + file.getPath());
            }
            it.setDataAndType(mUri, "video/mp4");
            startActivity(it);
        }

    }

    private void showSavePhotos() {
        String[] mPhotoList = new String[0];
        if (mBean.getPhotoList() != null) {
            mPhotoList = mBean.getPhotoList().split(",");
            if (mPhotoList != null && mPhotoList.length > 0) {
                for (int i = 0; i < mPhotoList.length; i++) {
                    mPhotoNameList.add(mPhotoList[i]);
                }
            }
        }
        if (mBean.getTakeDate() != null && mBean.getPhotoList() != null) {
            String[] takeTime = mBean.getTakeDate().split(";");
            if (takeTime != null && takeTime.length > 0) {
                for (int j = 0; j < takeTime.length; j++) {
                    ImgTime time = new ImgTime();
                    time.setName(mPhotoList[j]);
                    time.setTime(takeTime[j]);
                    mPhotoTimeList.add(time);
                }
            }
        }
        String[] from = {"image", "text", "check"};
        int[] to = {R.id.iv_image, R.id.tv_info, R.id.cb_select};
        ArrayList<HashMap<String, Object>> data_list = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < mPhotoNameList.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image", smallPhotoPath + "/" + mPhotoNameList.get(i));
            map.put("text", mPhotoNameList.get(i));
            map.put("check", false);
            data_list.add(map);
        }
        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.photolistitem, from, to);

        Log.i("gridView", "gridView item:" + data_list.size());
        mGriview.setAdapter(sim_adapter);
        mGriview.setOnItemClickListener(new photoItemClickListener());
        mGriview.invalidate();

    }


    private void initSavingDlg() {

        mSavingingDlg = new Dialog(EventEditActivity.this, R.style.loginingDlg);
        mSavingingDlg.setContentView(R.layout.logindlg);

        Window window = mSavingingDlg.getWindow();
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
        ((TextView) mSavingingDlg.findViewById(R.id.tv_loading_show)).setText("正在处理照片");
        params.width = cxScreen;
        params.height = height;
        // width,height表示mLoginingDlg的实际大小
        mSavingingDlg.setCanceledOnTouchOutside(false);
//        mSavingingDlg.setCanceledOnTouchOutside(true); // 设置点击Dialog外部任意区域关闭Dialog
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_photo:
                String address = mEtAddress.getText().toString().trim();
                String mDiaoPerson = mTvDiaochaZhe.getText().toString().trim();
                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(getApplicationContext(), "请填写调查地点!", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mDiaoPerson)) {
                    Toast.makeText(getApplicationContext(), "请设置调查人!", Toast.LENGTH_SHORT).show();
                } else {
                    takePhoto();
                }
                break;
            case R.id.btn_deletephoto:
                deletePhoto();
                break;
            case R.id.text_submit:
                SaveData();
                break;
            case R.id.text_back:
                finish();
                break;
            case R.id.btn_cacuate:
                caculate();
                break;
            case R.id.btn_add_photo:
                AddPhoto();
                break;
            case R.id.btn_video:
                OpenVideo();
                break;

            case R.id.btn_delete_video:
                deleteVideo();
                break;


        }
    }


    private void deleteVideo() {
        ListAdapter adapter = mVideoGridview.getAdapter();
        delVideos = new ArrayList<String>();
        List<String> delPhotos = new ArrayList<>();
        if (adapter != null && adapter.getCount() > 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                HashMap<String, Object> map = (HashMap<String, Object>) adapter.getItem(i);
                View view = mVideoGridview.getChildAt(i);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_video_select);
                if (checkBox.isChecked()) {
//                    File file = new File(map.get("text").toString());
//                    if (delFile(file.getParentFile())) {
//                        Log.d("TAG", "删除成功");
//                    }
                    for (String f : mVideoNameList) {
                        if (f.equals(map.get("text").toString())) {
                            delVideos.add(f);
                        }
                    }
                    for (String f : mThumNameList) {
                        if (f.equals(map.get("image").toString())) {
                            delPhotos.add(f);
                        }
                    }


                }
            }
        }

        for (String f : delPhotos) {
            mThumNameList.remove(f);
        }
        for (String f : delVideos) {
            mVideoNameList.remove(f);
        }
        showVideo();
    }


    private void showVideo() {
        String[] from = {"image", "text", "check"};
        int[] to = {R.id.iv_video_image, R.id.tv_video_info, R.id.cb_video_select};
        List data_list = new ArrayList<HashMap<String, Object>>();
        if (mVideoNameList != null) {
            for (int i = 0; i < mVideoNameList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("image", mThumNameList.get(i));
                map.put("text", mVideoNameList.get(i));
                map.put("check", false);
                data_list.add(map);
            }

        }
        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.video_list_item, from, to);
        //配置适配器
        mVideoGridview.setAdapter(sim_adapter);
        mVideoGridview.setOnItemClickListener(new videoItemClickListener());
        mVideoGridview.invalidate();

    }


    static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }


    private void OpenVideo() {
        File file = new File(mVideoPath);
        if (!file.exists()) {
            file.mkdir();
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String path = mVideoPath + "/" + sdf.format(date);
        File files = new File(path);
        if (!files.exists()) {
            files.mkdir();
        }
        PubVar.mPath = path;
        Intent intent = new Intent(PubVar.m_DoEvent.m_Context, VideoRecordActivity.class);
        PubVar.m_DoEvent.m_Context.startActivity(intent);
    }


    private void AddPhoto() {
        Intent intent = new Intent();
        /* 开启Pictures画面Type设定为image */
        intent.setType("image/*");
        /* 使用Intent.ACTION_GET_CONTENT这个Action */
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /* 取得相片后返回本画面 */
        startActivityForResult(intent, 0x110);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x110 && resultCode == RESULT_OK && null != data) {
            Uri uri = data.getData();
            String img_url = uri.getPath();//这是本机的图片路径
            if (img_url != null && !TextUtils.isEmpty(img_url) && img_url.contains("优良树调查")) {
                img_url = img_url.replace("/root" + photoPath + "/", "");
                mPhotoNameList.add(img_url);
                ImgTime time = new ImgTime();
                time.setName(img_url);
                try {
                    Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(img_url.replace(".jpg", ""));
                    String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                    time.setTime(now);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mPhotoTimeList.add(time);

                showPhotos();
                if (mPhotoNameList != null && mPhotoNameList.size() > 0) {
                    mTvNumber.setText("");
                    for (int i = 0; i < mPhotoNameList.size(); i++) {
                        mTvNumber.append(mPhotoNameList.get(i).substring(0, mPhotoNameList.get(i).indexOf(".")) + "_" + (i + 1) + ",");
                    }
                }
                if (mPhotoTimeList != null && mPhotoTimeList.size() > 0) {
                    mTakeDate.setText("");
                    for (int j = 0; j < mPhotoTimeList.size(); j++) {
                        mTakeDate.append(mPhotoTimeList.get(j).getTime() + ";");
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "暂不支持其他路径下的照片!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void caculate() {
        String name = mZHongCnName.getSelectedItem().toString().trim();
        Double xishu = 0.42;
        if (name.equals("冷杉")) {
            xishu = 0.45;
        } else if (name.equals("华山松") || name.equals("铁杉") || name.equals("泡桐")) {
            xishu = 0.43;
        } else if (name.equals("油松")) {
            xishu = 0.42;
        } else if (name.equals("山杨") || name.equals("光皮桦")) {
            xishu = 0.41;
        } else if (name.equals("栎类")) {
            xishu = 0.4;
        } else if (name.equals("红桦") || name.equals("白桦") || name.equals("马尾松") || name.equals("杂木") || name.equals("刺槐")) {
            xishu = 0.39;
        } else if (name.equals("侧柏")) {
            xishu = 0.38;
        }
        DecimalFormat df = new DecimalFormat("##0.000");
        double hight = Double.valueOf(mEtHigh.getText().toString().trim());
        double xiongJin = Double.valueOf(mEtXiong.getText().toString().trim());
        String XujiStr = df.format((hight + 3) * xishu * xiongJin / 100);
        mXujiResult.setText(XujiStr + "m³");
    }

    private void deletePhoto() {
        ListAdapter adapter = mGriview.getAdapter();
        List<String> delPhotos = new ArrayList<String>();
        mFilename = new ArrayList<>();
        mBigfilename = new ArrayList<>();

        if (adapter != null && adapter.getCount() > 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                HashMap<String, Object> map = (HashMap<String, Object>) adapter.getItem(i);
                View view = mGriview.getChildAt(i);
                if (view != null) {
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_select);
                    if (checkBox.isChecked()) {
                        String fileName = map.get("image") + "";
                        mFilename.add(fileName);
//                        File file = new File(fileName);
//                        if (file.exists()) {
//                            file.delete();
//                        }
                        String bigFileName = fileName.replace("/samllPhoto", "");
                        mBigfilename.add(bigFileName);
//                        File bigfile = new File(bigFileName);
//                        if (bigfile.exists()) {
//                            bigfile.delete();
//                        }
//                        bigfile.delete();

                        for (String f : mPhotoNameList) {
                            if (f.equals(map.get("text") + "")) {
                                delPhotos.add(f);
                            }
                        }
                    }
                }
            }
            if (delPhotos != null && delPhotos.size() > 0) {
                for (String f : delPhotos) {
                    mPhotoNameList.remove(f);
                }
            }

            if (delPhotos != null && delPhotos.size() > 0) {
                for (String f : delPhotos) {
                    for (int i = 0; i < mPhotoTimeList.size(); i++) {
                        if (f.equals(mPhotoTimeList.get(i).getName())) {
                            mPhotoTimeList.remove(i);
                        }
                    }
                }
            }
            if (mPhotoNameList != null && mPhotoNameList.size() >= 0) {
                mTvNumber.setText("");
                for (int i = 0; i < mPhotoNameList.size(); i++) {
                    mTvNumber.append(mPhotoNameList.get(i).substring(0, mPhotoNameList.get(i).indexOf(".")) + "_" + (i + 1) + ",");
                }
            }

            if (mPhotoTimeList != null && mPhotoTimeList.size() >= 0) {
                mTakeDate.setText("");
                for (int i = 0; i < mPhotoTimeList.size(); i++) {
                    mTakeDate.append(mPhotoTimeList.get(i).getTime() + ";");
                }
            }
            showPhotos();
        }

    }


    //拍照完成后回调
    public void photoCallbackResult(String resultCode, final Object ExtraStr) {
        if (!resultCode.equals("1")) {
            Log.i("事件拍照回调", "回调校验码：" + resultCode);
            return;
        }

        if (!Tools.ExistFile(tempPhotoName)) {
            Log.i("事件拍照回调", "没有写入文件：" + resultCode);
            return;
        }

        try {
            if (mSavingingDlg != null) {
                mSavingingDlg.show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "打开SavingDlg" + ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String PhotoFileName = simpleDateFormat.format(new java.util.Date()) + ".jpg";
                File f1 = new File(tempPhotoName.toString());

                boolean isFailed = true;
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPurgeable = true;
                    options.inMutable = true;
                    options.inInputShareable = true;
                    FileInputStream iSteam = new FileInputStream(tempPhotoName);
                    Bitmap bitmap = BitmapFactory.decodeStream(iSteam, null, options);
                    iSteam.close();
                    Log.v("tag", "读取照片。。。");

                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    Canvas canvasTemp = new Canvas(bitmap);


                    Paint p = new Paint();
                    String familyName = "宋体";
                    Typeface font = Typeface.create(familyName, Typeface.BOLD);
                    p.setColor(Color.RED);
                    p.setTypeface(font);
                    p.setTextSize(50);

                    String maddress = mEtAddress.getText().toString().trim();
                    String mDiaoPerson = mTvDiaochaZhe.getText().toString().trim();

                    String examPerson = "调查人:" + mDiaoPerson;
                    canvasTemp.drawText(examPerson, 8, h - 250, p);
                    String address = "调查地点:" + maddress;
                    canvasTemp.drawText(address, 8, h - 200, p);

                    String dt = Tools.GetSystemDate();
                    String strCamerTime = "拍摄时间：" + dt;
                    canvasTemp.drawText(strCamerTime, 8, h - 100, p);

                    if (AppSetting.curUser != null && AppSetting.curUser.getLoginName() != null) {
                        canvasTemp.drawText("拍摄者：" + AppSetting.curUser.getLoginName(), 8, h - 50, p);
                    }

                    String pswz = "拍摄位置：未定位";
                    if (PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsFixMode() == lkGpsFixMode.en3DFix &&
                            PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.0001 && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.0001) {
                        try {
                            String[] Coor = PubVar.m_GPSLocate.getJWGPSCoordinate().split(",");
                            String jd = Tools.GetDDMMSS(Tools.ConvertToDouble(Coor[0]));
                            String wd = Tools.GetDDMMSS(Tools.ConvertToDouble(Coor[1]));
                            pswz = "拍摄位置：" + wd + "," + jd + "," + PubVar.m_GPSLocate.getGPSCoordinate().getZ();

                            Log.v("tag", "添加拍摄位置。。。");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                    canvasTemp.drawText(pswz, 8, h - 150, p);

                    canvasTemp.save();
                    canvasTemp.restore();

                    FileOutputStream fos = new FileOutputStream(f1);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    fos = null;
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    FileInputStream iSteam2 = new FileInputStream(tempPhotoName);
                    Bitmap bitmap2 = BitmapFactory.decodeStream(iSteam2);
                    File smallF = new File(AppSetting.smallPhotoPath + "/" + PhotoFileName);
                    FileOutputStream f = new FileOutputStream(smallF);
                    Bitmap b = Bitmap.createScaledBitmap(bitmap2, bitmap2.getWidth() / 8, bitmap2.getHeight() / 8, false);
                    b.compress(Bitmap.CompressFormat.JPEG, 100, f);

                    isFailed = false;
                    f.flush();
                    f.close();
                    f = null;

                    if (!b.isRecycled()) {
                        b.recycle();
                    }

                    f1.renameTo(new File(photoPath + "/" + PhotoFileName));
                    mPhotoNameList.add(PhotoFileName);
                    ImgTime time = new ImgTime();
                    time.setName(PhotoFileName);
                    time.setTime(dt);
                    mPhotoTimeList.add(time);

                    mGriview.post(new Runnable() {
                        @Override
                        public void run() {
                            showPhotos();
                            if (mPhotoNameList != null && mPhotoNameList.size() > 0) {
                                mTvNumber.setText("");
                                for (int i = 0; i < mPhotoNameList.size(); i++) {
                                    mTvNumber.append(mPhotoNameList.get(i).substring(0, mPhotoNameList.get(i).indexOf(".")) + "_" + (i + 1) + ",");
                                }
                            }
                            if (mPhotoTimeList != null && mPhotoTimeList.size() > 0) {
                                mTakeDate.setText("");
                                for (int j = 0; j < mPhotoTimeList.size(); j++) {
                                    mTakeDate.append(mPhotoTimeList.get(j).getTime() + ";");
                                }
                            }
                        }
                    });
                    String fileName = AppSetting.photoPath + "/" + PhotoFileName;
                    JSONObject exif = (JSONObject) ExtraStr;
                    //存储exif信息

                    try {
                        if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null) {
                            String[] Coor = PubVar.m_GPSLocate.getJWGPSCoordinate().split(",");

                            ExifInterface exifInfo = new ExifInterface(fileName);
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Tools.ConvertToSexagesimal(Coor[0]));
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Tools.ConvertToSexagesimal(Coor[1]));
                            String[] GPSDateTime = PubVar.m_GPSLocate.getGPSDateForPhotoFormat();
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, GPSDateTime[1]);
                            exifInfo.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, GPSDateTime[0]);

                            if (PubVar.m_GPSLocate.m_LocationEx != null) {
                                exif.put("lat:", PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude());
                                exif.put("lon:", PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude());
                                exif.put("gpsTime", PubVar.m_GPSLocate.m_LocationEx.GetGpsDate() + " " + PubVar.m_GPSLocate.m_LocationEx.GetGpsTime());
                            }

                            Log.d("exif save", exif.toString());
                            exifInfo.setAttribute(ExifInterface.TAG_USER_COMMENT, exif.toString());
                            exifInfo.saveAttributes();
                        }


                    } catch (Exception io) {
                        //TODO:save to log
                        io.printStackTrace();
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (mSavingingDlg != null && mSavingingDlg.isShowing()) {
                        mSavingingDlg.dismiss();
                    }
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "关闭SavingDlg" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }).start();
    }


    private void takePhoto() {

        if (!Tools.ExistFile(photoPath)) {
            boolean createPath = (new File(photoPath)).mkdirs();
            Log.i("createPath", createPath + "");
        }

        if (!Tools.ExistFile(smallPhotoPath)) {
            (new File(smallPhotoPath)).mkdirs();
        }

        Intent photoCamera = new Intent(this, PhotoCamera.class);
        Log.i("拍照", "打开相机");
        photoCamera.putExtra("PhotoPath", photoPath);
        photoCamera.putExtra("TempPhoto", "TempPhoto.jpg");
        photoCamera.putExtra("from", "EventEditActivity");
        this.startActivity(photoCamera);
    }


    private void SaveData() {
        boolean okay = true;
        String id = mId.getText().toString();
        String sheng = mTvSheng.getText().toString().trim();
        String xian = mTvXian.getText().toString().trim();
        String maddress = mEtAddress.getText().toString().trim();
        String mDiaoPerson = mTvDiaochaZhe.getText().toString().trim();
        String mTianBiao = mTianBiaoRen.getText().toString().trim();
        String mFillDate = mTvdate.getText().toString().trim();
        String mCNzhong = mZHongCnName.getSelectedItem().toString().trim();
        String mCNshu = mShuCnName.getText().toString().trim();
        String mCNke = mKeCnName.getText().toString().trim();
        String mLaZhong = mZHongLaName.getSelectedItem().toString().trim();
        String mLaShu = mShuLaName.getText().toString().trim();
        String mLaKe = mKeLaName.getText().toString().trim();
        String lat = mLat.getText().toString().trim();
        String lot = mLon.getText().toString().trim();
        String haiba = mHigh.getText().toString().trim();
        String mPoxiang = mSpPoxiang.getSelectedItem().toString().trim();
        String mPoDu = mSpPodu.getSelectedItem().toString().trim();
        String mPoWei = mSpPowei.getSelectedItem().toString().trim();
        String treeHight = mEtHigh.getText().toString().trim();
        String xiongD = mEtXiong.getText().toString().trim();
        String guanfu = mEtGuanFu.getText().toString().trim();
        String zhiGao = mEtZhiHigh.getText().toString().trim();
        String xuji = mXujiResult.getText().toString().trim().substring(0, mXujiResult.getText().toString().trim().indexOf("m³"));
        String tuType = mSpTuTYpe.getSelectedItem().toString().trim();
        String mdescibe = mEtDanZHudes.getText().toString().trim();
        String mPhotoNumber = mTvNumber.getText().toString().trim();
        String mTakePersons = mTakePerson.getText().toString().trim();
        String mTakeDates = mTakeDate.getText().toString().trim();
        String mBei = mBeiZhu.getText().toString().trim();

        if (okay) {
            final RoundExamineEntity exam = new RoundExamineEntity();
            exam.setOrderNumber(id);
            exam.setSheng(sheng);
            exam.setXian(xian);
            exam.setAddress(maddress);
            exam.setExmainPerson(mDiaoPerson);
            exam.setFillPerson(mTianBiao);
            exam.setExamineDate(mFillDate);
            exam.setZhongCName(mCNzhong);
            exam.setShuCName(mCNshu);
            exam.setKeCName(mCNke);
            exam.setZhongLaName(mLaZhong);
            exam.setShuLaName(mLaShu);
            exam.setKeLaName(mLaKe);
            exam.setLatitude(Double.parseDouble(lat));
            exam.setLongtitude(Double.parseDouble(lot));
            exam.setHight(Double.parseDouble(haiba));
            exam.setPoXiang(mPoxiang);
            exam.setPoDu(mPoDu);
            exam.setPoWei(mPoWei);
            if (treeHight != null && !TextUtils.isEmpty(treeHight)) {
                double treeHigh = Double.valueOf(treeHight);
                BigDecimal bd = new BigDecimal(treeHigh).setScale(0, BigDecimal.ROUND_HALF_UP);
                exam.setTreeHight(Integer.valueOf(bd.toString()));
            }
            if (xiongD != null && !TextUtils.isEmpty(xiongD)) {
                double xiong = Double.valueOf(xiongD);
                BigDecimal bd = new BigDecimal(xiong).setScale(0, BigDecimal.ROUND_HALF_UP);
                exam.setXiongJin(Integer.valueOf(bd.toString()));
            }
            if (guanfu != null && !TextUtils.isEmpty(guanfu)) {
                double fu = Double.valueOf(guanfu);
                BigDecimal bd = new BigDecimal(fu).setScale(0, BigDecimal.ROUND_HALF_UP);
                exam.setGuanFu(Integer.valueOf(bd.toString()));
            }
            if (zhiGao != null && !TextUtils.isEmpty(zhiGao)) {
                exam.setZhiHight(Double.valueOf(zhiGao));
            }
            if (xuji != null && !TextUtils.isEmpty(xuji)) {
                exam.setXuji(Double.valueOf(xuji));
            }
            exam.setTuType(tuType);
            exam.setImportDescribe(mdescibe);
            exam.setPhotoOrderNum(mPhotoNumber);
            exam.setTakePerson(mTakePersons);
            exam.setTakeDate(mTakeDates);
            exam.setStateDescribe(mBei);
            if (mPhotoNameList != null && mPhotoNameList.size() > 0) {
                StringBuffer txt = new StringBuffer();
                for (int i = 0; i < mPhotoNameList.size(); i++) {
                    txt.append(mPhotoNameList.get(i) + ",");
                }
                exam.setPhotoList(txt.toString());
            }
            if (mVideoNameList != null && mVideoNameList.size() > 0) {
                StringBuffer video = new StringBuffer();
                StringBuffer videoThum = new StringBuffer();
                for (int i = 0; i < mVideoNameList.size(); i++) {
                    video.append(mVideoNameList.get(i) + ",");
                    videoThum.append(mThumNameList.get(i) + ",");
                }
//                exam.setVideoList(video.toString());
//                exam.setVideoThumList(videoThum.toString());

            }
            if (PatrolManager.getInstance().saveExaminEvent(exam)) {
                List<UploadEntity> entity = PatrolManager.getInstance().getUpload();
                if (entity != null && entity.size() > 0) {
                    for(int i=0;i<entity.size();i++){
                        if(entity.get(i).getOrderNumber().equals(exam.getOrderNumber())){
                            Id=entity.get(i).getId();
                        }
                    }

                    UploadMananger.getInstance().EditEvent(Id,exam, new ICallback() {
                        @Override
                        public void OnClick(String Str, Object ExtraStr) {
                            if (Str.equals("success")) {
                                UploadPhoto(0, exam, ExtraStr.toString());
                                Toast.makeText(getApplicationContext(), "当前修改的单株调查信息,已上传成功!", Toast.LENGTH_SHORT).show();
                            } else if (Str.equals("failed")) {
                                if (ExtraStr != null) {

                                } else {
                                    Toast.makeText(getApplicationContext(), "当前修改的单株调查信息,已上传失败!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    });
                }
                if (mFilename != null && mFilename.size() > 0) {
                    for (int i = 0; i < mFilename.size(); i++) {
                        File file = new File(mFilename.get(i));
                        if (file.exists()) {
                            file.delete();
                        }
                        File bigfile = new File(mBigfilename.get(i));
                        if (bigfile.exists()) {
                            bigfile.delete();
                        }
                        bigfile.delete();

                    }
                }
                if (delVideos != null && delVideos.size() > 0) {
                    for (int i = 0; i < delVideos.size(); i++) {
                        File file = new File(delVideos.get(i));
                        if (delFile(file.getParentFile())) {
                            Log.d("TAG", "删除成功");
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), "保存成功！", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "保存失败！", Toast.LENGTH_SHORT).show();
            }

        }

    }


    private void UploadPhoto(final int i, final RoundExamineEntity entity, final String data) {
        String mPhoto = entity.getPhotoList();
        if (mPhoto != null && mPhoto.split(",").length > 0) {
            hasPhoto = i < mPhoto.split(",").length;
            if (hasPhoto) {
                UploadMananger.getInstance().uploadPhotoes(entity, entity.getPhotoList().split(",")[i], data, new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        if (Str.equals("success")) {
                            Toast.makeText(getApplicationContext(), "当前修改的调查信息,第" + (i + 1) + "张照片,上传成功!", Toast.LENGTH_SHORT).show();
                        } else if (Str.equals("failed")) {
                            Toast.makeText(getApplicationContext(), "当前修改的调查信息,第" + (i + 1) + "张照片,上传失败!", Toast.LENGTH_SHORT).show();
                        }
                        UploadPhoto(i + 1, entity, data);

                    }
                });
            }

        }

    }


    private void showPhotos() {
        String[] from = {"image", "text", "check"};
        int[] to = {R.id.iv_image, R.id.tv_info, R.id.cb_select};
        ArrayList<HashMap<String, Object>> data_list = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < mPhotoNameList.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image", smallPhotoPath + "/" + mPhotoNameList.get(i));
            map.put("text", mPhotoNameList.get(i));
            map.put("check", false);
            data_list.add(map);
        }
        //sim_adapter = new ImageListAdapter(mOwnActivity, data_list, R.layout.photolistitem, from, to);
        SimpleAdapter sim_adapter = new SimpleAdapter(this, data_list, R.layout.photolistitem, from, to);

        Log.i("gridView", "gridView item:" + data_list.size());
        mGriview.setAdapter(sim_adapter);
        mGriview.setOnItemClickListener(new photoItemClickListener());
        mGriview.invalidate();
    }


    //照片点击展示
    class photoItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened
                                View arg1,//The view within the AdapterView that was clicked
                                int arg2,//The position of the view in the adapter
                                long arg3//The row id of the item that was clicked
        ) {
            //在本例中arg2=arg3
            HashMap<String, Object> item = (HashMap<String, Object>) arg0.getItemAtPosition(arg2);
            //显示所选Item的ItemText

            Intent it = new Intent(Intent.ACTION_VIEW);
            String photoName = item.get("image") + "";
            photoName = photoName.replace("/samllPhoto", "");
            File file = new File(photoName);
            Uri fileName = FileProvider.getUriForFile(getApplicationContext(), "com.example.event.fileprovider", file);
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            it.setDataAndType(fileName, "image/*");

            startActivity(it);
        }
    }


}
