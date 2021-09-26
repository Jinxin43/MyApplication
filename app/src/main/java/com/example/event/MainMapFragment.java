package com.example.event;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.DingTu.Base.DoEvent;
import com.DingTu.Base.HashValueObject;
import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.Cargeometry.Envelope;
import com.DingTu.Cargeometry.Geometry;
import com.DingTu.Cargeometry.Polygon;
import com.DingTu.Data.RoundGPSLine;
import com.DingTu.Dataset.Dataset;
import com.DingTu.Dataset.SQLiteDataReader;
import com.DingTu.Dataset.Workspace;
import com.DingTu.Enum.lkDataCollectType;
import com.DingTu.Enum.lkGeoLayerType;
import com.DingTu.Enum.lkGeoLayersType;
import com.DingTu.Enum.lkGpsFixMode;
import com.DingTu.GPS.GPSLocate;
import com.DingTu.GPS.GPSMap;
import com.DingTu.GPS.LocationEx;
import com.DingTu.Layer.GeoLayer;
import com.DingTu.Map.Map;
import com.DingTu.Map.StaticObject;
import com.DingTu.Project.BKLayerExplorer;
import com.DingTu.Project.LayerExplorer;
import com.DingTu.Project.ProjectDB;
import com.DingTu.Project.ProjectExplorer;
import com.DingTu.ToolBar.Agent_Measure;
import com.DingTu.mapcontainer.MapControl;
import com.example.event.db.XDbManager;
import com.example.event.db.xEntity.PatrolPointEntity;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.db.xEntity.TraceEntity;
import com.example.event.http.Httpmodel.HttpTraceModel;
import com.example.event.http.Httpmodel.RequestId;
import com.example.event.http.RetrofitHttp;
import com.example.event.manager.PatrolManager;
import com.example.event.manager.ReuploadManager;
import com.example.event.manager.TraceManager;
import com.example.event.manager.UploadMananger;
import com.example.event.manager.UserManager;
import com.example.event.model.PointBean;
import com.example.event.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.DingTu.Base.Tools.GetDDMMSS;
import static com.DingTu.mapcontainer.Tools.ZoomInOutPan;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MainMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static boolean unloadTraces = true;
    public TextView mapViewAreaView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //    private OnFragmentInteractionListener mListener;
    private MapControl mp;
    private View myView;
    //    private boolean mIsRounding = false;
    private boolean isStarting = false;
