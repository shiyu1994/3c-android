package com.example.shiyu.server;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.OnGeoFenceListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("NewApi")
public class Geofence implements OnClickListener {

    protected PopupWindow popupwindow = null;

    private Button btnSetfence = null;
    private Button btnMonitoredstatus = null;
    private Button btnHistoryalarm = null;
    private Button btnDelayalarm = null;

    private LayoutInflater mInflater = null;

    private double latitude = 0;

    private double longitude = 0;

    protected static int radius = 100;

    protected static int radiusTemp = radius;

    protected static int fenceId = 0;

    private int delayTime = 5;

    protected static OnGeoFenceListener geoFenceListener = null;

    protected static OverlayOptions fenceOverlay = null;

    protected static OverlayOptions fenceOverlayTemp = null;

    private Context mContext = null;

    protected OnMapClickListener mapClickListener = new OnMapClickListener() {

        public void onMapClick(LatLng arg0) {
            MainActivity.mBaiduMap.clear();
            latitude = arg0.latitude;
            longitude = arg0.longitude;

            MapStatus mMapStatus = new MapStatus.Builder().target(arg0).zoom(18).build();
            TrackUploadFragment.msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

            fenceOverlayTemp = fenceOverlay;
            fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(arg0)
                    .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                    .radius(radius);

            TrackUploadFragment.addMarker();
            createOrUpdateDialog();
        }

        public boolean onMapPoiClick(MapPoi arg0) {
            return false;
        }
    };

    public Geofence(Context context, LayoutInflater inflater) {
        initOnGeoFenceListener();
        mContext = context;
        mInflater = inflater;
        if (null == fenceOverlay) {
            queryFenceList();
        }
    }

    protected static void addEntity() {
        String entityName = MainActivity.entityName;
        String columnKey = "";
        MainActivity.client.addEntity(MainActivity.serviceId, entityName, columnKey, MainActivity.entityListener);
    }

    private void createFence() {

        String creator = MainActivity.entityName;

        String fenceName = MainActivity.entityName + "_fence";

        String fenceDesc = "test";

        String monitoredPersons = MainActivity.entityName;

        String observers = MainActivity.entityName;

        String validTimes = "0800,2300";

        int validCycle = 4;

        String validDate = "";

        String validDays = "";

        int coordType = 3;

        String center = longitude + "," + latitude;

        double radius = Geofence.radius;

        int alarmCondition = 3;

        MainActivity.client.createCircularFence(MainActivity.serviceId, creator, fenceName, fenceDesc,
                monitoredPersons, observers,
                validTimes, validCycle, validDate, validDays, coordType, center, radius, alarmCondition,
                geoFenceListener);

    }


    private void updateFence() {

        String fenceName = MainActivity.entityName + "_fence";

        int fenceId = Geofence.fenceId;

        String fenceDesc = "test fence";

        String monitoredPersons = MainActivity.entityName;

        String observers = MainActivity.entityName;

        String validTimes = "0800,2300";

        int validCycle = 4;

        String validDate = "";

        String validDays = "";

        int coordType = 3;

        String center = longitude + "," + latitude;

        double radius = Geofence.radius;

        int alarmCondition = 3;

        MainActivity.client.updateCircularFence(MainActivity.serviceId, fenceName, fenceId, fenceDesc,
                monitoredPersons,
                observers, validTimes, validCycle, validDate, validDays, coordType, center, radius, alarmCondition,
                geoFenceListener);
    }

    private void queryFenceList() {

        String creator = MainActivity.entityName;
        String fenceIds = "";
        MainActivity.client.queryFenceList(MainActivity.serviceId, creator, fenceIds, geoFenceListener);
    }

    private void monitoredStatus() {
        int fenceId = Geofence.fenceId;
        String monitoredPersons = MainActivity.entityName;
        MainActivity.client.queryMonitoredStatus(MainActivity.serviceId, fenceId, monitoredPersons,
                geoFenceListener);
    }

    private void historyAlarm() {
        int fenceId = Geofence.fenceId;
        String monitoredPersons = MainActivity.entityName;
        int beginTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        int endTime = (int) (System.currentTimeMillis() / 1000);

        MainActivity.client.queryFenceHistoryAlarmInfo(MainActivity.serviceId, fenceId, monitoredPersons, beginTime,
                endTime,
                geoFenceListener);
    }

