/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.event.Video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.DingTu.Base.PubVar;
import com.example.event.EventActivity;
import com.example.event.EventActivityFragment;
import com.example.event.EventEditActivity;
import com.example.event.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class Camera2VideoFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "qingyang";

    private static final int HANDLER_RECORD = 200;    //录制视频

    private static final int HANDLER_OPEN_PREVIEW = 207;    //打开预览

    private VideoRecordActivity mainActivity;

    //本次段落是否录制完成
    private boolean isRecordedOver;

    private static int maxDuration = 15000;   //最大录制时间 单位 毫秒

    private static int minDuration = 10;    //最小录制时间

    private static int refreshDuration = 50;    //btn 刷新间隔

    private int recorderTime = 0;    //记录录制时间

    private String mVideoPath;//视频存储路径

    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;

    private RelativeLayout mRl_preview;//预览页面 根
    private ReviewVideoView vv_play;// 播放view

    private ImageView iv_delete;//预览时删除按键
    private ImageView iv_finish;//预览时完成按键

    private RecordedButton rb_start;//长按录制按健

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A reference to the current {@link CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;
    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * The {@link Size} of video recording.
     */
    private Size mVideoSize;
    private CaptureRequest.Builder mPreviewBuilder;

    private Integer mSensorOrientation;


    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;


    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;


    public static Camera2VideoFragment newInstance() {
        return new Camera2VideoFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mainActivity = (VideoRecordActivity) getActivity();
        mVideoPath = getVideoFilePath(mainActivity);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        rb_start = (RecordedButton) view.findViewById(R.id.rb_start);

        mRl_preview = (RelativeLayout) view.findViewById(R.id.rl_preview);//预览页面 根
        vv_play = (ReviewVideoView) view.findViewById(R.id.vv_player);

        iv_delete = (ImageView) view.findViewById(R.id.iv_delete);//预览时删除按键
        iv_finish = (ImageView) view.findViewById(R.id.iv_finish);//预览时完成按键


        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vv_play.pause();
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.tip_deletefile)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //显示录制界面
                                switchPage(false);
                                //删除视频数据
                                deleteFile(mVideoPath);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        vv_play.start();
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                vv_play.start();
                            }
                        })
                        .show();

            }
        });
        iv_finish.setOnClickListener(new NoDoubleClickListener() {


            @Override
            protected void onNoDoubleClick(View view) {
                Bitmap map = getVideoThumbnail(mVideoPath);
                String ThumPath = bitmap2File(map, PubVar.mPath);
                if (EventActivityFragment.videoCallBack != null) {
                    EventActivityFragment.videoCallBack .OnClick("ok", ThumPath, mVideoPath);
                }
                if(EventEditActivity.videoCallBack!=null){
                    EventEditActivity.videoCallBack .OnClick("ok", ThumPath, mVideoPath);
                }

                //视频完成,关闭界面
                mainActivity.finish();

            }
        });

    }


