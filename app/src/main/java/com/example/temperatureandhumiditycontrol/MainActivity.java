package com.example.temperatureandhumiditycontrol;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MylLogs";
    public static Handler handler;
    private final static int ERROR = 0;
    BluetoothDevice HC06 = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        Button connect = findViewById(R.id.connectToDevice);
        Button clearValues = findViewById(R.id.clear);
        TextView readings = findViewById(R.id.bluetoothReadings);
        Log.d(TAG, "Beginning process");

        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readings.setText("");
            }
        });

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {

                    case ERROR:
                        String arduinoMessage = message.obj.toString(); // Read message from Arduino
                        readings.setText(arduinoMessage);
                        break;
                }
            }
        };

        connect.setOnClickListener(new View.OnClickListener() {

            ActivityResultLauncher<Intent> newActivity = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {

                        }
                    });

            @Override
            public void onClick(View view) {

                //If this is null, then our device does not support bluetooth
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                }

                else {
                    Log.d(TAG, "Device supports Bluetooth");

                    //If bluetooth is disabled, we ask the user to enable it
                    if (!bluetoothAdapter.isEnabled()){
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            newActivity.launch(enableBluetoothIntent);
                        }
                        else {
                            newActivity.launch(enableBluetoothIntent);
                        }
                    }

                    //Get a list of the paired devices
                    Set <BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    //If there are any, we loop through all of them to find HC-06
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();

                            //This means we found HC-06
                            if (deviceName.equals("HC-06")) {
                                arduinoUUID = device.getUuids()[0].getUuid();
                                HC06 = device;
                                readings.setText("");

                                if (HC06 != null) {
                                    //Attempting to connect to HC-06
                                    Log.d(TAG, "Calling ConnectThread class");

                                    //Creating a new ConnectThread object
                                    ConnectThread connectThread = new ConnectThread(HC06, arduinoUUID, handler);
                                    connectThread.run();

                                    if (connectThread.getSocket().isConnected()) {
                                        Log.d(TAG, "Calling ConnectedThread class");
                                        ConnectedThread connectedThread = new ConnectedThread(connectThread.getSocket());
                                        Log.d(TAG, "Created thread");
                                        Log.d(TAG, "Running thread");
                                        connectedThread.run();

                                        //Check if Socket is connected
                                        if (connectedThread.getValue() != null) {
                                            readings.setText(connectedThread.getValue());
                                            connectedThread.closeSocket();   //Make sure to close the socket after we're done
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }}