package com.example.shiyu.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by shiyu on 15/12/13.
 */
public class BlueTooth extends Thread {

    private BluetoothAdapter adapter;
    private BluetoothSocket CarSocket;
    private boolean CarSocketSuccess;
    private TextView bluetoothText;
    private LinearLayout bluetoothList;
    private MainActivity main;
    private UUID uuid;
    private Button goButton;
    private Button refreshButton;

    public BlueTooth(MainActivity main) {
        this.main = main;

        bluetoothList = (LinearLayout)main.findViewById(R.id.BluetoothList);
        bluetoothText = (TextView) main.findViewById(R.id.info);

        goButton = (Button)main.findViewById(R.id.go);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CarSocketSuccess) sendInformation(CarSocket,"33\n");
            }
        });

        refreshButton = (Button)main.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBluetoothDevice();
            }
        });
    }

    @Override
    public void run() {

    }

    public void refreshBluetoothDevice() {
        if (adapter==null) adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {

            if (!adapter.isEnabled()) {
                bluetoothText.setText(" 请打开本机的蓝牙设备 ");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                main.startActivity(enableBtIntent);
                return;
            }

            bluetoothList.removeAllViews();
            bluetoothText.setText(" 正在搜索周围的蓝牙设备");
            for (BluetoothDevice device : adapter.getBondedDevices()) {
                Button temp = new Button(main);
                temp.setText(device.getName());
                temp.setTextColor(Color.BLACK);
                temp.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                bluetoothList.addView(temp);
                temp.setOnClickListener(new BluetoothDeviceListener(device));
            }

        } else {
            bluetoothText.setText(" 未能找到蓝牙设备！");
        }
    }

     private  class BluetoothDeviceListener implements View.OnClickListener {
        BluetoothDevice device;

        BluetoothDeviceListener(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void onClick(View arg0) {
            try {
                if (CarSocket != null)
                    CarSocket.close();
                CarSocketSuccess=false;
                uuid = device.getUuids()[0].getUuid();
                CarSocket = device.createRfcommSocketToServiceRecord(uuid);
                adapter.cancelDiscovery();
                CarSocket.connect();
                main.CarSocket = CarSocket;
                CarSocketSuccess=true;
                bluetoothText.setText(" 连接成功：" + device.getName());
                bluetoothList.removeAllViews();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothText.setText(" 连接"+device.getName()+"失败");
        }
    }



    public void sendInformation(BluetoothSocket socket,String information) {
        if (socket == null) {
            return;
        }
        try {
            socket.getOutputStream().write(String.valueOf(information).getBytes());
            socket.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
