package com.example.soundmotionlogger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.security.KeyException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocationSession {

    private final static String TAG = BluetoothSession.class.getName();
    private MainActivity mContext;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;private ScheduledExecutorService scheduler;
    private FileStreamer mFileStreamer = null;
    private final static int MAX_LOCATION_SAMPING_DELAY = 30; // seconds
    private final static int MIN_LOCATION_SAMPLING_DELAY = 5; // seconds
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private AtomicBoolean mIsWritingFile = new AtomicBoolean(false);

    public LocationSession(Context context) {
        mContext = (MainActivity) context;
        this.scheduler = null;
    }

    public void startSession(String streamFolder, String fileName) {

        // Set up location requests
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * MAX_LOCATION_SAMPING_DELAY);
        locationRequest.setFastestInterval(1000 * MIN_LOCATION_SAMPLING_DELAY);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // initialize text file streams
        if (streamFolder != null) {
            mFileStreamer = new FileStreamer(mContext, streamFolder);
            try {
                mFileStreamer.addFile("loc", fileName + "_" + "loc" + ".csv");
                mIsWritingFile.set(true);
            } catch (IOException e) {
                mContext.showToast("Error occurred while creating output sensor files.");
                e.printStackTrace();
            }
        }

        // Set up locationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                saveLocation(locationResult.getLastLocation());
            }
        };

        mIsRecording.set(true);

        getLastLocation();

        // Schedule location updates
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates();
            }
        }, 0,MIN_LOCATION_SAMPLING_DELAY, TimeUnit.SECONDS);
    }

    public void stopSession() {
        stopLocationUpdates();
        mIsRecording.set(false);

        if (mIsWritingFile.get()) {

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
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(mContext, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                saveLocation(location);
            }
        });
    }

    private void saveLocation(Location location) {

        if (location == null)
            return;

        boolean isFileSaved = (this.mIsRecording.get() && this.mIsWritingFile.get());
        long timestamp = location.getTime();
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();
        float altitude = (float) location.getAltitude();
        float accuracy = (float) location.getAccuracy();
        Log.e(TAG, longitude + "," + latitude + "," + altitude + "," + accuracy + "," + timestamp);
        if (isFileSaved)
            try {
                mFileStreamer.addRecord(timestamp, "loc", 4, new float[]{latitude, longitude, altitude, accuracy});
            } catch (IOException | KeyException e) {
                Log.d(TAG, "saveLocation: Something is wrong.");
                e.printStackTrace();
            }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


}
