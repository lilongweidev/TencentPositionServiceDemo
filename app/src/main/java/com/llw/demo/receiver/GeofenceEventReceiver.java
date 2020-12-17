package com.llw.demo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class GeofenceEventReceiver extends BroadcastReceiver {

    private static final String ACTION_TRIGGER_GEOFENCE = "com.llw.demo.receiver.GeofenceEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null
                || !GeofenceEventReceiver.ACTION_TRIGGER_GEOFENCE.equals(intent
                .getAction())) {
            return;
        }

        String tag = intent.getStringExtra("tag");
        double lng = intent.getDoubleExtra("longitude", 0);
        double lat = intent.getDoubleExtra("latitude", 0);

        // 进入围栏还是退出围栏
        boolean enter = intent.getBooleanExtra(
                LocationManager.KEY_PROXIMITY_ENTERING, true);
        Toast.makeText(context,"是否进入围栏范围："+ enter,Toast.LENGTH_SHORT).show();

    }
}
