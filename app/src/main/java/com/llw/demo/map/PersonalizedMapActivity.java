package com.llw.demo.map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.RadioGroup;

import com.llw.demo.R;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;

/**
 * 个性化地图
 *
 * @author llw
 */
public class PersonalizedMapActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private MapView mapView;
    private TencentMap tencentMap;
    private MapLifecycle mapLifecycle;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalized_map);

        mapView = findViewById(R.id.mapView);
        tencentMap = mapView.getMap();

        mapLifecycle = new MapLifecycle(mapView);
        getLifecycle().addObserver(mapLifecycle);

        radioGroup = findViewById(R.id.rp_style);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_normal://经典
                tencentMap.setMapStyle(2);
                break;
            case R.id.rb_style_one://烟翠
                tencentMap.setMapStyle(1);
                break;
            case R.id.rb_style_two://墨渊
                tencentMap.setMapStyle(3);
                break;
            default:
                break;
        }
    }
}
