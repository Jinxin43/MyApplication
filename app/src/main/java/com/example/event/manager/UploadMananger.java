package com.example.event.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.media.ExifInterface;
import android.util.Log;
import android.widget.Toast;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.example.event.AppSetting;
import com.example.event.db.xEntity.PatrolEntity;
import com.example.event.db.xEntity.PatrolPointEntity;
import com.example.event.db.xEntity.PhotoEntity;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.db.xEntity.TraceEntity;
import com.example.event.http.Httpmodel.HttpPatrolPointModel;
import com.example.event.http.Httpmodel.HttpRoundModel;
import com.example.event.http.Httpmodel.HttpTraceModel;
import com.example.event.http.RetrofitHttp;
import com.example.event.model.EditBean;
import com.example.event.model.UploadMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dingtu2 on 2018/4/19.
 */

public class UploadMananger {

    private static UploadMananger mInstantce;

    private UploadMananger() {
    }

    public static UploadMananger getInstance() {
        synchronized (UploadMananger.class) {
            if (mInstantce == null) {
                mInstantce = new UploadMananger();
            }
        }
        return mInstantce;
    }

    public void uploadRound(final PatrolEntity roundEntity, final ICallback myCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRoundModel httpRoundModel = new HttpRoundModel();
                httpRoundModel.setStartTime(roundEntity.getStartTime().getTime());
                httpRoundModel.setRoundName(roundEntity.getRoundName());
                httpRoundModel.setRoundType(roundEntity.getRoundType());
                httpRoundModel.setRoundStatus(roundEntity.getRoundStatus());
                httpRoundModel.setUserId(roundEntity.getUserID());
                httpRoundModel.setDescription(roundEntity.getSummary());
                httpRoundModel.setWeather(roundEntity.getWeather());
                httpRoundModel.setDutyId(roundEntity.getDutyId());
                httpRoundModel.setUserNames(roundEntity.getUserNames());
                httpRoundModel.setContent(roundEntity.getContent());
                httpRoundModel.setLineOrZone(roundEntity.getLineOrZone());
                try {
                    if (roundEntity.getRoundStatus() == 1) {
                        httpRoundModel.setEndTime(roundEntity.getEndTime().getTime());
                    }
                } catch (Exception ex) {
                    Toast.makeText(AppSetting.applicaton.getApplicationContext(), "结束时间：" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }


                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                promptRount(roundEntity, RetrofitHttp.getRetrofit(builder.build()).CreateRound("CreateNewRound", httpRoundModel), myCallback);
            }
        }).start();
    }

    public void uploadPhotoes(final RoundExamineEntity entity, final String photoName, final String data, final ICallback myCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, RequestBody> map = new HashMap<String, RequestBody>();
                RequestBody Id = RequestBody.create(MediaType.parse("text/plain"), data);
                map.put("TargetId", Id);
                RequestBody order = RequestBody.create(MediaType.parse("text/plain"), entity.getPhotoOrderNum());
                map.put("filelist", order);
                File photoFile = new File(PubVar.m_SysAbsolutePath + "/Photo/" + photoName);
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), photoFile);
                map.put("uploadedFiles\"; filename=\"" + photoFile.getName(), fileBody);
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                result(RetrofitHttp.getRetrofit(builder.build()).UploadPhoto("UploadTreePhoto", map), myCallback);
            }
        }).start();
    }

