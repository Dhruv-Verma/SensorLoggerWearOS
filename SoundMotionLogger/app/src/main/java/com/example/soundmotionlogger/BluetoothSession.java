package com.example.soundmotionlogger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.security.KeyException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothSession {

    private final static String TAG = BluetoothSession.class.getName();
    private MainActivity mContext;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 1313;
    private ScheduledExecutorService scheduler;
    private FileStreamer mFileStreamer = null;
    private final static int BLUETOOTH_SAMPING_DELAY = 30; // seconds
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private AtomicBoolean mIsWritingFile = new AtomicBoolean(false);

    public BluetoothSession(Context context) {
        mContext = (MainActivity) context;
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        this.scheduler = null;
        this.bluetoothAdapter.cancelDiscovery();
    }

    private void startDiscovery() {
        if (bluetoothAdapter.startDiscovery())
            Log.e(TAG, "Bluetooth discovery started");
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                boolean isFileSaved = (BluetoothSession.this.mIsRecording.get() && BluetoothSession.this.mIsWritingFile.get());
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = "" + device.getName();
                String deviceHardwareAddress = "" + device.getAddress(); // MAC address
                String RSSI = "" + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                long timestamp = SystemClock.elapsedRealtimeNanos();
//                Log.e(TAG, deviceName + "," + RSSI + "," + deviceHardwareAddress + "," + timestamp);
                if (isFileSaved) {
                    try {
                        Log.e(TAG, "onReceive: Bluetooth is writing..." );
                        mFileStreamer.addRecord(timestamp, "bluetooth", 3, new String[] {deviceName, RSSI, deviceHardwareAddress});
                    } catch (IOException | KeyException e) {
                        Log.d(TAG, "onActionFound: Something is wrong.");
                        e.printStackTrace();
                    }
                }
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, "Bluetooth discovery finished");
            }
        }
    };

    public void startSession(String streamFolder, String fileName) {
        IntentFilter filter_found = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter_completed = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(receiver, filter_found);
        mContext.registerReceiver(receiver, filter_completed);

        // initialize text file streams
        if (streamFolder != null) {
            mFileStreamer = new FileStreamer(mContext, streamFolder);
            try {
                mFileStreamer.addFile("bluetooth", fileName + "_" + "bluetooth" + ".csv");
                mIsWritingFile.set(true);
                } catch (IOException e) {
                mContext.showToast("Error occurred while creating output sensor files.");
                e.printStackTrace();
            }
        }
        mIsRecording.set(true);
        // Schedule remote device discovery process
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                startDiscovery();
            }
        }, 0,BLUETOOTH_SAMPING_DELAY, TimeUnit.SECONDS);
    }

    public void stopSession() {
        mIsRecording.set(false);
        if (mIsWritingFile.get()){

            // close all recorded text files
            try {
                mFileStreamer.endFiles();
            } catch (IOException e) {
                mContext.showToast("Error occurred while finishing IMU text files.");
                e.printStackTrace();
            }

            // reset some properties
            mIsWritingFile.set(false);
            mFileStreamer = null;
        }

        // Terminate the device discovery process
        scheduler.shutdown();
        while(!scheduler.isTerminated()) {
            ;
        }
        // Unregister the broadcast receiver
        mContext.unregisterReceiver(receiver);
        Log.e(TAG, "Bluetooth session stopped");
    }

}
