package com.example.shiyu.client;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by shiyu on 15/11/20.
 */
public class TextActivity extends Thread {
    public EditText editTextAddress;
    private Button connectButton;
    TextView textResponse;
    ImageButton buttonLeftFront, buttonFront, buttonRightFront, buttonRightBack, buttonBack, buttonLeftBack, buttonStop;
    MainActivity main;
    public DirectionButtons directionButtons;

    public TextActivity(MainActivity main) {
        this.main = main;
    }

    @Override
    public void run() {
        buttonLeftFront = (ImageButton) main.findViewById(R.id.buttonLeftFront);
        buttonFront = (ImageButton) main.findViewById(R.id.buttonFront);
        buttonRightFront = (ImageButton) main.findViewById(R.id.buttonRightFront);
        buttonRightBack = (ImageButton) main.findViewById(R.id.buttonRightBack);
        buttonBack = (ImageButton) main.findViewById(R.id.buttonBack);
        buttonLeftBack = (ImageButton) main.findViewById(R.id.buttonLeftBack);
        buttonStop = (ImageButton) main.findViewById(R.id.buttonStop);

        buttonLeftFront.setVisibility(View.GONE);
        buttonFront.setVisibility(View.GONE);
        buttonRightFront.setVisibility(View.GONE);
        buttonRightBack.setVisibility(View.GONE);
        buttonBack.setVisibility(View.GONE);
        buttonLeftBack.setVisibility(View.GONE);
        buttonStop.setVisibility(View.GONE);

        textResponse = (TextView)main.findViewById(R.id.response);

        editTextAddress = (EditText) main.findViewById(R.id.address);
        editTextAddress.setVisibility(View.VISIBLE);

        connectButton = (Button) main.findViewById(R.id.connect);

        connectButton.setOnClickListener(buttonConnectOnClickListener);
    }


    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    main.serverIP = editTextAddress.getText().toString();
                    ConnectClientTask myClientTask = new ConnectClientTask(
                            main.serverIP,
                            main.SERVER_PORT,
                            textResponse);
                    myClientTask.execute();

                    editTextAddress.setVisibility(View.GONE);

                    directionButtons = new DirectionButtons(main);
                    directionButtons.start();

                    editTextAddress.setVisibility(View.GONE);

                    connectButton.setVisibility(View.GONE);

                    buttonLeftFront.setVisibility(View.VISIBLE);
                    buttonFront.setVisibility(View.VISIBLE);
                    buttonRightFront.setVisibility(View.VISIBLE);
                    buttonRightBack.setVisibility(View.VISIBLE);
                    buttonBack.setVisibility(View.VISIBLE);
                    buttonLeftBack.setVisibility(View.VISIBLE);
                    buttonStop.setVisibility(View.VISIBLE);
                }};
}
