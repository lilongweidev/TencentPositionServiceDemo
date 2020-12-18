package com.llw.demo.map;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.llw.demo.R;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;
import com.tencent.tencentmap.mapsdk.maps.UiSettings;

/**
 * 基础地图
 *
 * @author llw
 */
public class BaseMapActivity extends AppCompatActivity implements LocationSource,
        TencentLocationListener {

    //基础地图
    private MapView mapView;
    //腾讯地图
    private TencentMap tencentMap;
    //地图生命周期
    private MapLifecycle mapLifecycle;

    //定位管理
    private TencentLocationManager locationManager;
    //定位请求
    private TencentLocationRequest locationRequest;
    //定位数据源监听
    private LocationSource.OnLocationChangedListener locationChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_map);
        //页面初始化
        initView();

        //初始化定位
        initLocation();
    }

    /**
     * 页面初始化
     */
    private void initView() {
        //地图
        mapView = findViewById(R.id.mapView);
        //创建tencentMap地图对象
        tencentMap = mapView.getMap();
        //设置为经典样式
        tencentMap.setMapStyle(2);

        //设置最大缩放等级 最大值 20 最小值 1
        tencentMap.setMaxZoomLevel(20);
        //设置最小缩放等级 最大值 20 最小值 1
        tencentMap.setMinZoomLevel(18);
        //启用3D视图
        tencentMap.setBuilding3dEffectEnable(true);

        //获取地图UI设置对象
        UiSettings uiSettings = tencentMap.getUiSettings();
        //设置logo的大小   比例范围(0.7~1.3)
        uiSettings.setLogoScale(0.7f);

        //设置logo底部居中
        uiSettings.setLogoPosition(TencentMapOptions.LOGO_POSITION_BOTTOM_CENTER);
        //设置logo左下角显示同时设置与MapView的左边距和下边距为100
        //uiSettings.setLogoPosition(TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT, new int[]{100, 100});

        //设置比例尺是否显示  true显示，false不显示，不设置则为默认值，默认为true
        uiSettings.setScaleViewEnabled(true);
        //设置比例尺显示在右下角
        uiSettings.setScaleViewPosition(TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_RIGHT);

        //设置指南针是否显示 true显示， false不显示，不设置则为默认值，默认为false
        uiSettings.setCompassEnabled(true);
        //设置指南针的填充值。一个值 为设置指南针上边缘距离 MapView 上边缘的距离为100px
        // 两个值 为设置指南针距离 MapView 左边缘和上边缘的距离，第一个值为左，第二个值为上。
        uiSettings.setCompassExtraPadding(20, 20);

        //设置缩放控件是否显示，true显示，false不显示，不设置则为默认值，默认为false
        uiSettings.setZoomControlsEnabled(true);
        //设置缩放控件显示在左下角
        uiSettings.setZoomPosition(TencentMapOptions.ZOOM_POSITION_BOTTOM_LEFT);


        mapLifecycle = new MapLifecycle(mapView);
        //将观察者与被观察者绑定起来
        getLifecycle().addObserver(mapLifecycle);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        locationManager = TencentLocationManager.getInstance(this);
        //设置坐标系
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        //创建定位请求
        locationRequest = TencentLocationRequest.create();
        //设置定位周期（位置监听器回调周期）为3s
        locationRequest.setInterval(3000);

        //地图上设置定位数据源
        tencentMap.setLocationSource(this);
        //设置当前位置可见
        tencentMap.setMyLocationEnabled(true);
    }

    /**
     * 接收定位结果
     */
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int code, String reason) {

        if (code == TencentLocation.ERROR_OK && locationChangedListener != null) {
            //重新构建一个定位对象
            Location location = new Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            Log.d("location-->", tencentLocation.getLatitude() + "==" + tencentLocation.getLongitude());
            //更改位置定位，此时地图上就会显示当前定位到位置
            locationChangedListener.onLocationChanged(location);
        }
    }

    /**
     * 用于接收GPS、WiFi、Cell状态码
     */
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        //GPS, WiFi, Radio 等状态发生变化
        Log.v("State changed", name + "===" + desc);
    }

    /**
     * 启用
     *
     * @param onLocationChangedListener 数据源更改监听
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;

        int error = locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
        switch (error) {
            case 1:
                showMsg("设备缺少使用腾讯定位服务需要的基本条件");
                break;
            case 2:
                showMsg("AndroidManifest 中配置的 key 不正确");
                break;
            case 3:
                showMsg("自动加载libtencentloc.so失败");
                break;
            default:
                break;
        }
    }

    /**
     * 停用
     */
    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
        locationManager = null;
        locationRequest = null;
        locationChangedListener = null;
    }

    /**
     * Toast提示
     *
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
