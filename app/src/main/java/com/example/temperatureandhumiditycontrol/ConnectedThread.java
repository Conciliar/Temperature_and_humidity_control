package com.example.temperatureandhumiditycontrol;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

//Once a socket has connected, this class will attempt to read data from the bluetooth module
public class ConnectedThread extends Thread {
    private static final String TAG = "MyLogs";
    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private String value;

    public ConnectedThread(BluetoothSocket socket) {
        Log.d(TAG, "Creating socket");
        this.socket = socket;
        InputStream temp = null;   //This is a temporary socket because socket variable is final

        try {
            Log.d(TAG, "Getting input stream");
            temp = socket.getInputStream();
        }

        catch (IOException e) {
            Log.e(TAG, "Failed to get input stream", e);
        }

        inputStream = temp;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while (true) {
            try {
                buffer[bytes] = (byte) inputStream.read();
                String readMessage;
                Log.d(TAG, "Reading data");
                readMessage = new String(buffer, 0, bytes);

                //If we detect a new line it means we read a full measurement
                if (Pattern.matches("%\r\nTemperature: ([0-9]*\\.[0-9]*) °C Humidity: ([0-9]*\\.[0-9]*) %\r\n", readMessage)) {
                    value = readMessage.substring(1);
                    Log.d(TAG, "End of line");
                    break;
                }

                else {
                    bytes++;
                }


            }

            catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
            }
        }
    }


    //Call this method to close the socket
    public void closeSocket() {
        try {
            socket.close();
        }

        catch (IOException e) {
            Log.e(TAG, "Failed to close the connect socket", e);
        }
    }

    public String getValue() {
        return value;
    }
}