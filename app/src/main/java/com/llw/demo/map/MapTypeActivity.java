package com.llw.demo.map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.RadioGroup;

import com.llw.demo.R;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

/**
 * 地图类型
 *
 * @author llw
 */
public class MapTypeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private MapView mapView;
    private RadioGroup radioGroup;
    private TencentMap tencentMap;
    //地图生命周期
    private MapLifecycle mapLifecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_type);

        mapView = findViewById(R.id.mapView);
        radioGroup = findViewById(R.id.rp_map_type);
        radioGroup.setOnCheckedChangeListener(this);
        tencentMap = mapView.getMap();
        //设置为经典样式
        tencentMap.setMapStyle(2);

        mapLifecycle = new MapLifecycle(mapView);
        getLifecycle().addObserver(mapLifecycle);

    }

    /**
     * 类型选中监听
     *
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_normal: //普通地图-默认地图类型
                tencentMap.setTrafficEnabled(false);
                tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
                break;
            case R.id.rb_satellite: //卫星地图
                tencentMap.setTrafficEnabled(false);
                tencentMap.setMapType(TencentMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.rb_dark: //暗色地图
                tencentMap.setTrafficEnabled(false);
                tencentMap.setMapType(TencentMap.MAP_TYPE_DARK);
                break;
            case R.id.rb_traffic://路况图
                tencentMap.setTrafficEnabled(true);
                break;
            default:
                break;
        }
    }
}