//    public void uploadVideo(final RoundExamineEntity entity, final String videoName, final ICallback myCallback) {
//        SharedPreferences preferences =AppSetting.applicaton.getApplicationContext().getSharedPreferences("userID", Context.MODE_PRIVATE);
//        final String userId=preferences.getString("UserId","");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Map<String, RequestBody> map = new HashMap<String, RequestBody>();
//                RequestBody Id = RequestBody.create(MediaType.parse("text/plain"),userId);
//                map.put("TargetId", Id);
//                RequestBody order = RequestBody.create(MediaType.parse("text/plain"),entity.getVideoList());
//                map.put("filelist",order);
//                File photoFile = new File(videoName);
//                RequestBody fileBody = RequestBody.create(MediaType.parse("video/mp4"), photoFile);
//                map.put("uploadedFiles\"; filename=\"" + photoFile.getName(), fileBody);
//                OkHttpClient.Builder builder = new OkHttpClient.Builder();
//                result(RetrofitHttp.getRetrofit(builder.build()).UploadVideo("UploadTreeVideo", map), myCallback);
//            }
//        }).start();
//
//    }


    private void result(Call<ResponseBody> newPhoto, final ICallback myCallback) {
        newPhoto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response.body() == null) {
                        myCallback.OnClick("failed", null);
                        Log.e("Tag", " response.body() is null ");
                        return;
                    }
                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {
                        Log.e("Tag", " result: " + result + "*****");
                        myCallback.OnClick("success", null);

                    } else {
                        myCallback.OnClick("failed", null);
                        Log.e("Tag", " result: " + result + "*****");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myCallback.OnClick("Tag", null);
                Log.e("Tag", t.getMessage());
            }
        });

    }

    public void EditEvent(final String id, final RoundExamineEntity exam, final ICallback myCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EditBean edit = new EditBean();
                edit.setTreeId(id);
                edit.setSheng(exam.getSheng());
                edit.setXian(exam.getXian());
                edit.setPlace(exam.getAddress());
                edit.setSurveyusers(exam.getExmainPerson());
                edit.setSurveytime(exam.getExamineDate());
                edit.setWriteusers(exam.getFillPerson());
                edit.setDescription(exam.getImportDescribe());
                edit.setRemark(exam.getStateDescribe());
                edit.setZhongcnname(exam.getZhongCName());
                edit.setShucnname(exam.getShuCName());
                edit.setKecnname(exam.getKeCName());
                edit.setZhonglaname(exam.getZhongLaName());
                edit.setShulaname(exam.getShuLaName());
                edit.setKelaname(exam.getKeLaName());
                edit.setAspect(exam.getPoXiang());
                edit.setSlopeposition(exam.getPoWei());
                edit.setSlope(exam.getPoDu());
                edit.setTreeheight(exam.getTreeHight() + "");
                edit.setXiongj(exam.getXiongJin() + "");
                edit.setGuanfu(exam.getGuanFu() + "");
                edit.setZhiheight(exam.getZhiHight() + "");
                edit.setXj(exam.getXuji() + "");
                edit.setType(exam.getTuType());
                edit.setPhotos(exam.getPhotoList());
                edit.setPhotouser(exam.getTakePerson());
                edit.setPhototime(exam.getTakeDate());
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                prompt(RetrofitHttp.getRetrofit(builder.build()).EditMessage("EditTreeSurvey", edit), myCallback);
            }
        }).start();
    }


    public void uploadEvent(final RoundExamineEntity roundEntity, final ICallback myCallback) {
        SharedPreferences preferences = AppSetting.applicaton.getApplicationContext().getSharedPreferences("userID", Context.MODE_PRIVATE);
        final String userId = preferences.getString("UserId", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadMessage message = new UploadMessage();
                message.setUserId(userId);
                message.setNumber(roundEntity.getOrderNumber());
                message.setSheng(roundEntity.getSheng());
                message.setXian(roundEntity.getXian());
                message.setPlace(roundEntity.getAddress());
                message.setSurveyusers(roundEntity.getExmainPerson());
                message.setSurveytime(roundEntity.getExamineDate());
                message.setWriteusers(roundEntity.getFillPerson());
                message.setLatitude(roundEntity.getLongtitude() + "");
                message.setLongitude(roundEntity.getLatitude() + "");
                message.setHeight(roundEntity.getHight() + "");
                message.setDescription(roundEntity.getImportDescribe());
                message.setRemark(roundEntity.getStateDescribe());
                message.setZhongcnname(roundEntity.getZhongCName());
                message.setShucnname(roundEntity.getShuCName());
                message.setKecnname(roundEntity.getKeCName());
                message.setZhonglaname(roundEntity.getZhongLaName());
                message.setShulaname(roundEntity.getShuLaName());
                message.setKelaname(roundEntity.getKeLaName());
                message.setAspect(roundEntity.getPoXiang());
                message.setSlopeposition(roundEntity.getPoWei());
                message.setSlope(roundEntity.getPoDu());
                message.setTreeheight(Integer.parseInt(new java.text.DecimalFormat("0").format(roundEntity.getTreeHight())) + "");
                message.setXiongj(roundEntity.getXiongJin() + "");
                message.setGuanfu(roundEntity.getGuanFu() + "");
                message.setZhiheight(roundEntity.getZhiHight() + "");
                message.setXj(roundEntity.getXuji() + "");
                message.setType(roundEntity.getTuType());
                message.setPhotos(roundEntity.getPhotoList());
                message.setPhotouser(roundEntity.getTakePerson());
                message.setPhototime(roundEntity.getTakeDate());
                message.setGpstime(roundEntity.getOrderNumber() + "");
                //wgs-84
                message.setSrid("4326");
                if(roundEntity.getZhongCName().contains("红豆杉")){
                    message.setTreetype("红豆杉调查");
                }else{
                    message.setTreetype("七叶树调查");
                }

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                prompt(RetrofitHttp.getRetrofit(builder.build()).UploadMessage("CreateTreeSurvey", message), myCallback);
            }
        }).start();
    }

    private void prompt(Call<ResponseBody> createTreeSurvey, final ICallback myCallback) {
        createTreeSurvey.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() == null) {
                        myCallback.OnClick("failed", null);
                        Log.e("Tag", " response.body() is null ");
                        return;
                    }
                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {
                        Log.e("Tag", " result: " + result);
                        myCallback.OnClick("success", result.get("data"));

                    } else {
                        myCallback.OnClick("failed", result.get("msg"));
                        Log.e("Tag", " result: " + result + "%%%%%%");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myCallback.OnClick("failed", null);
                Log.e("Tag", t.getMessage());
            }
        });

    }


    private void promptRount(final PatrolEntity roundEntity, Call<ResponseBody> newRound, final ICallback myCallback) {
        newRound.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> reg, Response<ResponseBody> response) {

                try {

                    if (response.body() == null) {
                        myCallback.OnClick("failed", AppSetting.curRound);
                        Log.e("Start Round upload", " response.body() is null ");
                        return;
                    }

                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {
                        Log.e("upload round", " result: " + result);

//                        roundEntity.setServerId(result.get("data").toString());
//                        roundEntity.setUploadStatus(1);
//                        PatrolManager.getInstance().savePatrol(roundEntity);
                        myCallback.OnClick("success", roundEntity.getServerId());

                    } else {
                        myCallback.OnClick("failed", roundEntity);
                        Log.e("upload round failed", " result: " + result);
                    }
                } catch (Exception io) {
                    Log.e("upload round fail", " exception: " + io.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> reg, Throwable t) {
                myCallback.OnClick("failed", AppSetting.curRound);
                Log.e("Round upload failed", " exception: " + t.getMessage());
            }
        });
    }

    public void uploadPatrolPoint(final PatrolPointEntity pointEntity, final String patrolServerId, final ICallback callback) {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                HttpPatrolPointModel httpPointModel = new HttpPatrolPointModel();
                httpPointModel.setPatrolId(patrolServerId);
                httpPointModel.setGpsTime(pointEntity.getGpsTime().getTime());
                if (pointEntity.getLatitude() > 0 && pointEntity.getLatitude() > 0) {
                    httpPointModel.setHeight(pointEntity.getHeight() + "");
                    httpPointModel.setLatitude(pointEntity.getLatitude() + "");
                    httpPointModel.setLongitude(pointEntity.getLongitude() + "");
                    httpPointModel.setX(pointEntity.getX());
                    httpPointModel.setY(pointEntity.getY());
                }
                httpPointModel.setSrid(pointEntity.getSrid());
                httpPointModel.setType(pointEntity.getPointType());

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                promptAddPoint(RetrofitHttp.getRetrofit(builder.build()).AddPatrolPoint("AddPatrolPoint", httpPointModel), pointEntity, callback);

            }
        });
    }

    private void promptAddPoint(Call<ResponseBody> newPoint, final PatrolPointEntity pointEntity, final ICallback myCallback) {
        newPoint.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {
                        Log.e("upload Point", " result: " + result);

//                        Toast.makeText(AppSetting.applicaton.getApplicationContext(),"巡护点上传完成",Toast.LENGTH_SHORT).show();
                        pointEntity.setUploadStatus(1);
                        PatrolManager.getInstance().savePatrolPoint(pointEntity);
                        myCallback.OnClick("success", null);
                    } else {
                        myCallback.OnClick("failed", null);
//                        Toast.makeText(AppSetting.applicaton.getApplicationContext(),"巡护点上传失败",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    myCallback.OnClick("failed", null);
                    Toast.makeText(AppSetting.applicaton.getApplicationContext(), "巡护点上传失败" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myCallback.OnClick("failed", null);
                Toast.makeText(AppSetting.applicaton.getApplicationContext(), "网络异常,网络恢复后补传", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadTraces(TraceEntity traceEntity, String serverPatrolId, final ICallback callback) {
        HttpTraceModel httpTraceModel = new HttpTraceModel();
        httpTraceModel.setUserId(traceEntity.getUserID());
        httpTraceModel.setRoundId(traceEntity.getServerRoundId());
        if (traceEntity.getLatitude() > 0 && traceEntity.getLongitude() > 0) {
            httpTraceModel.setLatitude(traceEntity.getLatitude() + "");
            httpTraceModel.setLongitude(traceEntity.getLongitude() + "");
        }
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
                    callback.OnClick("failed", callback);
                    return;
                }

                try {
                    if (response.body().string().contains("true")) {
                        Log.d("上传轨迹", trace.getGpsTime().toString());
                        trace.setUploadStatus(1);
                        TraceManager.getInstance().SaveTrace(trace);
                        callback.OnClick("success", callback);
                    } else {
                        callback.OnClick("failed", callback);
                        Log.e("上传轨迹", response.body().string());

                    }
                } catch (IOException io) {
                    callback.OnClick("failed", callback);
                    Log.e("上传轨迹失败", io.getMessage());
                    io.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.OnClick("failed", callback);
                Log.e("上传轨迹失败：", t.getMessage());
            }
        });
    }

    public void uploadPhoto(final String fileName) {
        File photo = new File(AppSetting.photoPath + "/" + fileName);
        RequestBody userUid = RequestBody.create(MediaType.parse("text/plain"), AppSetting.curRound.getServerId());
        Map<String, RequestBody> map = new HashMap<>();
        map.put("roundId", userUid);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), photo);
        map.put("uploadedFiles\"; filename=\"" + photo.getName(), fileBody);


        String imageInfo = "";
        try {

            ExifInterface exifInfo = new ExifInterface(AppSetting.photoPath + "/" + fileName);
            imageInfo = exifInfo.getAttribute(ExifInterface.TAG_USER_COMMENT);
            Log.d("exif read", imageInfo);
        } catch (Exception ex) {
        }
        if (imageInfo == null) {
            imageInfo = "";
        }
        RequestBody imageExif = RequestBody.create(MediaType.parse("text/plain"), imageInfo);
        map.put("imageInfo", imageExif);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Call<ResponseBody> newPhoto = RetrofitHttp.getRetrofit(builder.build()).uploadPicture("UploadRoundPhoto", map);
        newPhoto.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject result = new JSONObject(response.body().string());
                    if (result.get("success").equals(Boolean.TRUE)) {

                        try {
                            PhotoEntity photoEntity = PhotoManager.getInstance().getPhotoEntity(fileName);
                            photoEntity.setUploadStatus(1);
                            photoEntity.setUploadTime(new Date());
                            PhotoManager.getInstance().savePhoto(photoEntity);
                        } catch (Exception ex) {
                            Toast.makeText(AppSetting.applicaton.getApplicationContext(), "更新照片失败：" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Toast.makeText(AppSetting.applicaton.getApplicationContext(), response.body().toString(), Toast.LENGTH_LONG).show();
                        Log.e("上传照片", response.body().string());
                    }


                } catch (Exception ex) {
                    Toast.makeText(AppSetting.applicaton.getApplicationContext(), "上传照片：" + ex.getMessage(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AppSetting.applicaton.getApplicationContext(), "上传照片失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
