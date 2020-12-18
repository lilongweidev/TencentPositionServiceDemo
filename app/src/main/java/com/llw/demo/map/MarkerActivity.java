package com.llw.demo.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.llw.demo.R;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

/**
 * 地图覆盖物
 *
 * @author llw
 */
public class MarkerActivity extends AppCompatActivity {

    private MapView mapView;
    private TencentMap tencentMap;
    private MapLifecycle mapLifecycle;

    //默认标注
    private Marker defaultMarker;
    //自定义标注
    private Marker customMarker;
    //自定义标注 显示默认信息窗口
    private Marker customMarkerDefaultInfoWindow;
    //自定义标注 显示自定义信息窗口
    private Marker customMarkerCustomInfoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        mapView = findViewById(R.id.mapView);
        tencentMap = mapView.getMap();
        mapLifecycle = new MapLifecycle(mapView);
        getLifecycle().addObserver(mapLifecycle);
    }

    /**
     * 创建选项菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.marker_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 菜单选择
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_default_marker:
                //添加默认标注点
                addDefaultMarker();
                break;
            case R.id.menu_add_custom_marker:
                //添加自定义标注
                addCustomMarker();
                break;
            case R.id.menu_add_default_info_window:
                //添加默认信息窗口
                addDefaultInfoWindow();
                break;
            case R.id.menu_add_custom_info_window:
                //添加自定义信息窗口
                addCustomInfoWindow();
                break;
            case R.id.menu_remove_marker:
                //移除标注
                removeMarker();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 添加默认标注
     */
    private void addDefaultMarker() {
        LatLng latLng = new LatLng(22.540893, 113.949082);
        defaultMarker = tencentMap.addMarker(new MarkerOptions(latLng));
        //移动地图
        tencentMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 添加自定义标注
     */
    private void addCustomMarker() {
        //创建Marker对象之前，设置属性
        LatLng latLng = new LatLng(40.011313, 116.391907);
        BitmapDescriptor custom = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        customMarker = tencentMap.addMarker(new MarkerOptions(latLng)
                //设置图标
                .icon(custom)
                //设置图标透明度
                .alpha(0.8f));
        //移动地图
        tencentMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 添加默认信息窗口
     */
    private void addDefaultInfoWindow() {
        //通过MarkerOptions配置
        LatLng latLng = new LatLng(39.908710, 116.397499);
        MarkerOptions options = new MarkerOptions(latLng);
        options.title("天安门")//标注的InfoWindow的标题
                .snippet("地址: 北京市东城区东长安街")//标注的InfoWindow的内容
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));//设置自定义Marker图标
        customMarkerDefaultInfoWindow = tencentMap.addMarker(options);
        //开启信息窗口
        customMarkerDefaultInfoWindow.setInfoWindowEnable(true);
        //移动地图
        tencentMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 添加自定义信息窗口
     */
    private void addCustomInfoWindow() {
        //上海虹桥机场经纬度
        LatLng latLng = new LatLng(31.19590, 121.34113);
        MarkerOptions options = new MarkerOptions(latLng);
        options.title("上海市")//标注的InfoWindow的标题
                .snippet("地址: 虹桥机场")//标注的InfoWindow的内容
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        customMarkerCustomInfoWindow = tencentMap.addMarker(options);
        //移动地图
        tencentMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //设置自定义信息窗口
        tencentMap.setInfoWindowAdapter(new TencentMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return createCustomInfoView(marker);
            }

            @Override
            public View getInfoContents(Marker marker) {
                return createCustomInfoView(marker);
            }

            private View createCustomInfoView(Marker marker) {
                LinearLayout mCustomInfoWindowView = (LinearLayout) View.inflate(
                        getApplicationContext(), R.layout.custom_infowindow, null);
                TextView tvContent = mCustomInfoWindowView.findViewById(R.id.tv_content);
                //设置自定义信息窗的内容
                tvContent.setText("自定义信息窗口:");
                tvContent.append("\n" + marker.getTitle());
                tvContent.append("\n" + marker.getSnippet());
                return mCustomInfoWindowView;
            }
        });
        //窗口点击事件
        tencentMap.setOnInfoWindowClickListener(new TencentMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "信息窗被点击\n" + marker.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInfoWindowClickLocation(int i, int i1, int i2, int i3) {
                Toast.makeText(getApplicationContext(),
                        "尺寸：" + i + "x" + i1 + " 位置：" + i2 + "," + i3, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 移除标注
     */
    private void removeMarker() {

        //移除默认标注
        if (defaultMarker != null) {
            defaultMarker.remove();
            defaultMarker = null;
        }
        //移除自定义标注
        if (customMarker != null) {
            customMarker.remove();
            customMarker = null;
        }
        //移除自定义标注  显示默认信息窗口
        if (customMarkerDefaultInfoWindow != null) {
            customMarkerDefaultInfoWindow.remove();
            customMarkerDefaultInfoWindow = null;
        }
        //移除自定义标注  显示自定义信息窗口
        if (customMarkerCustomInfoWindow != null) {
            customMarkerCustomInfoWindow.remove();
            customMarkerCustomInfoWindow = null;
        }
    }
}
