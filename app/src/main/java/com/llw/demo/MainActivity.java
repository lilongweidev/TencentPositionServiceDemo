package com.llw.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.llw.demo.map.BaseMapActivity;
import com.llw.demo.map.MarkerActivity;
import com.llw.demo.map.MapTypeActivity;
import com.llw.demo.map.PersonalizedMapActivity;
import com.llw.demo.receiver.GeofenceEventReceiver;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.map.geolocation.TencentGeofenceManager;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentPoi;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TencentLocationListener {

    public static final String TAG = "MainActivity";

    //定位信息显示
    private TextView tvLocationInfo;
    //连续定位按钮
    private Button btnContinuousPositioning;
    //停止定位按钮
    private Button btnStopPositioning;
    //单次定位
    private Button btnSinglePositioning;
    //后台定位
    private Button btnBackgroundPositioning;
    //添加围栏
    private Button btnAddFence;
    //移除围栏
    private Button btnRemoveFence;
    //基础地图
    private Button btnBaseMap;
    //地图类型
    private Button btnMapType;
    //个性化地图
    private Button btnPersonalizedMap;
    //地图覆盖物
    private Button btnMapCover;

    //定位管理
    private TencentLocationManager mLocationManager;
    //定位请求
    private TencentLocationRequest locationRequest;

    //权限
    private RxPermissions rxPermissions;

    //定位模式
    private String positioningMode = null;

    //通知管理
    private NotificationManager notificationManager;
    //通知ID
    private static final int LOC_NOTIFICATIONID = 20;
    //通知渠道名
    private static final String NOTIFICATION_CHANNEL_NAME = "后台定位";
    //是否创建了通知渠道
    private boolean isCreateChannel = false;

    //地理围栏管理
    private TencentGeofenceManager mTencentGeofenceManager;
    //围栏别名
    private String geofenceName = "测试范围";
    //围栏
    private TencentGeofence geofence;
    //设置动作
    private static final String ACTION_TRIGGER_GEOFENCE = "com.llw.demo.receiver.GeofenceEventReceiver";
    //PendingIntent
    private PendingIntent pi;

    //广播接收器
    private GeofenceEventReceiver geofenceEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        //初始化定位
        initLocation();
        //初始化地理围栏
        initGeofence();
        //检查版本
        checkVersion();
    }

    /**
     * 初始化地理围栏
     */
    private void initGeofence() {
        //实例化
        mTencentGeofenceManager = new TencentGeofenceManager(this);
        //地理围栏构建
        TencentGeofence.Builder builder = new TencentGeofence.Builder();
        geofence = builder.setTag(geofenceName)
                //设置圆心和半径，v 是 纬度，v1 是经度，v2 是半径 500米
                .setCircularRegion(22.5, 113.9, 500)
                //设置地理围栏有效期
                .setExpirationDuration(3 * 3600 * 1000)
                //完成构建
                .build();

        //构建Action和传递信息
        Intent receiverIntent = new Intent(ACTION_TRIGGER_GEOFENCE);
        receiverIntent.putExtra("tag", geofence.getTag());
        receiverIntent.putExtra("longitude", geofence.getLongitude());
        receiverIntent.putExtra("latitude", geofence.getLatitude());


        // 随机产生的 requestCode, 避免冲突
        int requestCode = (int) (Math.random() * 1E7);
        //构建PendingIntent
        pi = PendingIntent.getBroadcast(this, requestCode,
                receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //实例化
        geofenceEventReceiver = new GeofenceEventReceiver();
        //添加动作拦截
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TRIGGER_GEOFENCE);
        //注册动态广播  记得去掉AndroidManifest.xml的静态广播配置
        registerReceiver(geofenceEventReceiver, intentFilter);

    }

    /**
     * 页面初始化
     */
    private void initView() {
        //定位
        tvLocationInfo = findViewById(R.id.tv_location_info);
        btnContinuousPositioning = findViewById(R.id.btn_continuous_positioning);
        btnStopPositioning = findViewById(R.id.btn_stop_positioning);
        btnSinglePositioning = findViewById(R.id.btn_single_positioning);
        btnBackgroundPositioning = findViewById(R.id.btn_background_positioning);
        btnAddFence = findViewById(R.id.btn_add_fence);
        btnRemoveFence = findViewById(R.id.btn_remove_fence);
        btnBaseMap = findViewById(R.id.btn_base_map);
        btnMapType = findViewById(R.id.btn_map_type);
        btnPersonalizedMap = findViewById(R.id.btn_personalized_map);
        btnMapCover = findViewById(R.id.btn_map_cover);

        btnContinuousPositioning.setOnClickListener(this);
        btnStopPositioning.setOnClickListener(this);
        btnSinglePositioning.setOnClickListener(this);
        btnBackgroundPositioning.setOnClickListener(this);
        btnAddFence.setOnClickListener(this);
        btnRemoveFence.setOnClickListener(this);
        btnBaseMap.setOnClickListener(this);
        btnMapType.setOnClickListener(this);
        btnPersonalizedMap.setOnClickListener(this);
        btnMapCover.setOnClickListener(this);

        //实例化
        rxPermissions = new RxPermissions(this);
    }

    /**
     * 初始化定位信息
     */
    private void initLocation() {
        //获取TencentLocationManager实例
        mLocationManager = TencentLocationManager.getInstance(this);
        //获取定位请求TencentLocationRequest 实例
        locationRequest = TencentLocationRequest.create();
        //设置定位时间间隔，10s
        locationRequest.setInterval(5000);
        //位置信息的详细程度 REQUEST_LEVEL_ADMIN_AREA表示获取经纬度，位置所处的中国大陆行政区划
        locationRequest.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
        //是否允许使用GPS
        locationRequest.setAllowGPS(true);
        //是否需要获取传感器方向，提高室内定位的精确度
        locationRequest.setAllowDirection(true);
        //是否需要开启室内定位
        locationRequest.setIndoorLocationMode(true);
    }

    /**
     * 检查Android版本
     */
    private void checkVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0或6.0以上
            //动态权限申请
            permissionsRequest();
        } else {
            showMsg("您不需要动态获得权限，可以直接定位");
        }
    }

    /**
     * 动态权限申请
     */
    private void permissionsRequest() {//使用这个框架使用了Lambda表达式，设置JDK版本为 1.8或者更高
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                .subscribe(granted -> {
                    if (granted) {//申请成功
                        //发起连续定位请求
                        //showMsg("您已获得权限，可以定位了");
                        Log.d(TAG,"您已获得权限，可以定位了");
                    } else {//申请失败
                        showMsg("权限未开启");
                    }
                });
    }

    /**
     * 页面控件点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_continuous_positioning:
                //连续定位
                tvLocationInfo.setText("定位中");
                positioningMode = "连续定位";
                mLocationManager.requestLocationUpdates(locationRequest, this);
                break;
            case R.id.btn_single_positioning:
                //单次定位
                positioningMode = "单次定位";
                mLocationManager.requestSingleFreshLocation(locationRequest, this, getMainLooper());
                break;
            case R.id.btn_background_positioning:
                //后台定位
                positioningMode = "后台定位";
                mLocationManager.enableForegroundLocation(LOC_NOTIFICATIONID, buildNotification());
                mLocationManager.requestLocationUpdates(locationRequest, this, getMainLooper());
                break;
            case R.id.btn_stop_positioning:
                //停止定位
                stopPositioning();
                break;
            case R.id.btn_add_fence:
                //添加围栏
                mTencentGeofenceManager.addFence(geofence, pi);
                showMsg("地理围栏已添加，请在附近溜达一下");
                break;
            case R.id.btn_remove_fence:
                //移除围栏
                //指定围栏对象移除
                mTencentGeofenceManager.removeFence(geofence);
                //通过tag移除
                //mTencentGeofenceManager.removeFence(geofenceName);
                showMsg("地理围栏已移除，撒有那拉！");
                break;
            case R.id.btn_base_map:
                //基础地图
                goToActivity(BaseMapActivity.class);
                break;
            case R.id.btn_map_type:
                //地图类型
                goToActivity(MapTypeActivity.class);
                break;
            case R.id.btn_personalized_map:
                //个性化地图
                goToActivity(PersonalizedMapActivity.class);
                break;
            case R.id.btn_map_cover:
                //地图覆盖物
                goToActivity(MarkerActivity.class);
                break;
            default:
                break;
        }
    }

    /**
     * 跳转页面
     *
     * @param clazz 目标Activity
     */
    private void goToActivity(Class<? extends AppCompatActivity> clazz) {
        startActivity(new Intent(this, clazz));
    }

    /**
     * 停止定位
     */
    private void stopPositioning() {
        if (positioningMode == null) {
            showMsg("您还没有开始定位呢？");
            return;
        }
        if ("单次定位".equals(positioningMode)) {
            showMsg("单次定位会自动停止，但您想点一下我也没办法！╮(╯▽╰)╭");
            return;
        }
        mLocationManager.removeUpdates(this);
        if ("后台定位".equals(positioningMode)) {
            //关闭后台定位
            mLocationManager.disableForegroundLocation(true);
        }
        showMsg("定位已停止");
    }

    /**
     * 创建通知
     *
     * @return
     */
    private Notification buildNotification() {
        Notification.Builder builder = null;
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Android 8.0及以后对Notification进行了修改 需要创建通知渠道
            if (notificationManager == null) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                //是否在桌面icon右上角展示小圆点
                notificationChannel.enableLights(true);
                //小圆点颜色
                notificationChannel.setLightColor(Color.BLUE);
                //是否在久按桌面图标时显示此渠道的通知
                notificationChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("PositionServiceDemo")
                .setContentText("正在后台运行")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }

    /**
     * Toast提示
     *
     * @param msg 内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 接收定位结果
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        //显示定位信息
        showLocationInfo(location);
    }

    /**
     * 用于接收GPS、WiFi、Cell状态码
     */
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        Log.d(TAG, "name：" + name + " desc：" + statusUpdate(name, status));
    }

    /**
     * 显示定位信息
     *
     * @param location
     */
    private void showLocationInfo(TencentLocation location) {
        //经度
        double longitude = location.getLongitude();
        //纬度
        double latitude = location.getLatitude();
        //准确性
        float accuracy = location.getAccuracy();
        //地址信息
        String address = location.getAddress();
        //海拔高度
        double altitude = location.getAltitude();
        //面积统计
        Integer areaStat = location.getAreaStat();
        //方向
        float bearing = location.getBearing();
        double direction = location.getDirection();
        //城市
        String city = location.getCity();
        //城市代码
        String cityCode = location.getCityCode();
        //城市电话代码
        String cityPhoneCode = location.getCityPhoneCode();
        //坐标类型
        int coordinateType = location.getCoordinateType();
        //区
        String district = location.getDistrict();
        //经过时间
        long elapsedRealtime = location.getElapsedRealtime();
        //Gps信息
        int gpsRssi = location.getGPSRssi();
        //室内建筑
        String indoorBuildingFloor = location.getIndoorBuildingFloor();
        //室内建筑编码
        String indoorBuildingId = location.getIndoorBuildingId();
        //室内位置类型
        int indoorLocationType = location.getIndoorLocationType();
        //名称
        String name = location.getName();
        //国家
        String nation = location.getNation();
        //周边poi信息列表
        List<TencentPoi> poiList = location.getPoiList();
        //提供者
        String provider = location.getProvider();
        //省
        String province = location.getProvince();
        //速度
        float speed = location.getSpeed();
        //街道
        String street = location.getStreet();
        //街道编号
        String streetNo = location.getStreetNo();
        //时间
        long time = location.getTime();
        //镇
        String town = location.getTown();
        //村
        String village = location.getVillage();

        StringBuffer buffer = new StringBuffer();
        buffer.append("定位模式：" + positioningMode + "\n");
        buffer.append("经度：" + longitude + "\n");
        buffer.append("纬度：" + latitude + "\n");
        buffer.append("国家：" + nation + "\n");
        buffer.append("省：" + province + "\n");
        buffer.append("市：" + city + "\n");
        buffer.append("县/区：" + district + "\n");
        buffer.append("街道：" + street + "\n");
        //buffer.append("名称：" + name + "\n");
        buffer.append("提供者：" + provider + "\n");
        buffer.append("详细地址：" + address + "\n");
        tvLocationInfo.setText(buffer.toString());
    }

    /**
     * 定位状态判断
     *
     * @param name   GPS、WiFi、Cell
     * @param status 状态码
     * @return
     */
    private String statusUpdate(String name, int status) {
        if ("gps".equals(name)) {
            switch (status) {
                case 0:
                    return "GPS开关关闭";
                case 1:
                    return "GPS开关打开";
                case 3:
                    return "GPS可用，代表GPS开关打开，且搜星定位成功";
                case 4:
                    return "GPS不可用";
                default:
                    return "";
            }
        } else if ("wifi".equals(name)) {
            switch (status) {
                case 0:
                    return "Wi-Fi开关关闭";
                case 1:
                    return "Wi-Fi开关打开";
                case 2:
                    return "权限被禁止，禁用当前应用的 ACCESS_COARSE_LOCATION 等定位权限";
                case 5:
                    return "位置信息开关关闭，在android M系统中，此时禁止进行Wi-Fi扫描";
                default:
                    return "";
            }
        } else if ("cell".equals(name)) {
            switch (status) {
                case 0:
                    return "cell 模块关闭";
                case 1:
                    return "cell 模块开启";
                case 2:
                    return "定位权限被禁止，位置权限被拒绝通常发生在禁用当前应用的 ACCESS_COARSE_LOCATION 等定位权限";
                default:
                    return "";
            }
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有围栏
        mTencentGeofenceManager.removeAllFences();
        //销毁围栏管理
        mTencentGeofenceManager.destroy();
    }
}