//    private int[] allDrawButtons = new int[]{R.id.bt_delete_op, R.id.bt_undo_op, R.id.bt_poly_drawline, R.id.bt_poly_gps,
//            R.id.bt_poly_create, R.id.bt_line_drawline, R.id.bt_line_gps, R.id.bt_line_create,
//            R.id.bt_point_draw, R.id.bt_point_coor, R.id.bt_point_gps};
//    private int[] polyButtons = new int[]{R.id.bt_poly_drawline, R.id.bt_poly_gps, R.id.bt_poly_create};
//    private int[] lineButtons = new int[]{R.id.bt_line_drawline, R.id.bt_line_gps, R.id.bt_line_create};
//    private int[] pointButtons = new int[]{R.id.bt_point_draw, R.id.bt_point_coor, R.id.bt_point_gps};

    private int[] m_SelectToolsBarItemIdList = {R.id.bt_poly_drawline, R.id.bt_poly_gps, R.id.bt_line_drawline, R.id.bt_line_gps,
            R.id.bt_point_draw, R.id.bt_point_gps, R.id.btnSelectDraw, R.id.btnStartRound, R.id.btnFullScreen};

    private List<PointBean> mListPoints;
    private final int MSG_SUCCESS_FLAG = 1;
    private final int MSG_FAIL_FLAG = 2;
    private List<Coordinate> m_trackList;
    private EditText mXian, mPerson, mPersonTable;
    private Spinner mSheng;
    public static final String TAG = "diaochasetting";
    public static final String ShengDB = "shengStr";
    public static final String XianDB = "xianStr";
    public static final String DiaoChaZheDB = "diaochazheStr";
    public static final String FillTableDB = "filltableStr";
    //主线程中的handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case MSG_SUCCESS_FLAG:
                    startDraw();
                    break;
                case MSG_FAIL_FLAG:
                    Toast.makeText(getActivity(), "获取巡护点信息失败", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };


    private void startDraw() {
        if (PubVar.m_DoEvent.m_ProjectDB.saveProjectInfo(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem().GetName(), String.valueOf(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem().GetCenterMeridian()))) {
            MainActivity.mMapSettingCallbak.OnClick("", null);
        }
        if (mListPoints != null && mListPoints.size() > 0) {
            m_trackList = new ArrayList<Coordinate>();
            if (m_trackList != null && m_trackList.size() > 0) {
                m_trackList.clear();
            }
            for (int i = 0; i < mListPoints.size(); i++) {
                Coordinate coord = new Coordinate(Double.parseDouble(mListPoints.get(i).getX()), Double.parseDouble(mListPoints.get(i).getY()));
                m_trackList.add(coord);
            }
            PubVar.mBaseLine.UpdateData(m_trackList);
            PubVar.mBaseLine.startCanvas();
        }


    }


    public MainMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMapFragment newInstance(String param1, String param2) {
        MainMapFragment fragment = new MainMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    private static String GetDDMMSS(double DDD) {
//        //DD°MM'SS.SSSS″
//        int dd = (int) Math.floor(DDD);
//        double MM = (DDD - dd) * 60;
//        int mm = (int) Math.floor(MM);
//
//        double SS = (MM - mm) * 60;
//        String ss = Tools.ConvertToDigi(SS + "", 4);
//        return dd + "°" + mm + "'" + ss + "″";
//
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this, myView);
        addMapViewOnUI();
        initPubVar();
        initWorkDirectionary();
        startGPS();
        initBusiness();
//        if (mParam1 != null) {
//            mListPoints = new ArrayList<PointBean>();
//            if (Utils.isNetworkAvailable(getActivity())) {
//                getDataPointDraw(mParam1);
//            } else {
//                List<TraceEntity> mEntity = TraceManager.getInstance().getUnloadTracesByPatrolId(mParam1);
//                if (mEntity != null && mEntity.size() > 0) {
//                    for (int i = 0; i < mEntity.size(); i++) {
//                        PointBean bean = new PointBean();
//                        bean.setX(String.valueOf(mEntity.get(i).getX()));
//                        bean.setY(String.valueOf(mEntity.get(i).getY()));
//                        bean.setLatitude(String.valueOf(mEntity.get(i).getLatitude()));
//                        bean.setLongitude(String.valueOf(mEntity.get(i).getLongitude()));
//                        bean.setCreatetime(String.valueOf(mEntity.get(i).getGpsTime()));
//                        bean.setUserid(String.valueOf(mEntity.get(i).getUserID()));
//                        bean.setId(String.valueOf(mEntity.get(i).getId()));
//                        bean.setRoundId(String.valueOf(mEntity.get(i).getRoundID()));
//                        bean.setHigh(String.valueOf(mEntity.get(i).getHeight()));
//                        mListPoints.add(bean);
//                    }
//                }
//                startDraw();
//        }

//    }

        return myView;
    }

    private void getDataPointDraw(String mParam1) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        RequestId requestId = new RequestId();
        requestId.setRequestId(mParam1);
        Call<ResponseBody> newPointCall = RetrofitHttp.getRetrofit(builder.build()).GetPointLine("GetTracksFromPatrolId", requestId);
        newPointCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() == null) {
                        return;
                    }
                    JSONObject result = new JSONObject(response.body().string());
                    String data = result.getString("data");
                    JSONArray jsonarray = new JSONArray(data);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject object = (JSONObject) new JSONArray(data).get(i);
                        PointBean bean = new PointBean();
                        bean.setX(object.getString("x"));
                        bean.setY(object.getString("y"));
                        bean.setLatitude(object.getString("Latitude"));
                        bean.setLongitude(object.getString("Longitude"));
                        bean.setCreatetime(object.getString("Createtime"));
                        bean.setUserid(object.getString("Userid"));
                        bean.setId(object.getString("Id"));
                        bean.setRoundId(object.getString("roundId"));
                        bean.setHigh(object.getString("high"));
                        mListPoints.add(bean);
                    }

                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = MSG_SUCCESS_FLAG;
                    mHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Message msg = mHandler.obtainMessage();
                msg.arg1 = MSG_FAIL_FLAG;
                mHandler.sendMessage(msg);
            }
        });

    }

    private void startGPS() {
        PubVar.m_GPSLocate.OpenGPS();
        PubVar.m_GPSLocate.SetGpsSetCallback(new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {

                if (ExtraStr == null || ((LocationEx) ExtraStr).GetGpsFixMode() != lkGpsFixMode.en3DFix) {
                    ((TextView) myView.findViewById(R.id.status_location)).setText("GPS信号弱");
                } else {
                    if (((LocationEx) ExtraStr).GetGpsLongitude() > 0 && (((LocationEx) ExtraStr).GetGpsLatitude()) > 0) {
                        String coor = GetDDMMSS(((LocationEx) ExtraStr).GetGpsLongitude()) + ", " + GetDDMMSS((((LocationEx) ExtraStr).GetGpsLatitude())) + ", " + Tools.ConvertToDigi(((LocationEx) ExtraStr).GetGpsAltitude() + "", 1);
                        ((TextView) myView.findViewById(R.id.status_location)).setText(coor);
                    } else {
                        ((TextView) myView.findViewById(R.id.status_location)).setText("GPS信号弱");
                    }

                }
            }
        });
    }

    private void initPubVar() {
        PubVar.m_GPSMap = new GPSMap(PubVar.m_MapControl);
        PubVar.m_DoEvent = new DoEvent(this.getActivity());
        PubVar.m_DoEvent.mGPSSatus.SetStatusView((ImageView) myView.findViewById(R.id.iv_status));

        PubVar.m_GPSLocate = new GPSLocate(PubVar.m_MapControl);
        PubVar.m_DoEvent.mRoundLinePresenter = new RoundGPSLine();
        PubVar.m_GPSMap.SetRoundGpsLine(PubVar.m_DoEvent.mRoundLinePresenter);
        PubVar.m_SysDictionaryName = this.getResources().getString(R.string.app_name);
    }

    private void addMapViewOnUI() {

        mp = new MapControl(this.getActivity());
        RelativeLayout linearLayout = (RelativeLayout) myView.findViewById(R.id.mMainContainer);
        linearLayout.addView(mp,0);

        RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(0, 0);
        para.height = RelativeLayout.LayoutParams.FILL_PARENT;
        para.width = RelativeLayout.LayoutParams.FILL_PARENT;
        para.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        para.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        para.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mp.setLayoutParams(para);
        PubVar.m_MapControl = mp;
        mp.invalidate();
    }

    private void initWorkDirectionary() {
        HashMap<String, String> resultHM = SystemSetup.CheckSystemFile(this.getActivity());
        if (resultHM.get("Result").equals("系统主目录缺失")) {

            SelectSystemPath sst = new SelectSystemPath(this.getActivity());
            sst.SetCallback(new ICallback() {
                @Override
                public void OnClick(String Str, Object ExtraStr) {
                    if (Str.equals("退出")) {
                        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        String PackName = getActivity().getPackageName();
                        am.killBackgroundProcesses(PackName);
                        System.exit(0);
                    }
                    if (Str.equals("工作目录")) {
                        SystemSetup.CheckSystemFile(PubVar.m_DoEvent.m_Context);
                        HashMap<String, String> resultHM = SystemSetup.CheckSystemFile(getActivity());
                        if ((resultHM.get("Result").equals("OK"))) {
                            PubVar.m_SysAbsolutePath = resultHM.get("Path");
                            AppSetting.photoPath = PubVar.m_SysAbsolutePath + "/Photo";
                            AppSetting.smallPhotoPath = AppSetting.photoPath + "/samllPhoto";
                            openProject();
                        }
                    }
                }
            });
            sst.ShowDialog();
            return;
        } else {
            if ((resultHM.get("Result").equals("OK"))) {
                PubVar.m_SysAbsolutePath = resultHM.get("Path");
                AppSetting.photoPath = PubVar.m_SysAbsolutePath + "/Photo";
                AppSetting.smallPhotoPath = AppSetting.photoPath + "/samllPhoto";
                openProject();

            } else {

                Tools.ShowMessageBox(this.getActivity(), resultHM.get("Result"), new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        String PackName = getActivity().getPackageName();
                        am.killBackgroundProcesses(PackName);   // API Level至少为8才能使用
                        System.exit(0);
                    }
                });
            }
        }
    }

    private void initBusiness() {

        if (AppSetting.curUser == null) {
            AppSetting.curUser = UserManager.getInstance().getLoginUser(AppSetting.curUserKey);
        }

        if (AppSetting.curUser == null) {
            return;
        }

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                settingOnGoingPatrol();
                //数据补传
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(getActivity(), "开始补传数据", Toast.LENGTH_LONG).show();
                        new ReuploadManager().reloadAll();
                    }
                }).start();
            }
        });

    }

