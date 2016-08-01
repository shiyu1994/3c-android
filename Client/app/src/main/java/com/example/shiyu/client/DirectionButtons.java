package com.example.shiyu.client;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View.OnClickListener;

/**
 * Created by shiyu on 15/11/20.
 */
public class DirectionButtons extends Thread{
    EditText editTextAddress;
    public Button buttonConnect;
    String serverIP;
    public ImageButton buttonLeftFront, buttonFront, buttonRightFront, buttonRightBack, buttonBack, buttonLeftBack, buttonStop;
    MainActivity main;

    public DirectionButtons(MainActivity main) {
        this.main = main;
        serverIP = main.serverIP;
    }


    public void initUI() {
        editTextAddress = (EditText)main.findViewById(R.id.address);
        buttonConnect = (Button)main.findViewById(R.id.connect);

        buttonLeftFront = (ImageButton) main.findViewById(R.id.buttonLeftFront);
        buttonFront = (ImageButton) main.findViewById(R.id.buttonFront);
        buttonRightFront = (ImageButton) main.findViewById(R.id.buttonRightFront);
        buttonRightBack = (ImageButton) main.findViewById(R.id.buttonRightBack);
        buttonBack = (ImageButton) main.findViewById(R.id.buttonBack);
        buttonLeftBack = (ImageButton) main.findViewById(R.id.buttonLeftBack);
        buttonStop = (ImageButton) main.findViewById(R.id.buttonStop);

        UIOptimization.setButtonFocusChanged(buttonLeftFront);
        UIOptimization.setButtonFocusChanged(buttonFront);
        UIOptimization.setButtonFocusChanged(buttonRightFront);
        UIOptimization.setButtonFocusChanged(buttonRightBack);
        UIOptimization.setButtonFocusChanged(buttonBack);
        UIOptimization.setButtonFocusChanged(buttonLeftBack);
        UIOptimization.setButtonFocusChanged(buttonStop);

        buttonLeftFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "03\n");
            }
        });

        buttonFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "33\n");
            }
        });

        buttonRightFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "30\n");
            }
        });

        buttonRightBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "85\n");
            }
        });

        buttonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "88\n");
            }
        });

        buttonLeftBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "58\n");
            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ActivityDemo", "Stop Click");
                main.blueTooth.sendInformation(main.CarSocket, "00\n");
            }
        });

        editTextAddress.setVisibility(View.GONE);

        buttonConnect.setVisibility(View.GONE);

        buttonLeftFront.setVisibility(View.VISIBLE);
        buttonFront.setVisibility(View.VISIBLE);
        buttonRightFront.setVisibility(View.VISIBLE);
        buttonRightBack.setVisibility(View.VISIBLE);
        buttonBack.setVisibility(View.VISIBLE);
        buttonLeftBack.setVisibility(View.VISIBLE);
        buttonStop.setVisibility(View.VISIBLE);
    }

    @Override
    public void run() {
        //initUI();
        editTextAddress = (EditText)main.findViewById(R.id.address);
        buttonConnect = (Button)main.findViewById(R.id.connect);

        buttonLeftFront = (ImageButton) main.findViewById(R.id.buttonLeftFront);
        buttonFront = (ImageButton) main.findViewById(R.id.buttonFront);
        buttonRightFront = (ImageButton) main.findViewById(R.id.buttonRightFront);
        buttonRightBack = (ImageButton) main.findViewById(R.id.buttonRightBack);
        buttonBack = (ImageButton) main.findViewById(R.id.buttonBack);
        buttonLeftBack = (ImageButton) main.findViewById(R.id.buttonLeftBack);
        buttonStop = (ImageButton) main.findViewById(R.id.buttonStop);

        UIOptimization.setButtonFocusChanged(buttonLeftFront);
        UIOptimization.setButtonFocusChanged(buttonFront);
        UIOptimization.setButtonFocusChanged(buttonRightFront);
        UIOptimization.setButtonFocusChanged(buttonRightBack);
        UIOptimization.setButtonFocusChanged(buttonBack);
        UIOptimization.setButtonFocusChanged(buttonLeftBack);
        UIOptimization.setButtonFocusChanged(buttonStop);

        buttonLeftFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "03\n");
            }
        });

        buttonFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "33\n");
            }
        });

        buttonRightFront.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "30\n");
            }
        });

        buttonRightBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "85\n");
            }
        });

        buttonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "88\n");
            }
        });

        buttonLeftBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                main.blueTooth.sendInformation(main.CarSocket, "58\n");
            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ActivityDemo", "Stop Click");
                main.blueTooth.sendInformation(main.CarSocket, "00\n");
            }
        });
    }
}
