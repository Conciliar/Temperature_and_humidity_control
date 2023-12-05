package com.example.temperatureandhumiditycontrol;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;

//This class will try to connect to a socket
public class ConnectThread extends Thread {
    private final BluetoothSocket socket;
    private static final String TAG = "MyLogs";
    public static Handler handler;
    private final static int ERROR = 0;

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID newUUID, Handler handler) {
        BluetoothSocket temp = null; //This is a temporary socket because mmSocket is final
        this.handler=handler;

        try {
            Log.d(TAG, "Creating socket.");
            temp = device.createRfcommSocketToServiceRecord(newUUID);
        }

        catch (IOException e) {
            Log.e(TAG, "Failed to create socket", e);
        }

        socket = temp;
    }

    @SuppressLint("MissingPermission")
    public void run() {

        try {
            Log.d(TAG, "Connecting to socket");
            socket.connect();
        }

        catch (IOException connectException) {
            handler.obtainMessage(ERROR, "Unable to connect to the bluetooth device").sendToTarget();
            Log.e(TAG, "connect exception: " + connectException);

            try {
                socket.close();
                Log.d(TAG, "Closed socket");
            }

            catch (IOException closeException) {
                Log.e(TAG, "Failed to close the client socket", closeException);
            }

            return;
        }
    }

    //Call this method to close the socket
    public void closeSocket() {
        try {
            socket.close();
            Log.d(TAG, "Closed socket");
        }

        catch (IOException e) {
            Log.e(TAG, "Failed to close socket", e);
        }
    }

    public BluetoothSocket getSocket(){
        return socket;
    }
}
