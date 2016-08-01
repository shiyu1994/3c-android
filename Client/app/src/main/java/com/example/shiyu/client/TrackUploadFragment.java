package com.example.shiyu.client;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.TraceLocation;
import com.baidu.trackutils.GsonService;
import com.baidu.trackutils.HistoryTrackData;
import com.baidu.trackutils.RealtimeTrackData;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class TrackUploadFragment extends Fragment {

    private Button btnStartTrace = null;

    private Button btnStopTrace = null;

    private Button btnOperator = null;

    protected TextView tvEntityName = null;

    private Geofence geoFence = null;

    protected static OnStartTraceListener startTraceListener = null;

    protected static OnStopTraceListener stopTraceListener = null;

    private int gatherInterval = 5;

    private static BitmapDescriptor realtimeBitmap;

    protected static OverlayOptions overlay;

    private static PolylineOptions polyline = null;

    private static List<LatLng> pointList = new ArrayList<LatLng>();

    protected boolean isTraceStart = false;

    protected RefreshThread refreshThread = null;

    protected static MapStatusUpdate msUpdate = null;

    private View view = null;

    private LayoutInflater mInflater = null;

    protected static boolean isInUploadFragment = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_trackupload, container, false);

        mInflater = inflater;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        init();

        initListener();

        setInterval();

        setRequestType();
    }

    private void init() {

        btnStartTrace = (Button) view.findViewById(R.id.btn_starttrace);

        btnStopTrace = (Button) view.findViewById(R.id.btn_stoptrace);

        btnOperator = (Button) view.findViewById(R.id.btn_operator);

        tvEntityName = (TextView) view.findViewById(R.id.tv_entityName);

        tvEntityName.setText(" entityName : " + TraceActivity.entityName + " ");

        btnStartTrace.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(getActivity(), "正在开启轨迹服务，请稍候", Toast.LENGTH_LONG).show();
                startTrace();
            }
        });

        btnStopTrace.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(getActivity(), "正在停止轨迹服务，请稍候", Toast.LENGTH_SHORT).show();
                stopTrace();
            }
        });

        btnOperator.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                geoFence = new Geofence(getActivity(), mInflater);
                if (geoFence.popupwindow != null && geoFence.popupwindow.isShowing()) {
                    geoFence.popupwindow.dismiss();
                    return;
                } else {
                    geoFence.initPopupWindowView();
                    geoFence.popupwindow.showAsDropDown(v, 0, 5);
                }
            }
        });

    }

    private void initListener() {
        initOnStartTraceListener();
        initOnStopTraceListener();
    }

    private void startTrace() {
        TraceActivity.client.startTrace(TraceActivity.trace, startTraceListener);
    }

    private void stopTrace() {
        TraceActivity.client.stopTrace(TraceActivity.trace, stopTraceListener);
    }

    private void setInterval() {
        gatherInterval = 5;
        int packInterval = 30;
        TraceActivity.client.setInterval(gatherInterval, packInterval);
    }

    private void setRequestType() {
        int type = 0;
        TraceActivity.client.setProtocolType(type);
    }

    private void queryEntityList() {
        String entityNames = "357512057411038";
        String columnKey = "";
        int returnType = 0;
        int activeTime = (int) (System.currentTimeMillis() / 1000 - 30);

        int pageSize = 10;

        int pageIndex = 1;

        OnEntityListener entityListener = new OnEntityListener() {
            @Override
            public void onRequestFailedCallback(String arg0) {
                System.out.println("entity请求失败回调接口消息 : " + arg0);
            }

            @Override
            public void onAddEntityCallback(String var1) {
                Log.e("ac", "Add call back");
            }

            @Override
            public void onUpdateEntityCallback(String var1) {
                Log.e("ac", "update call back");
            }

            @Override
            public void onReceiveLocation(TraceLocation var1) {
                Log.e("ac", "receive location");
            }

            @Override
            public void onQueryEntityListCallback(String arg0) {
                System.out.println("entity回调接口消息 : " + arg0);
                System.out.println("entity message " + arg0 == "");

                Log.e("ac", "query success");

                RealtimeTrackData realtimeTrackData = GsonService.parseJson(arg0,
                        RealtimeTrackData.class);

                List<LatLng> latLngList = new ArrayList<LatLng>();
                if (realtimeTrackData != null && realtimeTrackData.getStatus() == 0) {
                    if (realtimeTrackData.getRealtimePoint() != null) {
                        latLngList.add(realtimeTrackData.getRealtimePoint());
                    }

                    drawRealtimePoint(realtimeTrackData.getRealtimePoint());

                }
            }
        };

        int protocoType = 1;
        TraceActivity.client.setProtocolType(protocoType);

        TraceActivity.client.queryEntityList(107606, entityNames, columnKey, returnType, activeTime,
                pageSize,
                pageIndex, /*TraceActivity.*/entityListener);
    }

    private void queryRealtimeTrack() {
        queryEntityList();
    }

    private void initOnStartTraceListener() {
        startTraceListener = new OnStartTraceListener() {
            public void onTraceCallback(int arg0, String arg1) {
                showMessage("开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]", Integer.valueOf(arg0));
                if (0 == arg0 || 10006 == arg0) {
                    isTraceStart = true;
                    startRefreshThread(true);
                }
            }

            public void onTracePushCallback(byte arg0, String arg1) {
                showMessage("轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]", null);
            }

        };
    }

    private void initOnStopTraceListener() {
        stopTraceListener = new OnStopTraceListener() {

            public void onStopTraceSuccess() {
                showMessage("停止轨迹服务成功", Integer.valueOf(1));
                isTraceStart = false;
                startRefreshThread(false);
            }

            public void onStopTraceFailed(int arg0, String arg1) {
                showMessage("停止轨迹服务接口消息 [错误编码 : " + arg0 + "，消息内容 : " + arg1 + "]", null);
            }
        };
    }

    protected class RefreshThread extends Thread {

        protected boolean refresh = true;

        @Override
        public void run() {
            while (refresh) {
                queryRealtimeTrack();
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    System.out.println("线程休眠失败");
                }
            }

        }
    }

    protected void showRealtimeTrack(TraceLocation location) {

        if (null == refreshThread || !refreshThread.refresh) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            showMessage("当前查询无轨迹点", null);

        } else {

            LatLng latLng = new LatLng(latitude, longitude);
            
            pointList.add(latLng);

            if (isInUploadFragment) {
                drawRealtimePoint(latLng);
            }

        }

    }

    private void drawRealtimePoint(LatLng point) {

        TraceActivity.mBaiduMap.clear();

        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(18).build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        realtimeBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);

        overlay = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);

        if (pointList.size() >= 2 && pointList.size() <= 10000) {
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }

        addMarker();

    }

    protected static void addMarker() {

        if (null != msUpdate) {
            TraceActivity.mBaiduMap.setMapStatus(msUpdate);
        }

        if (null != polyline) {
            TraceActivity.mBaiduMap.addOverlay(polyline);
        }

        if (null != Geofence.fenceOverlay) {
            TraceActivity.mBaiduMap.addOverlay(Geofence.fenceOverlay);
        }

        if (null != overlay) {
            TraceActivity.mBaiduMap.addOverlay(overlay);
        }
    }

    protected void startRefreshThread(boolean isStart) {
        if (null == refreshThread) {
            refreshThread = new RefreshThread();
        }
        refreshThread.refresh = isStart;
        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }
    }

    private void showMessage(final String message, final Integer errorNo) {

        new Handler(TraceActivity.mContext.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(TraceActivity.mContext, message, Toast.LENGTH_LONG).show();

                if (null != errorNo) {
                    if (0 == errorNo.intValue() || 10006 == errorNo.intValue() || 10008 == errorNo.intValue()
                            || 10009 == errorNo.intValue()) {
                        btnStartTrace.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
                        btnStartTrace.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
                    } else if (1 == errorNo.intValue() || 10004 == errorNo.intValue()) {
                        btnStartTrace.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
                        btnStartTrace.setTextColor(Color.rgb(0x00, 0x00, 0x00));
                    }
                }
            }
        });

    }
}