//    private void getUserDutyArea(String departmentId) {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        RequestId requestId = new RequestId();
//        requestId.setRequestId(departmentId);
//        Call<ResponseBody> getDutyAreaCall = RetrofitHttp.getRetrofit(builder.build()).GetDutyArea("GetDutyArea", requestId);
//        getDutyAreaCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//
//                    if (response.body() == null) {
//                        Log.e("getDutyAreaCall", "response.body() is null ");
//                        return;
//                    }
//
//                    JSONObject result = new JSONObject(response.body().string());
//                    Log.d("DutyAreaString", response.body().string());
//                    if (result.get("success").equals(Boolean.TRUE)) {
//                        JSONArray dataArray = new JSONArray(result.get("data").toString());
//                        if (AppSetting.myDutyArea == null) {
//                            AppSetting.myDutyArea = new HashMap<String, String>();
//                        } else {
//                            AppSetting.myDutyArea.clear();
//                        }
//                        for (int i = 0; i < dataArray.length(); i++) {
//                            try {
//                                JSONObject jsonObject = dataArray.getJSONObject(i);
//                                DutyAreaEntity daEntity = new DutyAreaEntity();
//                                daEntity.setDepId(jsonObject.getString("DepId"));
//                                daEntity.setId(jsonObject.getString("Id"));
//                                daEntity.setName(jsonObject.getString("Name"));
//                                daEntity.setDutyUserNames(jsonObject.getString("DutyUserNames"));
//                                daEntity.setDutymanager(jsonObject.getString("Dutymanager"));
//                                daEntity.setDescription(jsonObject.getString("Description"));
//                                daEntity.setUpdateTime(new Date());
//                                AppSetting.myDutyArea.put(daEntity.getId(), daEntity.getName());
//                                UserManager.getInstance().saveDutyArea(daEntity);
//                            } catch (Exception ex) {
//                                Log.e("ParseAreaDutyJson", ex.getMessage());
//                            }
//                        }
//                    }
//
//                } catch (IOException io) {
//                    Log.e("getDutyArea IO", io.getMessage());
//                } catch (Exception ex) {
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e("getUserDutyArea", t.getMessage());
//            }
//        });
//    }

    private void settingOnGoingPatrol() {

        AppSetting.curRound = PatrolManager.getInstance().getOnGoingPatrol(AppSetting.curUserKey);
        if (AppSetting.curRound == null) {
            return;
        }

        Tools.ShowYesOrOtherMessage(getActivity(), "是否继续您上次未完成的巡护？", new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                if (Str == "YES") {
                    Log.d("巡护", "继续上次未完成的巡护");
                    Log.i("curRound", "round id：" + AppSetting.curRound.getId());
                    Log.i("curRound", "round server id：" + AppSetting.curRound.getServerId());
//                    if (AppSetting.curRound.getServerId() == null || AppSetting.curRound.getServerId().isEmpty()) {
//                                UploadMananger.getInstance().uploadRound(AppSetting.curRound, new ICallback() {
//                                    @Override
//                                    public void OnClick(String Str, Object ExtraStr) {
//
//                                    }
//                        });
//                    }

                    PubVar.m_DoEvent.mRoundLinePresenter.Start(lkDataCollectType.enGps_T, AppSetting.curRound.getStartTime());

                    try {
                        List<TraceEntity> traces = TraceManager.getInstance().getTracesByPatrolId(AppSetting.curRound.getId());
                        Log.d("PatrolId", AppSetting.curRound.getId());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        if (traces != null) {
                            Log.d("TraceCount", traces.size() + "");
                            for (TraceEntity trace : traces) {
                                if (trace.getLatitude() != 0 && trace.getLongitude() != 0) {
                                    LocationEx location = new LocationEx();
                                    Log.d("trace", "lon:" + trace.getLongitude() + " lat:" + trace.getLatitude());
                                    location.SetGpsLongitude(trace.getLongitude());
                                    location.SetGpsLatitude(trace.getLatitude());
                                    location.SetGpsAltitude(trace.getHeight());
                                    location.SetGpsDate(dateFormat.format(trace.getGpsTime()));
                                    location.SetGpsTime(timeFormat.format(trace.getGpsTime()));
                                    PubVar.m_DoEvent.mRoundLinePresenter.UpdateGpsPosition(location, false);
                                }


                            }
                        } else {
                            Log.d("NO Trace", "NO Trace");
                        }

                    } catch (Exception ex) {
                        Log.d("GetTrace", ex.getMessage());
                        Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferencesUtils.putBoolean(getActivity(), "mIsRounding", true);
                    initRounding();
                } else {
//                    mIsRounding = false;
                    stopRound();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(getActivity(), "开始补传数据", Toast.LENGTH_LONG).show();
                        new ReuploadManager().reloadAll();
                    }
                }).start();

            }
        });
    }

    private void openProject() {
        HashValueObject hvo = new HashValueObject();
        hvo.Value = "优良树种调查";
        PubVar.m_HashMap.Add("Project", hvo);


        PubVar.m_DoEvent.m_ProjectDB = new ProjectDB();

        PubVar.m_DoEvent.m_ProjectDB.CreateProject(hvo.Value);

        PubVar.m_DoEvent.m_ProjectDB.OpenProject(hvo.Value, false);
        StaticObject.soProjectSystem.SetCoorSystem(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem());
        //在PorjectDB.v1_BKLayerExplorer,底图数据图层
        BKLayerExplorer _BKLayerExplorer = PubVar.m_DoEvent.m_ProjectDB.GetBKLayerExplorer();

        //在PorjectDB.PorjectDB.v1_LayerExplorer,采集数据图层
        LayerExplorer _LayerExplorer = PubVar.m_DoEvent.m_ProjectDB.GetLayerExplorer();

//			//图层渲染器
//			v1_LayerRenderExplorer _LayerRenderExplorer = PubVar.m_DoEvent.m_ProjectDB.GetLayerRenderExplorer();

        //工程浏览器
        ProjectExplorer _ProjectExplorer = PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer();

        //创建工程空间实例
        if (PubVar.m_Workspace != null) {
            PubVar.m_Workspace.FreeWorkSpace();
        }

        Workspace pWorkspace = new Workspace();
        PubVar.m_Workspace = pWorkspace;

        //设置工程空间坐标系统，工程坐标系统存储于StaticObject.soProjectSystem.
        pWorkspace.SetCoorSystemInfo(StaticObject.soProjectSystem.GetCoorSystem());

        //创建MAP对象，在此处赋最大范围this.setFullExtend(StaticObject.soMapCellIndex.GetCellExtend());
        Map map = new Map(PubVar.m_MapControl);
        map.SetScaleBar(PubVar.m_DoEvent.mScaleBar);
        map.setEmpty();
        PubVar.m_Map = map;
        ImageView iv = (ImageView) myView.findViewById(R.id.iv_scalebar);
        PubVar.m_DoEvent.mScaleBar.SetImageView(iv);
//        PubVar.m_Map.getOverMapLayer().SetOverMapType(lkOverMapType.enGoogle_Satellite);

        //不需要比例尺
        //map.SetScaleBar(PubVar.m_DoEvent.m_ScaleBar);

        //打开采集数据源，其中有渲染底图图层 ，也就是创建GeoLayer
        _LayerExplorer.OpenDataSource(_ProjectExplorer.GetProjectDataFileName());

        //打开底图数据源，其中有渲染底图图层 ，也就是创建GeoLayer
        _BKLayerExplorer.OpenBKDataSource();

//        //读取工程的上次视图范围，如果没有则全图显示
//        Envelope pEnv = _ProjectExplorer.ReadShowExtend();
//        if (pEnv != null) {
//            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreenSize);
//            PubVar.m_Map.setExtend(pEnv);
////            PubVar.m_Map.Refresh();
//        } else {
//            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreen);
//        }
        PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreen);
        PubVar.m_Map.Refresh();
        PubVar.m_MapControl.setActiveTool(ZoomInOutPan);

        Dataset pDataset = PubVar.m_Workspace.GetDatasetById("T5A37395CA75F49F2B0A017DEE983D4EF");
        PubVar.m_DoEvent.mRoundLinePresenter.SetDataset(pDataset);
        try {
            Coordinate coordinate = PubVar.m_GPSLocate.getGPSCoordinate();
            if (coordinate != null) {
                PubVar.m_MapControl._Pan.SetNewCenter(PubVar.m_GPSLocate.getGPSCoordinate());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btnFullScreen, R.id.btnStartRound, R.id.btnSelectDraw, R.id.btnReportAlarm, R.id.btnReportEvent, R.id.btnDraw, R.id.btnFinishRound, R.id.btn_history,R.id.bt_gpslocate})
    public void clickBtn(View view) {

        switch (view.getId()) {
            case R.id.btnFullScreen:
                PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.ZoomInOutPan);
                break;
            case R.id.btnStartRound:
                startNormalPatrol();
                break;
            case R.id.btnReportAlarm:
                break;
            case R.id.btnReportEvent:
                reportEvent();
                break;
            case R.id.btnDraw:
//                isDrawing = !isDrawing;
//                if(isDrawing)
//                {
//                    selectDrawType();
//                }
//                else
//                {
//                    exitDraw();
//                }
                if (AppSetting.curRound != null) {
                    addPatrolPoint();
                } else {
                    Toast.makeText(getActivity(), "请先开始巡护！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnFinishRound:
                stopRound();
                break;
            case R.id.btnSelectDraw:
//                PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.Select);
                if (PubVar.m_DoEvent.m_Agent_Measure == null) {
                    PubVar.m_DoEvent.m_Agent_Measure = new Agent_Measure();
                    PubVar.m_DoEvent.m_Agent_Measure.OnPrepare();
                }
                PubVar.m_DoEvent.m_Agent_Measure.SetZHMode();

                break;
            case R.id.btn_history:
                startActivity(new Intent(getActivity(), ListExamineActivity.class));
                break;

            case R.id.bt_gpslocate:
                try {
                    Coordinate coordinate = PubVar.m_GPSLocate.getGPSCoordinate();
                    if (coordinate != null) {
                        PubVar.m_MapControl._Pan.SetNewCenter(PubVar.m_GPSLocate.getGPSCoordinate());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

//            case R.id.btn_export:
//                Export();
//                break;

//            case R.id.btn_navigate:
//                if (isAvilible(getActivity(), "com.baidu.BaiduMap")) {//传入指定应用包名
//                    Intent i1 = new Intent();
//                    i1.setData(Uri.parse("baidumap://map?src=andr.baidu.openAPIdemo"));
//                    startActivity(i1);
//                }else{
//                    Toast.makeText(getActivity(),getString(R.string.install_baiumap_apk),Toast.LENGTH_SHORT).show();
//                }
//                break;

        }
        setSelectStatus(view);
    }

    private void Export() {
        try {
            if (!Tools.ExistFile(PubVar.m_SysAbsolutePath + "/Data/调查系统/")) {
                (new File(PubVar.m_SysAbsolutePath + "/Data/调查系统/")).mkdirs();
            }

//            InputStream inputStream = myView.getResources().openRawResource(R.raw.tudiqq);// 将raw中的test.db放入输入流中
//            FileOutputStream fileOutputStream = new FileOutputStream(
//                    PubVar.m_SysAbsolutePath + "/Data/调查系统/调查表.xls");// 将新的文件放入输出流中
//            byte[] buff = new byte[8192];
//            int len = 0;
//            while ((len = inputStream.read(buff)) > 0) {
//                fileOutputStream.write(buff, 0, len);
//            }
//            fileOutputStream.close();
//            inputStream.close();
//
//            Workbook wb = Workbook.getWorkbook(new File(PubVar.m_SysAbsolutePath + "/Data/调查系统/调查表.xls"));
//            WritableWorkbook book = Workbook.createWorkbook(new File(PubVar.m_SysAbsolutePath + "/Data/调查系统/调查表.xls"), wb);
            WritableWorkbook book = Workbook.createWorkbook(new File(PubVar.m_SysAbsolutePath + "/Data/调查系统/调查表.xls"));
            List<RoundExamineEntity> list = PatrolManager.getInstance().getExam();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    WritableSheet sheet = book.createSheet("sheet_" + i, i);
                    createTable(sheet, list.get(i));
                }

            }
        } catch (Exception ex) {
            Tools.ShowMessageBox(ex.getMessage());
        }


    }

    private void createTable(WritableSheet sheet, RoundExamineEntity roundExamineEntity) {

//        try {
//            Label label1 = new Label(0, 0, "优良单株调查表", getHeaderCellStyle());
//            sheet.addCell(label1);
//            sheet.mergeCells(0, 0, 11, 3);
//
//            Label label2 = new Label(10, 4, "编号",getHeaderCellStyle());
//            sheet.addCell(label2);
//            Label label3 = new Label(11, 4, roundExamineEntity.getOrderNumber(),getBodyCellStyle());
//            sheet.addCell(label3);
//
//            Label label4= new Label(0, 0, "省份",getHeaderCellStyle());
//            sheet.addCell(label4);
//            sheet.mergeCells(0, 5, 0, 6);
//
//            Label label5= new Label(1, 5, roundExamineEntity.getSheng(),getBodyCellStyle());
//            sheet.addCell(label5);
//            sheet.mergeCells(1, 5, 3, 5);
//            sheet.mergeCells(1, 5, 1, 6);
//
//            Label label6= new Label(4, 5, "县(市）",getHeaderCellStyle());
//            sheet.addCell(label6);
//            sheet.mergeCells(1, 5, 1, 6);
//
//            Label label7= new Label(5, 5, roundExamineEntity.getXian(),getBodyCellStyle());
//            sheet.addCell(label7);
//            sheet.mergeCells(5, 5,7,5);
//            sheet.mergeCells(5, 5,5,6);
//
//            Label label8= new Label(5, 8, "调查地点",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(8, 5,8,6);
//
//            Label label9= new Label(9, 5, roundExamineEntity.getAddress(),getHeaderCellStyle());
//            sheet.addCell(label9);
//            sheet.mergeCells(9, 5, 9, 6);
//            sheet.mergeCells(11, 5, 12, 8);
//
//            Label label10= new Label(0, 8, "调查者",getHeaderCellStyle());
//            sheet.addCell(label10);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "填表人",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "调查日期",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "种(变种)中文名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "属中文名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "科中文名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "种(变种)拉丁名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "属拉丁名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "科拉丁名",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "经度",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "纬度",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "海拔",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "坡向",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "坡位",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "坡度",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "树高(m)",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "胸径(cm)",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "冠幅(m)",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "枝下高(m)",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "单株立木蓄积(m³)",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "土壤类型",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "优良单株重要特征描述",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "照片编号",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "拍摄者",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "拍摄日期",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);
//
//            Label label8= new Label(5, 8, "备注",getHeaderCellStyle());
//            sheet.addCell(label8);
//            sheet.mergeCells(5, 8, 6, 8);

//        } catch (WriteException e) {
//            e.printStackTrace();
//        }

    }


    public WritableCellFormat getHeaderCellStyle() {

        /*
         * WritableFont.createFont("宋体")：设置字体为宋体 10：设置字体大小
         * WritableFont.BOLD:设置字体加粗（BOLD：加粗 NO_BOLD：不加粗） false：设置非斜体
         * UnderlineStyle.NO_UNDERLINE：没有下划线
         */
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 18, WritableFont.BOLD, false,
                UnderlineStyle.NO_UNDERLINE);

        WritableCellFormat headerFormat = new WritableCellFormat(NumberFormats.TEXT);
        try {
            // 添加字体设置
            headerFormat.setFont(font);
            // 设置表头表格边框样式
            // 整个表格线为粗线、黑色
            headerFormat.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.BLACK);
            // 表头内容水平居中显示
            headerFormat.setAlignment(Alignment.CENTRE);
        } catch (WriteException e) {
            System.out.println("表头单元格样式设置失败！");
        }
        return headerFormat;
    }


    public WritableCellFormat getBodyCellStyle() {

        /*
         * WritableFont.createFont("宋体")：设置字体为宋体 10：设置字体大小
         * WritableFont.BOLD:设置字体加粗（BOLD：加粗 NO_BOLD：不加粗） false：设置非斜体
         * UnderlineStyle.NO_UNDERLINE：没有下划线
         */
        WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9, WritableFont.NO_BOLD, false,
                UnderlineStyle.NO_UNDERLINE);

        WritableCellFormat headerFormat = new WritableCellFormat(NumberFormats.TEXT);
        try {
            // 添加字体设置
            headerFormat.setFont(font);
            // 设置表头表格边框样式
            // 整个表格线为粗线、黑色
            headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
            // 表头内容水平居中显示
            headerFormat.setAlignment(Alignment.CENTRE);
        } catch (WriteException e) {
            System.out.println("内容单元格样式设置失败！");
        }
        return headerFormat;
    }