//    private static long lastClickTime;
//    public static final int INTEGER_800 = 800;
//    public static boolean isClickable() {
//        long time = System.currentTimeMillis();
//        long timeD = time - lastClickTime;
//        if (0 < timeD && timeD < INTEGER_800) {
//            return false;
//        }
//        lastClickTime = time;
//        return true;
//    }

    //代码2
    public abstract class NoDoubleClickListener implements View.OnClickListener {

        public static final int MIN_CLICK_DELAY_TIME = 10000;
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                onNoDoubleClick(v);
            }
        }
        protected abstract void onNoDoubleClick(View view);
    }



    /**
     * 获取视频文件缩略图
     *
     * @param videoPath 视频文件的路径  如:/storage/emulated/0/IM/video/1523343139288.mp4
     * @return Bitmap 返回获取的Bitmap
     */
    public Bitmap getVideoThumbnail(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * Bitmap保存成File
     *
     * @param bitmap input bitmap
     * @return String output file's path
     */
    public static String bitmap2File(Bitmap bitmap, String path) {
        File f = new File(path, System.currentTimeMillis() + ".jpg");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return f.getAbsolutePath();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rb_start.setMax(maxDuration);
        rb_start.setOnGestureListener(new RecordedButton.OnGestureListener() {
            @Override
            public void onLongClick() {
                //长按录像
                isRecordedOver = false;
                startRecordingVideo();

                recorderTime = 0;
                myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, 0);

                Log.i(TAG, "onLongClick: ");
            }

            @Override
            public void onClick() {
                Log.i(TAG, "onClick: 这里可实现点击拍照 ");
            }

            @Override
            public void onLift() {
                Log.i(TAG, "onLift: 这个方法提供了 视频拼接 的ui实现，此处并未发挥作用，当中途停止录制时，即认为录制完成。");
                isRecordedOver = true;
                stopRecordingVideo();
            }

            @Override
            public void onOver() {
                Log.i(TAG, "onOver: ");
                isRecordedOver = true;
                rb_start.closeButton();

                stopRecordingVideo();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        //如果正在预览，开启预览
        if (mRl_preview.getVisibility() == View.VISIBLE) {
            vv_play.start();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        closeCamera();
        stopBackgroundThread();
        //如果正在预览，暂停预览
        if (mRl_preview.getVisibility() == View.VISIBLE) {
            vv_play.pause();
        }
        super.onPause();
    }


    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            Log.i(TAG, "view width == : " + width);
            Log.i(TAG, "view height == : " + height);

            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class), width, height);
            Log.i(TAG, "openCamera: mVideoSize == " + mVideoSize.toString());

            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            Log.i(TAG, "openCamera: mPreviewSize == " + mPreviewSize.toString());
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
//            int heights = outMetrics.heightPixels;
//            int widths = outMetrics.widthPixels;
//            int orientation = getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                mTextureView.setAspectRatio(widths + 50, heights);
//            } else {
//                mTextureView.setAspectRatio(heights, widths + 50);
//            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseVideoSize(Size[] choices, int width, int height) {


//        for (Size size : choices) {
//            Log.i(TAG, "chooseVideoSize: " + size.toString());
//        }

        List<Size> bigEnough = new ArrayList<>();
        //根据相机界面的宽高信息去选择size，宽度相等，高度大于等于相机的界面，并选择最小高度，这样就可以实现全屏拍摄。事实上视频的实际画面比你看到的预览画面略微大一点，
        //比如  (1加6手机)我得到的相机界面的宽高是  width = 1080 ，heigh = 2134,得到的bigEnough 是
//        2019-05-15 18:00:43.467 15700-15700/com.lq.videodemo2 I/qingyang: bigEnough0: 2340x1080
//        2019-05-15 18:00:43.467 15700-15700/com.lq.videodemo2 I/qingyang: bigEnough0: 2280x1080
//        2019-05-15 18:00:43.467 15700-15700/com.lq.videodemo2 I/qingyang: bigEnough0: 2160x1080
        //实际选择的是 2160x1080 ，即拍摄的实际尺寸是2160*1080，而在拍摄时和预览时看到的尺寸是2134*1080.影响不大。


        for (Size option : choices) {

//            Log.i(TAG, "chooseVideoSize2: " + width + "   " + height);
//            Log.i(TAG, "chooseVideoSize: " + option.getWidth() + "   " + option.getHeight());

            //option.getHeight() -width <=option.getHeight() * 0.1 这个条件解决  这个问题 在预览界面 使手机熄屏或退出到后台，再次开启宽度变了。。。。1080 变为了 1067 。。。。。还有720 变为了639。。wtf
            //宽高信息相反
            if ((option.getHeight() == width || option.getHeight() - width <= option.getHeight() * 0.1) && option.getWidth() >= height) {
                bigEnough.add(option);
                continue;
            }
            //兼容宽高信息不反的，并未发现出现此种情况，
            if (option.getWidth() == width && option.getHeight() >= height) {
                bigEnough.add(option);
            }


        }

//        for (Size size : bigEnough) {
//            Log.i(TAG, "bigEnough0: " + size.toString());
//        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        Log.i(TAG, "chooseVideoSize: 进入4:3选择");

        //如果未找到宽高一致的，进入4:3 的选择方式，可能存在的问题：1，拍摄时的画面并没有充满全屏，下方留有黑屏。2，预览时会全屏预览，高度上会有拉伸视觉效果；

        for (Size size : choices) {

            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080 &&
                    size.getWidth() >= width) {
                return size;
            }
        }

        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {

//        for (Size size : choices) {
//            Log.i(TAG, "chooseOptimalSize: " + size.toString());
//        }
//        Log.i(TAG, "chooseOptimalSize: width == " + width);
//        Log.i(TAG, "chooseOptimalSize: height == " + height);
//        Log.i(TAG, "chooseOptimalSize: aspectRatio == " + aspectRatio.toString());

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width) {
                bigEnough.add(option);
            }
        }
