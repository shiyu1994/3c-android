package com.example.shiyu.client;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.OnTrackListener;
import com.baidu.trackutils.DateDialog;
import com.baidu.trackutils.DateDialog.CallBack;
import com.baidu.trackutils.DateDialog.PriorityListener;
import com.baidu.trackutils.DateUtils;
import com.baidu.trackutils.GsonService;
import com.baidu.trackutils.HistoryTrackData;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class TrackQueryFragment extends Fragment implements OnClickListener {

    private Button btnDate = null;

    private Button btnProcessed = null;

    private int startTime = 0;
    private int endTime = 0;

    private int year = 0;
    private int month = 0;
    private int day = 0;

    private static BitmapDescriptor bmStart;
    private static BitmapDescriptor bmEnd;

    private static MarkerOptions startMarker = null;
    private static MarkerOptions endMarker = null;
    private static PolylineOptions polyline = null;

    protected static OnTrackListener trackListener = null;

    private MapStatusUpdate msUpdate = null;

    private TextView tvDatetime = null;

    private static int isProcessed = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trackquery,
                container, false);

        init(view);

        initOnTrackListener();

        return view;
    }

    private void init(final View view) {

        btnDate = (Button) view.findViewById(R.id.btn_date);

        btnDate.setOnClickListener(this);

        btnProcessed = (Button) view.findViewById(R.id.btn_isprocessed);

        btnProcessed.setOnClickListener(this);

        tvDatetime = (TextView) view.findViewById(R.id.tv_datetime);
        tvDatetime.setText(" 当前日期 : " + DateUtils.getCurrentDate() + " ");

    }

    private void queryHistoryTrack() {

        String entityName = "357512057411038";
        Log.e("heistory entity", TraceActivity.entityName);
        int simpleReturn = 0;
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        int pageSize = 1000;
        int pageIndex = 1;

        TraceActivity.client.queryHistoryTrack(107606, entityName, simpleReturn, startTime, endTime,
                pageSize,
                pageIndex,
                trackListener);
    }

    private void queryProcessedHistoryTrack() {

        String entityName = TraceActivity.entityName;

        int simpleReturn = 0;

        int isProcessed = 1;

        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }

        int pageSize = 1000;

        int pageIndex = 1;

        TraceActivity.client.queryProcessedHistoryTrack(TraceActivity.serviceId, entityName, simpleReturn, isProcessed,
                startTime, endTime,
                pageSize,
                pageIndex,
                trackListener);
    }


    private void queryTrack() {

        int[] date = null;
        DisplayMetrics dm = new DisplayMetrics();
        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (year == 0 && month == 0 && day == 0) {
            String curDate = DateUtils.getCurrentDate();
            date = DateUtils.getYMDArray(curDate, "-");
        }

        if (date != null) {
            year = date[0];
            month = date[1];
            day = date[2];
        }

        DateDialog dateDiolog = new DateDialog(this.getActivity(), new PriorityListener() {

            public void refreshPriorityUI(String sltYear, String sltMonth,
                    String sltDay, CallBack back) {

                Log.d("TGA", sltYear + sltMonth + sltDay);
                year = Integer.parseInt(sltYear);
                month = Integer.parseInt(sltMonth);
                day = Integer.parseInt(sltDay);
                String st = year + "年" + month + "月" + day + "日0时0分0秒";
                String et = year + "年" + month + "月" + day + "日23时59分59秒";

                startTime = Integer.parseInt(DateUtils.getTimeToStamp(st));
                endTime = Integer.parseInt(DateUtils.getTimeToStamp(et));

                back.execute();
            }

        }, new CallBack() {

            public void execute() {

                tvDatetime.setText(" 当前日期 : " + year + "-" + month + "-" + day + " ");

                if (0 == isProcessed) {
                    Toast.makeText(getActivity(), "正在查询历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
                    queryHistoryTrack();
                } else {
                    Toast.makeText(getActivity(), "正在查询纠偏后的历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
                    queryProcessedHistoryTrack();
                }
            }
        }, year, month, day, width, height, "选择日期", 1);

        Window window = dateDiolog.getWindow();
        window.setGravity(Gravity.CENTER);
        dateDiolog.setCancelable(true);
        dateDiolog.show();

    }


    private void showHistoryTrack(String historyTrack) {

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack,
                HistoryTrackData.class);

        List<LatLng> latLngList = new ArrayList<LatLng>();
        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }

            drawHistoryTrack(latLngList, historyTrackData.distance);

        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date:
                queryTrack();
                break;

            case R.id.btn_isprocessed:
                isProcessed = isProcessed ^ 1;
                if (0 == isProcessed) {
                    btnProcessed.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
                    btnProcessed.setTextColor(Color.rgb(0x00, 0x00, 0x00));
                    Toast.makeText(getActivity(), "正在查询历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
                    queryHistoryTrack();
                } else {
                    btnProcessed.setBackgroundColor(Color.rgb(0x99, 0xcc, 0xff));
                    btnProcessed.setTextColor(Color.rgb(0x00, 0x00, 0xd8));
                    Toast.makeText(getActivity(), "正在查询纠偏后的历史轨迹，请稍候", Toast.LENGTH_SHORT).show();
                    queryProcessedHistoryTrack();
                }
                break;

            default:
                break;
        }
    }

    private void initOnTrackListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onRequestFailedCallback(String arg0) {
                Looper.prepare();
                Toast.makeText(getActivity(), "track请求失败回调接口消息 : " + arg0, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryHistoryTrackCallback(String arg0) {
                super.onQueryHistoryTrackCallback(arg0);
                showHistoryTrack(arg0);
            }
        };
    }

    private void drawHistoryTrack(final List<LatLng> points, final double distance) {
        TraceActivity.mBaiduMap.clear();

        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(getActivity(), "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
            Looper.loop();
            resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);

            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_start);
            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);

            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(points);

            addMarker();

            Looper.prepare();
            Toast.makeText(getActivity(), "当前轨迹里程为 : " + (int) distance + "米", Toast.LENGTH_SHORT).show();
            Looper.loop();

        }

    }

    protected void addMarker() {

        if (null != msUpdate) {
            TraceActivity.mBaiduMap.setMapStatus(msUpdate);
        }

        if (null != startMarker) {
            TraceActivity.mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            TraceActivity.mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            TraceActivity.mBaiduMap.addOverlay(polyline);
        }

    }

    private void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

}
