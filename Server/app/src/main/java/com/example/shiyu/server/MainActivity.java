package com.example.shiyu.server;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;

public class MainActivity extends android.support.v4.app.FragmentActivity implements View.OnClickListener {

    public BluetoothSocket CarSocket;
    public String clientIP;
    public BlueTooth blueTooth;
    public VideoThread videoThread = null;
    public SimSimi simSimi;
    public VoiceGenerator voiceGenerator;
    public SocketServerThread serverThread;

    private Button faceRecognitionOn;

    public boolean isFacing = false;

    protected static Context mContext = null;

    TextView BluetoothText, infoip, msg;

    protected static Trace trace = null;

    protected static String entityName = null;

    protected static long serviceId = 107606; // serviceId为开发者创建的鹰眼服务ID

    private int traceType = 2;

    protected static LBSTraceClient client = null;

    protected static OnEntityListener entityListener = null;

    private Button btnTrackUpload;
    private Button btnTrackQuery;

    protected static MapView bmapView = null;
    protected static BaiduMap mBaiduMap = null;

    private FragmentManager fragmentManager;

    private TrackUploadFragment mTrackUploadFragment;

    private Button traceButton;
    private Button displayButton;

    private View.OnClickListener onClickListener1;
    private View.OnClickListener onClickListener2;

    private SurfaceView surfaceView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        faceRecognitionOn = (Button)findViewById(R.id.face);

        faceRecognitionOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFacing) {
                    faceRecognitionOn.setText("Face On");
                    isFacing = true;
                }
                else {
                    faceRecognitionOn.setText("Face off");
                    isFacing = false;
                }
            }
        });

        traceButton = (Button)findViewById(R.id.traceButton);

        traceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefaultFragment();
            }
        });

        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);

        voiceGenerator = new VoiceGenerator(this);
        voiceGenerator.start();

        videoThread = new VideoThread(this);
        videoThread.start();

        serverThread = new SocketServerThread(this);
        serverThread.start();

        surfaceView = null;

        Thread socketServerThread = new Thread(new SocketServerThread(this));
        socketServerThread.start();


        displayButton = (Button)findViewById(R.id.display);

        displayButton.setOnClickListener(onClickListener1);

        simSimi = new SimSimi(this);
        simSimi.start();

        BluetoothText = (TextView)findViewById(R.id.info);

        infoip.setText(getIpAddress());

        blueTooth = new BlueTooth(this);
        blueTooth.run();

        mContext = getApplicationContext();

        client = new LBSTraceClient(mContext);

        entityName = getImei(mContext);

        trace = new Trace(getApplicationContext(), serviceId, entityName,
                traceType);

        initComponent();

        initOnEntityListener();

        addEntity();

        setDefaultFragment();
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    private void addEntity() {
        Geofence.addEntity();
    }

    private void initComponent() {
        // 初始化控件
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
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}