//    /**
//     * 检查手机上是否安装了指定的软件
//     * @return
//     */
//    private boolean isAvilible(Context context, String packageName) {
//        //获取packagemanager
//        final PackageManager packageManager = context.getPackageManager();
//        //获取所有已安装程序的包信息
//        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
//        //用于存储所有已安装程序的包名
//        List<String> packageNames = new ArrayList<String>();
//        //从pinfo中将包名字逐一取出，压入pName list中
//        if (packageInfos != null) {
//            for (int i = 0; i < packageInfos.size(); i++) {
//                String packName = packageInfos.get(i).packageName;
//                packageNames.add(packName);
//            }
//        }
//        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
//        return packageNames.contains(packageName);
//    }
//     * @param context
//     * @param packageName：应用包名
//     *


    private void setSelectStatus(View view) {
        boolean isNone = false;
        for (int id : m_SelectToolsBarItemIdList) {
            if (view.getId() == id) {
                view.setSelected(true);
                isNone = true;
            } else {
                View buttonView = myView.findViewById(id);
                buttonView.setSelected(false);
            }
        }


        if (!isNone) {
            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.ZoomInOutPan);
        }
    }

    private void reportEvent() {
//        if (AppSetting.curRound != null) {
        startActivity(new Intent(getActivity(), EventActivity.class));
//        } else {
//            Toast.makeText(getActivity(), "请先开始巡护！", Toast.LENGTH_LONG).show();
//        }

    }