//        for (Size size : bigEnough) {
//            Log.i(TAG, "bigEnough: " + size.toString());
//        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * 开启
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Start recording
                            mMediaRecorder.start();

                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 关闭
     */
    private void stopRecordingVideo() {
        if (recorderTime < minDuration) {
            Activity activity = getActivity();
            if (null != activity) {
                Toast.makeText(activity, mainActivity.getResources().getString(R.string.time_short_tip),
                        Toast.LENGTH_LONG).show();
                return;

            }
            try {
                recorderTime = 0;
                rb_start.setProgress(0);
                mMediaRecorder.stop();
                deleteFile(getVideoFilePath(mainActivity));
                startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;

        }
        recorderTime = 0;
        rb_start.setProgress(0);
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();


        startPreview();

        openPreview();
    }

    /**
     * 视频录制完毕打开预览页面
     */
    private void openPreview() {

        rb_start.setVisibility(View.GONE);//解决按钮共存问题，3个按键同时出现，看着不舒服

        switchPage(true);
        vv_play.pause();
        myHandler.sendEmptyMessageDelayed(HANDLER_OPEN_PREVIEW, 5);//给个延时，似乎没必要，因为视频立即就可以打开了，以为视频保存需要时间

        rb_start.setVisibility(View.VISIBLE);//解决按钮共存问题，3个按键同时出现，看着不舒服

    }

    /**
     * 传入 true ，切换到  预览页面
     *
     * @param isPreviewPage
     */
    private void switchPage(boolean isPreviewPage) {
        if (isPreviewPage) {
            mRl_preview.setVisibility(View.VISIBLE);
        } else {
            mRl_preview.setVisibility(View.GONE);
            vv_play.release();

        }
    }


    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {


        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            mainActivity.finish();
        }

    };

    /**
     * Start the camera preview.
     */
    @SuppressLint("NewApi")
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mVideoPath == null || mVideoPath.isEmpty()) {
            mVideoPath = getVideoFilePath(mainActivity);
        }
        mMediaRecorder.setOutputFile(mVideoPath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * 存储路径
     *
     * @param context
     * @return
     */
    private String getVideoFilePath(Context context) {
        File path = new File(PubVar.mPath, System.currentTimeMillis() + ".mp4");
        Log.d("TAG", path.getAbsolutePath());
        return (path == null ? "" : (path.getAbsolutePath()));
    }

    // 权限start----------------------------


    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

//权限end----------------------


    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };


    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_RECORD: {
                    //拍摄视频的handler
                    if (!isRecordedOver) {
                        rb_start.setProgress(recorderTime);
                        recorderTime += refreshDuration;
                        myHandler.sendEmptyMessageDelayed(HANDLER_RECORD, refreshDuration);

                    }

                }
                break;

                case HANDLER_OPEN_PREVIEW: {
                    vv_play.setVideoPath(mVideoPath);
                    vv_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(true);
                            vv_play.start();
                        }
                    });
                    if (vv_play.isPrepared()) {
                        vv_play.setLooping(true);
                        vv_play.start();
                    }
                }
                break;

            }
        }
    };

    /**
     * 删除文件
     *
     * @param path
     */
    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        } else {
            Log.d(TAG, "deleteFile: 文件不存在！");
        }

    }

}
