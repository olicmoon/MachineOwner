package com.olic.coffee;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String TAG = "BluetoothCoffee";
    BluetoothSocket mSocket;
    BluetoothDevice mDevice = null;

    final String DEVICE_NAME = "SampleServer";
    UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();

        final TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final class workerThread implements Runnable {
            private String btMsg;
            InputStream inputStream = null;
            OutputStream outputStream = null;

            public workerThread(String msg) {
                btMsg = msg;

                try {
                    mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                    if (!mSocket.isConnected())
                        mSocket.connect();

                    inputStream = mSocket.getInputStream();
                    outputStream = mSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

            }

            public void run()
            {
                try {
                    outputStream.write(btMsg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                while(!Thread.currentThread().isInterrupted())
                {
                    int bytesAvailable;
                    boolean workDone = false;

                    try {
                        bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] buf = new byte[bytesAvailable];
                            int readBytes = inputStream.read(buf);
                            Log.e(TAG, "Data available, readBytes:" + readBytes);

                            final String data = new String(buf, "US-ASCII");
                            tvStatus.setText(data);
                            Log.i(TAG, data);

                            if (workDone == true){
                                mSocket.close();
                                break;
                            }

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                Log.e(TAG, " Scanning.. " + device.getName());
                if(device.getName().equals(DEVICE_NAME)) {
                    Log.e(TAG, " Found device:" + device.getName());
                    mDevice = device;
                    (new Thread(new workerThread("testsetset"))).start();
                    break;
                }
            }
        }

    }
}