//    private void takePhoto() {
//        if (AppSetting.curRound == null) {
//            Toast.makeText(getActivity(), "没有正在巡护的路线", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        startActivity(new Intent(getActivity(), RoundActivity.class));
//    }

    private void stopRound() {
        if (AppSetting.curRound == null) {
            Toast.makeText(getActivity(), "没有正在巡护的路线", Toast.LENGTH_SHORT).show();
            return;
        }

        RoundActivity.mFinishCallbak = new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                if (Str.equals("Finish")) {
                    try {
                        int geometryID = PubVar.m_DoEvent.mRoundLinePresenter.Stop();
                        AppSetting.curRound.setGeometryId(geometryID);
                        try {
                            PatrolManager.getInstance().savePatrol(AppSetting.curRound);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        PubVar.m_DoEvent.mRoundLinePresenter.setGPSTrackList(new ArrayList<Coordinate>());
                        AppSetting.curRound = null;
                        SharedPreferencesUtils.putBoolean(getActivity(), "mIsRounding", false);
                        unloadTraces = true;

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

//                    PubVar.m_DoEvent.mRoundLinePresenter = new RoundGPSLine();

//                    initRounding();
                }
            }
        };
        startActivity(new Intent(getActivity(), RoundActivity.class));
    }


    private void startNormalPatrol() {
//        if (SharedPreferencesUtils.getBoolean(getActivity(), "mIsRounding")) {
//            Tools.ShowMessageBox("正在进行调查，请完成正在进行的调查后再开始新的巡护！");
//            return;
//        }

        AlertDialog.Builder startRoundDialog = new AlertDialog.Builder(getActivity());
        startRoundDialog.setCancelable(false);
        startRoundDialog.setTitle("调查设置");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_startround, null);
        startRoundDialog.setView(layout);
        mSheng = (Spinner) layout.findViewById(R.id.sp_sheng);
        String[] shengType = "北京市,天津市,上海市,重庆市,广东省,陕西省,河北省,山西省,黑龙江省,吉林省,辽宁省,甘肃省,青海省,山东省,河南省,江苏省,浙江省,安徽省,江西省,福建省,台湾省,湖北省,湖南省,海南省,四川省,云南省,贵州省,广西壮族自治区,内蒙古自治区,新疆维吾尔自治区,宁夏回族自治区,西藏自治区,香港特别行政区,澳门特别行政区".split(",");
        ArrayAdapter<String> shengTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                shengType);
        shengTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSheng.setAdapter(shengTypeAdapter);

        mXian = (EditText) layout.findViewById(R.id.et_xian);

        mPerson = (EditText) layout.findViewById(R.id.et_person);
        mPersonTable = (EditText) layout.findViewById(R.id.et_fill_table);

        SharedPreferences preferences = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        String sheng=preferences.getString(ShengDB,"");
        for (int i = 0; i < shengType.length; i++) {
            if (sheng.equals(shengType[i])) {
                mSheng.setSelection(i, true);
            }
        }
        mXian.setText(preferences.getString(XianDB, ""));
        mPerson.setText(preferences.getString(DiaoChaZheDB, ""));
        mPersonTable.setText(preferences.getString(FillTableDB, ""));

