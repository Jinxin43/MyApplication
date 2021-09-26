package com.example.event.http;

import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.http.Httpmodel.HttpEventModel;
import com.example.event.http.Httpmodel.HttpModifyModel;
import com.example.event.http.Httpmodel.HttpPatrolPointModel;
import com.example.event.http.Httpmodel.HttpRegisterModel;
import com.example.event.http.Httpmodel.HttpRoundModel;
import com.example.event.http.Httpmodel.HttpTraceModel;
import com.example.event.http.Httpmodel.HttpUserModel;
import com.example.event.http.Httpmodel.RequestId;
import com.example.event.http.Httpmodel.RequestUserId;
import com.example.event.model.EditBean;
import com.example.event.model.GetRoutelineBean;
import com.example.event.model.UploadMessage;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

/**
 * Created by Dingtu2 on 2017/6/23.
 */

public interface HttpInterface {

    @POST("/api/DTUsers/{action}")
        //Call<ResponseBody> login(@Path("action") String action, @Query("mIdString")String Account,@Query("mPwdString")String Password,@Query("Device")String Device);
    Call<ResponseBody> login(@Path("action") String action, @Body HttpUserModel userModel);

    @POST("/api/DTUsers/{action}")
    Call<ResponseBody> register(@Path("action") String action, @Body HttpRegisterModel userModel);

    @POST("/api/DTUsers/{action}")
    Call<ResponseBody> modify(@Path("action")String action, @Body HttpModifyModel model);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> CreateRound(@Path("action") String action, @Body HttpRoundModel httpRoundModel);

    @Multipart
    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> uploadPicture(@Path("action") String action, @PartMap Map<String, RequestBody> params);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> uploadTrace(@Path("action") String action, @Body HttpTraceModel traceEntity);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> CreateEvent(@Path("action") String action, @Body HttpEventModel roundEventEntity);

    @Multipart
    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> uploadEventPicture(@Path("action") String action, @PartMap Map<String, RequestBody> params);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetSpecialPatrol(@Path("action") String action, @Body RequestUserId userId);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetSpecialPatrolById(@Path("action") String action, @Body RequestId requestId);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> AddPatrolPoint(@Path("action") String action, @Body HttpPatrolPointModel patrolPointModel);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetDutyArea(@Path("action") String action, @Body RequestId requestId);

    @Multipart
    @POST("/api/DTNotifications/{action}")
    Call<ResponseBody> UploadErrorLogFile(@Path("action") String action, @PartMap Map<String, RequestBody> params);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetRouteLine(@Path("action") String action, @Body GetRoutelineBean roundBean);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetRouteManagerLine(@Path("action") String action, @Body GetRoutelineBean roundBean);

    @POST("/api/DTTracks/{action}")
    Call<ResponseBody> GetPointLine(@Path("action") String action, @Body RequestId requestId);


    @POST("/api/DTForestZones/{action}")
    Call<ResponseBody> UploadMessage(@Path("action") String action, @Body UploadMessage message);

    @Multipart
    @POST("/api/DTForestZones/{action}")
    Call<ResponseBody> UploadPhoto(@Path("action") String action , @PartMap Map<String, RequestBody> params);

    @POST("/api/DTForestZones/{action}")
    Call<ResponseBody> EditMessage(@Path("action") String action, @Body EditBean bean);

    @Multipart
    @POST("/api/DTForestZones/{action}")
    Call<ResponseBody> UploadVideo(@Path("action") String action , @PartMap Map<String, RequestBody> params);



}