    private void delayAlarm() {
        int fenceId = Geofence.fenceId;
        String observer = MainActivity.entityName;
        int delayTime = (int) (System.currentTimeMillis() / 1000 + this.delayTime * 60);
        MainActivity.client.delayFenceAlarm(MainActivity.serviceId, fenceId, observer, delayTime,
                geoFenceListener);
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_setfence:
                inputDialog();
                MainActivity.mBaiduMap.setOnMapClickListener(mapClickListener);
                popupwindow.dismiss();
                break;

            case R.id.btn_historyalarm:
                historyAlarm();
                popupwindow.dismiss();
                break;

            case R.id.btn_monitoredstatus:
                monitoredStatus();
                popupwindow.dismiss();
                break;

            case R.id.btn_delayalarm:
                delayAlarm();
                popupwindow.dismiss();
                break;

            default:
                break;
        }

    }

    @SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
    protected void initPopupWindowView() {

        View customView = mInflater.inflate(R.layout.menu_geofence, null);
        popupwindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        customView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (popupwindow != null && popupwindow.isShowing()) {
                    popupwindow.dismiss();
                    popupwindow = null;
                }

                return false;
            }

        });

        btnSetfence = (Button) customView.findViewById(R.id.btn_setfence);
        btnMonitoredstatus = (Button) customView.findViewById(R.id.btn_monitoredstatus);
        btnHistoryalarm = (Button) customView.findViewById(R.id.btn_historyalarm);
        btnDelayalarm = (Button) customView.findViewById(R.id.btn_delayalarm);

        btnSetfence.setOnClickListener(this);
        btnMonitoredstatus.setOnClickListener(this);
        btnHistoryalarm.setOnClickListener(this);
        btnDelayalarm.setOnClickListener(this);

    }

    private void initOnGeoFenceListener() {
        geoFenceListener = new OnGeoFenceListener() {

            @Override
            public void onRequestFailedCallback(String arg0) {
                MainActivity.mBaiduMap.clear();
                if (null != fenceOverlayTemp) {
                    fenceOverlay = fenceOverlayTemp;
                    fenceOverlayTemp = null;
                }
                radius = radiusTemp;
                TrackUploadFragment.addMarker();
                showMessage("geoFence请求失败回调接口消息 : " + arg0);
            }

            @Override
            public void onCreateCircularFenceCallback(String arg0) {

                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        fenceId = dataJson.getInt("fence_id");
                        fenceOverlayTemp = null;
                        showMessage("围栏创建成功");
                    } else {
                        MainActivity.mBaiduMap.clear();
                        fenceOverlay = fenceOverlayTemp;
                        fenceOverlayTemp = null;
                        radius = radiusTemp;
                        TrackUploadFragment.addMarker();
                        showMessage("创建圆形围栏回调接口消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    showMessage("解析创建围栏回调消息失败");
                }

            }

            @Override
            public void onUpdateCircularFenceCallback(String arg0) {
                showMessage("更新圆形围栏回调接口消息 : " + arg0);
            }

            @Override
            public void onDelayAlarmCallback(String arg0) {
                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        showMessage(delayTime + "分钟内不再报警");
                    } else {
                        showMessage("延迟报警回调接口消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    showMessage("解析延迟报警回调消息失败");
                }
            }

            @Override
            public void onDeleteFenceCallback(String arg0) {
                showMessage(" 删除围栏回调接口消息 : " + arg0);
            }

            @Override
            public void onQueryFenceListCallback(String arg0) {
                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        if (dataJson.has("size")) {
                            JSONArray jsonArray = dataJson.getJSONArray("fences");
                            JSONObject jsonObj = jsonArray.getJSONObject(0);
                            fenceId = jsonObj.getInt("fence_id");
                            JSONObject center = jsonObj.getJSONObject("center");

                            latitude = center.getDouble("latitude");
                            longitude = center.getDouble("longitude");
                            radius = (int) (jsonObj.getDouble("radius"));

                            LatLng latLng = new LatLng(latitude, longitude);

                            MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                            TrackUploadFragment.msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                            fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(latLng)
                                    .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                                    .radius(radius);

                            TrackUploadFragment.addMarker();
                        }
                    }
                } catch (JSONException e) {
                    System.out.println("解析围栏列表回调消息失败");
                }

            }

            @Override
            public void onQueryHistoryAlarmCallback(String arg0) {
                showMessage(" 查询历史报警回调接口消息 : " + arg0);
            }

            @Override
            public void onQueryMonitoredStatusCallback(String arg0) {
                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        int size = dataJson.getInt("size");
                        if (size >= 1) {
                            JSONArray jsonArray = dataJson.getJSONArray("monitored_person_statuses");
                            JSONObject jsonObj = jsonArray.getJSONObject(0);
                            String mPerson = jsonObj.getString("monitored_person");
                            int mStatus = jsonObj.getInt("monitored_status");
                            if (1 == mStatus) {
                                showMessage("监控对象[ " + mPerson + " ]在围栏内");
                            } else {
                                showMessage("监控对象[ " + mPerson + " ]在围栏外");
                            }
                        }
                    } else {
                        showMessage("查询监控对象状态回调消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    showMessage("解析查询监控对象状态回调消息失败");
                }
            }
        };
    }

    private void inputDialog() {

        final EditText circleRadius = new EditText(mContext);
        circleRadius.setFocusable(true);
        circleRadius.setText(radius + "");
        circleRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("围栏半径(单位:米)").setView(circleRadius)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.mBaiduMap.setOnMapClickListener(null);
                    }

                });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String radiusStr = circleRadius.getText().toString();
                if (!TextUtils.isEmpty(radiusStr)) {
                    radiusTemp = radius;
                    radius = Integer.parseInt(radiusStr) > 0 ? Integer.parseInt(radiusStr) : radius;
                }
                Toast.makeText(mContext, "请点击地图标记围栏圆心", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    private void createOrUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("确定设置围栏?");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                MainActivity.mBaiduMap.clear();
                if (null != fenceOverlayTemp) {
                    fenceOverlay = fenceOverlayTemp;
                }
                radius = radiusTemp;
                TrackUploadFragment.addMarker();
                MainActivity.mBaiduMap.setOnMapClickListener(null);
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (0 == fenceId) {
                    createFence();
                } else {
                    updateFence();
                }
                MainActivity.mBaiduMap.setOnMapClickListener(null);
            }
        });
        builder.show();
    }

    private void showMessage(String message) {
        Looper.prepare();
        Toast.makeText(MainActivity.mContext, message, Toast.LENGTH_LONG).show();
        Looper.loop();
    }

}