//        String[] arrRoundType = "常规巡护、稽查巡护、责任区巡护".split("、");
//        ArrayAdapter<String> roundTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
//                android.R.layout.simple_spinner_item,
//                arrRoundType);
//        roundTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spRoundType.setAdapter(roundTypeAdapter);
//        spRoundType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position > 0) {
//                    layout.findViewById(R.id.layoutZeRenqu).setVisibility(View.VISIBLE);
//                } else {
//                    layout.findViewById(R.id.layoutZeRenqu).setVisibility(View.GONE);
//                }
//                if (position == 2) {
//                    ((TextView) layout.findViewById(R.id.tvXuHuyuan)).setText("责任人");
//                } else {
//                    ((TextView) layout.findViewById(R.id.tvXuHuyuan)).setText("巡护人");
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

//        Date now = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String currenetDate = format.format(now);
//        ((TextView) layout.findViewById(R.id.etRoundDate)).setText(currenetDate);

//        final Spinner spRoundWeather = (Spinner) layout.findViewById(R.id.spWeather);
//        String[] arrRoundWeather = "晴、多云、 阴、雾、阵雨、雷阵雨、小雨、中雨、大雨、雨夹雪、暴雨、阵雪、小雪、中雪、大雪、暴雪、冻雨、浮尘、霾".split("、");
//        ArrayAdapter<String> roundWeatherAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
//                android.R.layout.simple_spinner_item,
//                arrRoundWeather);
//        roundWeatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spRoundWeather.setAdapter(roundWeatherAdapter);
//

        try {
            if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {

                ((TextView) layout.findViewById(R.id.tvLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                ((TextView) layout.findViewById(R.id.tvLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                ((TextView) layout.findViewById(R.id.tvHigh)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");

            } else {
            }
        } catch (Exception ex) {
            //TODO:record and upload error log
        }
        startRoundDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sheng = mSheng.getSelectedItem().toString();
                String xian = mXian.getText().toString();
                String diaochaPerson = mPerson.getText().toString();
                String fillTablePerson = mPersonTable.getText().toString();
//                String diaochaDate = mStartTable.getText().toString();
                SharedPreferences preferences = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
                preferences.edit().putString(ShengDB, sheng).commit();
                preferences.edit().putString(XianDB, xian).commit();
                preferences.edit().putString(DiaoChaZheDB, diaochaPerson).commit();
                preferences.edit().putString(FillTableDB, fillTablePerson).commit();
                if (isStarting) {
                    return;
                } else {
                    isStarting = true;
                }
            }
        });

        startRoundDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        startRoundDialog.show();
    }

    private void initRounding() {
        if (SharedPreferencesUtils.getBoolean(getActivity(), "mIsRounding")) {
            PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(new ICallback() {
                @Override
                public void OnClick(String Str, final Object ExtraStr) {

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            LocationEx location = (LocationEx) ExtraStr;
                            TraceEntity traceEntity = new TraceEntity();
                            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            if (location.GetGpsDate() == null || location.GetGpsDate().isEmpty() || location.GetGpsDate() == null || location.GetGpsTime().isEmpty()) {
                                try {
                                    traceEntity.setGpsTime(sd.parse(location.GetGpsDate() + " " + location.GetGpsTime()));
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                    Log.d("SaveTrace", "Time is Null");
                                    traceEntity.setGpsTime(new Date());
                                }
                            } else {
                                Log.d("SaveTrace", "Time Null");
                                traceEntity.setGpsTime(new Date());
                            }

                            if (AppSetting.curRound != null) {
                                traceEntity.setRoundID(AppSetting.curRound.getId());
                            } else {
                                Toast.makeText(getActivity(), "Current round is null", Toast.LENGTH_SHORT).show();
                            }

                            try {
                                final Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(location.GetGpsLongitude(), location.GetGpsLatitude(), location.GetGpsAltitude());

                                traceEntity.setUserID(AppSetting.curUserKey);
                                if (location.GetGpsLatitude() > 0 && location.GetGpsLongitude() > 0) {
                                    traceEntity.setHeight(location.GetGpsAltitude());
                                    traceEntity.setLatitude(location.GetGpsLatitude());
                                    traceEntity.setLongitude(location.GetGpsLongitude());
                                    NumberFormat nf = NumberFormat.getInstance();
                                    nf.setGroupingUsed(false);
                                    traceEntity.setX(nf.format(coord.getX()));
                                    traceEntity.setY(nf.format(coord.getY()));
                                }
                                String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
                                if (name.equals("西安80坐标")) {
                                    traceEntity.setSrid("2381");
                                } else if (name.equals("北京54坐标")) {
                                    traceEntity.setSrid("2433");
                                } else if (name.equals("2000国家大地坐标系")) {
                                    traceEntity.setSrid("4545");
                                } else if (name.equals("WGS-84坐标")) {
                                    traceEntity.setSrid("4326");
                                }
                                traceEntity.setUploadStatus(0);
                                traceEntity.setSaveTime(new Date());

                                TraceManager.getInstance().SaveTrace(traceEntity);

                                AppSetting.curRound.setLastTrace(traceEntity);

                                try {
                                    String curArea = findCurrentArea(coord);
                                    if (mapViewAreaView != null) {
                                        mapViewAreaView.setText(curArea);
                                    }

//                                    ((TextView) myView.findViewById(R.id.status_area)).setText(curArea);

                                } catch (Exception ex) {
                                    Log.e("curArea", ex.getMessage());
                                }


                                Log.d("轨迹存储", "traceEntity:" + traceEntity.getId() + " lat: " + traceEntity.getLatitude() + " lon:" + traceEntity.getLongitude() + " time:" + traceEntity.getGpsTime().toString());

                                if (AppSetting.curRound.getServerId() == null || AppSetting.curRound.getServerId().isEmpty()) {
                                    UploadMananger.getInstance().uploadRound(AppSetting.curRound, new ICallback() {
                                        @Override
                                        public void OnClick(String Str, Object ExtraStr) {
                                            if (ExtraStr != null) {
                                                AppSetting.curRound.setServerId(ExtraStr + "");
                                            }
                                        }
                                    });
                                } else {
                                    HttpTraceModel httpTraceModel = new HttpTraceModel();
                                    httpTraceModel.setUserId(traceEntity.getUserID());
                                    httpTraceModel.setRoundId(AppSetting.curRound.getServerId());
                                    httpTraceModel.setLatitude(traceEntity.getLatitude() + "");
                                    httpTraceModel.setLongitude(traceEntity.getLongitude() + "");
                                    httpTraceModel.setGpsTime(traceEntity.getGpsTime().getTime());
                                    httpTraceModel.setHeight(traceEntity.getHeight() + "");
                                    httpTraceModel.setX(traceEntity.getX());
                                    httpTraceModel.setY(traceEntity.getY());
                                    httpTraceModel.setSrid(traceEntity.getSrid());


                                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                                    Call<ResponseBody> newTraceCall = RetrofitHttp.getRetrofit(builder.build()).uploadTrace("InsertTrackData", httpTraceModel);
                                    final TraceEntity trace = traceEntity;
                                    newTraceCall.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.body() == null) {
                                                Log.e("upload trace", "response.body() is null");
                                                return;
                                            }

                                            try {
                                                if (response.body().string().contains("true")) {
                                                    Log.d("上传轨迹", trace.getGpsTime().toString());
                                                    trace.setUploadStatus(1);
//                                                    DatabaseCreator.getInstance(mContext).getDatabase().traceDao().insertTraces(trace);
                                                    TraceManager.getInstance().SaveTrace(trace);
                                                } else {
                                                    Log.e("上传轨迹", response.body().string());

                                                }
                                            } catch (IOException io) {
                                                Log.e("上传轨迹失败", io.getMessage());
                                                io.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Log.e("上传轨迹失败：", t.getMessage());
                                        }
                                    });
                                }

                            } catch (Exception ex) {

                                Log.e("存储迹失败：", ex.getMessage());

                            }
                        }
                    });
                }
            });


        } else {
            PubVar.m_DoEvent.mRoundLinePresenter.Stop();
            PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(null);
        }
    }

