package com.example.shiyu.client;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;

import com.baidu.mapapi.map.MapView;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("NewApi")
public class TraceActivity extends FragmentActivity implements OnClickListener {

    protected static Trace trace = null;

    protected static String entityName = null;

    protected static long serviceId = 107628; // serviceId为开发者创建的鹰眼服务ID

    private int traceType = 2;

    protected static LBSTraceClient client = null;

    protected static OnEntityListener entityListener = null;

    private Button btnTrackUpload;
    private Button btnTrackQuery;

    protected static MapView bmapView = null;
    protected static BaiduMap mBaiduMap = null;

    private FragmentManager fragmentManager;

    private TrackUploadFragment mTrackUploadFragment;

    private TrackQueryFragment mTrackQueryFragment;

    protected static Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.map_content);

        mContext = getApplicationContext();

        client = new LBSTraceClient(mContext);

        entityName = getImei(mContext);
        Log.e("entityName ", entityName);

        trace = new Trace(getApplicationContext(), serviceId, entityName,
                traceType);

        initComponent();

        initOnEntityListener();

        addEntity();

        setDefaultFragment();

    }

    private void addEntity() {
        Geofence.addEntity();
    }

    private void initComponent() {

        btnTrackUpload = (Button) findViewById(R.id.btn_trackupload);
        btnTrackQuery = (Button) findViewById(R.id.btn_trackquery);

        btnTrackUpload.setOnClickListener(this);
        btnTrackQuery.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();

        bmapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = bmapView.getMap();
        bmapView.showZoomControls(false);
    }


    private void setDefaultFragment() {
        handlerButtonClick(R.id.btn_trackupload);

    }

    public void onClick(View v) {
        handlerButtonClick(v.getId());
    }

    private void initOnEntityListener() {
        entityListener = new OnEntityListener() {

            @Override
            public void onRequestFailedCallback(String arg0) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(),
                        "entity请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT)
                        .show();
                Looper.loop();
            }

            @Override
            public void onAddEntityCallback(String arg0) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(),
                        "添加entity回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryEntityListCallback(String message) {}

            @Override
            public void onReceiveLocation(TraceLocation location) {
                if (mTrackUploadFragment != null) {
                    mTrackUploadFragment.showRealtimeTrack(location);
                }
            }

        };
    }

    private void handlerButtonClick(int id) {
        onResetButton();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);

        switch (id) {

            case R.id.btn_trackquery:

                TrackUploadFragment.isInUploadFragment = false;

                if (mTrackQueryFragment == null) {
                    mTrackQueryFragment = new TrackQueryFragment();
                    transaction.add(R.id.fragment_content, mTrackQueryFragment);
                } else {
                    transaction.show(mTrackQueryFragment);
                }
                mTrackQueryFragment.addMarker();
                btnTrackQuery.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
                btnTrackQuery.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
                mBaiduMap.setOnMapClickListener(null);
                break;

            case R.id.btn_trackupload:

                TrackUploadFragment.isInUploadFragment = true;

                if (mTrackUploadFragment == null) {
                    mTrackUploadFragment = new TrackUploadFragment();
                    transaction.add(R.id.fragment_content, mTrackUploadFragment);
                } else {
                    transaction.show(mTrackUploadFragment);
                }

                TrackUploadFragment.addMarker();
                btnTrackUpload.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
                btnTrackUpload.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
                mBaiduMap.setOnMapClickListener(null);
                break;
        }
        transaction.commit();

    }

    private void onResetButton() {
        btnTrackQuery.setTextColor(Color.rgb(0x00, 0x00, 0x00));
        btnTrackQuery.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
        btnTrackUpload.setTextColor(Color.rgb(0x00, 0x00, 0x00));
        btnTrackUpload.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
    }

    private void hideFragments(FragmentTransaction transaction) {

        if (mTrackQueryFragment != null) {
            transaction.hide(mTrackQueryFragment);
        }
        if (mTrackUploadFragment != null) {
            transaction.hide(mTrackUploadFragment);
        }
        mBaiduMap.clear();
    }

    protected static String getImei(Context context) {
        String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("获取IMEI码失败");
            mImei = "NULL";
        }
        return mImei;
    }

    @Override
    protected void onPause() {
        super.onPause();
        TrackUploadFragment.isInUploadFragment = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.onDestroy();
    }

}