//    private void uploadStartPoint(final PatrolPointEntity patrolPointEntity) {
//        UploadMananger.getInstance().uploadPatrolPoint(patrolPointEntity, AppSetting.curRound.getServerId(), new ICallback() {
//            @Override
//            public void OnClick(String Str, Object ExtraStr) {
//                if (Str.equals("success")) {
//                    try {
//                        patrolPointEntity.setUploadStatus(1);
//                        PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
//                    } catch (Exception ex) {
//                        Toast.makeText(getActivity(), "上传起始点" + ex.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                    Toast.makeText(getActivity(), "起始点已上传", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    Toast.makeText(getActivity(), "上传起始点" + Str, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//    }

//    private void saveStartPoint(LocationEx locationEx, boolean patrolUploaded) {
//        if (locationEx == null || locationEx.GetGpsLatitude() < 0.000001 || locationEx.GetGpsLongitude() < 0.000001) {
//            Log.e("nostartpoint", "起始点数值为0");
//            return;
//        }
//
//        final PatrolPointEntity patrolPointEntity = new PatrolPointEntity();
//        patrolPointEntity.setUserID(AppSetting.curUser.getUserID());
//        patrolPointEntity.setRoundID(AppSetting.curRound.getId());
//        patrolPointEntity.setLatitude(locationEx.GetGpsLatitude());
//        patrolPointEntity.setLongitude(locationEx.GetGpsLongitude());
//        patrolPointEntity.setGpsTime(new Date());
//        patrolPointEntity.setHeight(locationEx.GetGpsAltitude());
//        Coordinate coordinate = StaticObject.soProjectSystem.WGS84ToXY(locationEx.GetGpsLongitude(),
//                locationEx.GetGpsLatitude(),
//                locationEx.GetGpsAltitude());
//        NumberFormat nf = NumberFormat.getInstance();
//        nf.setGroupingUsed(false);
//        patrolPointEntity.setX(nf.format(coordinate.getX()));
//        patrolPointEntity.setY(nf.format(coordinate.getY()));
////            TODO:auto get srid;
//        String name=StaticObject.soProjectSystem.GetCoorSystem().GetName();
//        if(name.equals("西安80坐标")){
//            patrolPointEntity.setSrid("2381");
//        }else if(name.equals("北京54坐标")){
//            patrolPointEntity.setSrid("2433");
//        }else if(name.equals("2000国家大地坐标系")){
//            patrolPointEntity.setSrid("4545");
//        }else if(name.equals("WGS-84坐标")){
//            patrolPointEntity.setSrid("4326");
//        }
//        patrolPointEntity.setPointType("0");
//        patrolPointEntity.setPointName("起始点");
//
//        try {
//            PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            Toast.makeText(getActivity(), "起始点" + ex.getMessage(), Toast.LENGTH_SHORT);
//            return;
//        }
//
//        if (patrolUploaded) {
////            if (AppSetting.curRound.getServerId() != null && !(AppSetting.curRound.getServerId().isEmpty())) {
//            UploadMananger.getInstance().uploadPatrolPoint(patrolPointEntity, AppSetting.curRound.getServerId(), new ICallback() {
//                @Override
//                public void OnClick(String Str, Object ExtraStr) {
//                    if (Str.equals("success")) {
//                        try {
//                            patrolPointEntity.setUploadStatus(1);
//                            PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
//                        } catch (Exception ex) {
//                            Toast.makeText(getActivity(), "上传起始点异常！", Toast.LENGTH_SHORT).show();
//                        }
//                        Toast.makeText(getActivity(), "起始点已上传", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        Toast.makeText(getActivity(), "上传起始点" + Str, Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            });
//        }
////        }
//
//    }

    private void addPatrolPoint() {
        AlertDialog.Builder addPointDialog = new AlertDialog.Builder(getActivity());
        addPointDialog.setCancelable(true);
        addPointDialog.setTitle("记录巡点");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_addpatrolpoint, null);
        addPointDialog.setView(layout);

        if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsFixMode() == lkGpsFixMode.en3DFix) {
            try {
                ((TextView) layout.findViewById(R.id.etPointLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                ((TextView) layout.findViewById(R.id.etPointLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                ((TextView) layout.findViewById(R.id.etPointAlt)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");
            } catch (Exception ex) {

            }
        }

        ((TextView) layout.findViewById(R.id.etPointLon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsFixMode() == lkGpsFixMode.en3DFix) {
                    try {
                        ((TextView) layout.findViewById(R.id.etPointLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                        ((TextView) layout.findViewById(R.id.etPointLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                        ((TextView) layout.findViewById(R.id.etPointAlt)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");
                    } catch (Exception ex) {

                    }
                } else {
                    Toast.makeText(v.getContext(), "请开启GPS并在开阔地带精确定位,然后刷新位置!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPointDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null
                            && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0
                            && PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0) {
                        PatrolPointEntity pointEntity = new PatrolPointEntity();
                        pointEntity.setHeight(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude());
                        pointEntity.setLatitude(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude());
                        pointEntity.setLongitude(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude());
//                       pointEntity.setWarningtime(PubVar.m_GPSLocate.m_LocationEx.GetGpsTime());
                        pointEntity.setGpsTime(new Date());
                        Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude(),
                                PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude(),
                                PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude());

                        pointEntity.setPointType("1");
                        String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
                        if (name.equals("西安80坐标")) {
                            pointEntity.setSrid("2381");
                        } else if (name.equals("北京54坐标")) {
                            pointEntity.setSrid("2433");
                        } else if (name.equals("2000国家大地坐标系")) {
                            pointEntity.setSrid("4545");
                        } else if (name.equals("WGS-84坐标")) {
                            pointEntity.setSrid("4326");
                        }
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(false);
                        pointEntity.setX(nf.format(coord.getX()));
                        pointEntity.setY(nf.format(coord.getY()));
                        pointEntity.setRoundID(AppSetting.curRound.getId());
                        //TODO:
//                       pointEntity.setUserID(AppSetting.curUser.getUserID());
                        pointEntity.setUserID(AppSetting.curUserKey);
                        try {
                            PatrolManager.getInstance().savePatrolPoint(pointEntity);
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT);
                            return;
                        }

                        if (AppSetting.curRound.getServerId() != null && !(AppSetting.curRound.getServerId().isEmpty())) {
                            UploadMananger.getInstance().uploadPatrolPoint(pointEntity, AppSetting.curRound.getServerId(), new ICallback() {
                                @Override
                                public void OnClick(String Str, Object ExtraStr) {
                                    if (Str.equals("success")) {
                                        Toast.makeText(getActivity(), "中间点已上传", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        dialog.dismiss();

                    } else {
                        Toast.makeText(getActivity(), "请开启GPS并在开阔地带精确定位!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        addPointDialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        addPointDialog.show();
    }

//    public void UpdateRoundStatus(double len, Date startTime, LocationEx location) {
//        String mStatusString;
//        String mLocationString;
//        String mAreaString;
//
//        if (startTime == null || len < 0.01) {
//            mStatusString = "未开始巡护";
//        } else {
//            mStatusString = "巡护中," + Tools.ReSetDistance(len, true);
//        }
//
//
//        if (location != null) {
//            mLocationString = Tools.ConvertToDigi(location.GetGpsLatitude(), 7) + "," + Tools.ConvertToDigi(location.GetGpsLongitude(), 7) + "," + Tools.ConvertToDigi(location.GetGpsAltitude(), 1);
//            mAreaString = "安家岐保护站(实验区)";
//        } else {
//            mLocationString = "未定位";
//            mAreaString = "未定位";
//        }
//    }

    private String findCurrentArea(Coordinate curPoint) {
        //选择容忍距离
        double Tolerance = PubVar.m_MapControl.getMap().ToMapDistance(Tools.DPToPix(10));

        String strBaoHuQu = "";
        String strGongnengqu = "";
        //1-在采集数据层内选择
        int LayerCount = PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enVectorBackground).size();
        Log.d("LayerCount", LayerCount + "");
        for (int i = LayerCount - 1; i >= 0; i--) {
            GeoLayer pGeoLayer = PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enVectorBackground).GetLayerByIndex(i);

            for (Geometry StGeometry : pGeoLayer.getDataset().GetGeometryList()) {
                try {
                    if (pGeoLayer.getDataset().getType() == lkGeoLayerType.enPolygon &&
                            pGeoLayer.GetAliasName().contains("保护站")) {
                        Polygon StPolygon = (Polygon) StGeometry;
                        Log.d("PolygonID", StPolygon.getSysId() + "");
                        if (StPolygon.HitTest(curPoint, Tolerance)) {
                            Log.d("SelectID", StPolygon.getSysId() + "");
                            String SQL = "select F3 from " + pGeoLayer.getDataset().getDataTableName() + " where SYS_ID = " + StPolygon.getSysId();
                            SQLiteDataReader DR = pGeoLayer.getDataset().getDataSource().Query(SQL);
                            if (DR != null) {
                                if (DR.Read()) {
                                    strBaoHuQu = DR.GetString("F3");

                                }

                            }
                            DR.Close();
                            break;
                        }
                    }

                } catch (Exception ex) {

                }

                try {
                    if (pGeoLayer.getDataset().getType() == lkGeoLayerType.enPolygon &&
                            pGeoLayer.GetAliasName().contains("功能区")) {
                        Polygon StPolygon = (Polygon) StGeometry;
                        if (StPolygon.HitTest(curPoint, Tolerance)) {
                            String SQL = "select F2 from " + pGeoLayer.getDataset().getDataTableName() + " where SYS_ID = " + StPolygon.getSysId();
                            SQLiteDataReader DR = pGeoLayer.getDataset().getDataSource().Query(SQL);
                            if (DR != null) {
                                if (DR.Read()) {
                                    strGongnengqu = DR.GetString("F2");
                                    Log.d("SelectID", StPolygon.getSysId() + "");
                                }

                            }
                            DR.Close();
                            break;
                        }
                    }
                } catch (Exception ex) {

                }

            }

        }
        if (strGongnengqu == null || strGongnengqu.isEmpty()) {
            return strBaoHuQu;
        } else {
            return strBaoHuQu + "(" + strBaoHuQu + ")";
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

}
