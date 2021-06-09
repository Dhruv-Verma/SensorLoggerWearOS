package com.example.soundmotionlogger;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.soundmotionlogger.FileStreamer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.KeyException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IMUSession implements SensorEventListener {

    private final static String LOG_TAG = IMUSession.class.getName();

    private MainActivity mContext;
    private SensorManager mSensorManager;
    private HashMap<String, Sensor> mSensors = new HashMap<>();
    private FileStreamer mFileStreamer = null;

    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private AtomicBoolean mIsWritingFile = new AtomicBoolean(false);

    private float[] mAcceMeasure = new float[3];
    private float[] mGyroMeasure = new float[3];
    private int samplingRateAcce = 0;
    private int samplingRateGyro = 0;
    private long prevTSAcce = 0;
    private long prevTSGyro = 0;

    // constructor
    public IMUSession(MainActivity context) {

        // initialize object and sensor manager
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // setup and register various sensors
        mSensors.put("acce", mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensors.put("gyro", mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
    }

    // methods
    public void registerSensors() {
        for (Sensor eachSensor : mSensors.values()) {
            mSensorManager.registerListener(this, eachSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void unregisterSensors() {
        for (Sensor eachSensor : mSensors.values()) {
            mSensorManager.unregisterListener(this, eachSensor);
        }
    }

    public void startSession(String streamFolder, String fileName) {
        registerSensors();

        // initialize text file streams
        if (streamFolder != null) {
            mFileStreamer = new FileStreamer(mContext, streamFolder);
            try {
                for(String id: mSensors.keySet()){
                    mFileStreamer.addFile(id, fileName + "_" + id + ".csv");
                }
                mIsWritingFile.set(true);
            } catch (IOException e) {
                mContext.showToast("Error occurred while creating output IMU files.");
                e.printStackTrace();
            }
        }
        mIsRecording.set(true);
    }

    public void stopSession() {

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

        unregisterSensors();

        // TODO: get back to the main activity
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {

        // set some variables
        boolean isFileSaved = (mIsRecording.get() && mIsWritingFile.get());

        // update each sensor measurements
        // NOTE: This is the place to change the timestamp to unix if we want to
        long timestamp = sensorEvent.timestamp;
        Sensor eachSensor = sensorEvent.sensor;
        try {
            switch (eachSensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAcceMeasure[0] = sensorEvent.values[0];
                    mAcceMeasure[1] = sensorEvent.values[1];
                    mAcceMeasure[2] = sensorEvent.values[2];
                    if (isFileSaved) {
                        mFileStreamer.addRecord(timestamp, "acce", 3, sensorEvent.values);
                    }
                    samplingRateAcce = (int) (1000000000 / (timestamp - prevTSAcce));
                    prevTSAcce = timestamp;
                    if( timestamp % 7 == 0)
                        Log.e(LOG_TAG, "Acce sampling rate " +  samplingRateAcce);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    mGyroMeasure[0] = sensorEvent.values[0];
                    mGyroMeasure[1] = sensorEvent.values[1];
                    mGyroMeasure[2] = sensorEvent.values[2];
                    if (isFileSaved) {
                        mFileStreamer.addRecord(timestamp, "gyro", 3, sensorEvent.values);
                    }
                    samplingRateGyro = (int) (1000000000 / (timestamp - prevTSGyro));
                    prevTSGyro = timestamp;
                    if( timestamp % 7 == 0)
                        Log.e(LOG_TAG, "Gyro sampling rate " +  samplingRateGyro);
                    break;
            }
        } catch (IOException | KeyException e) {
            Log.d(LOG_TAG, "onSensorChanged: Something is wrong.");
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // getter and setter
    public boolean isRecording() {
        return mIsRecording.get();
    }

    public float[] getAcceMeasure() {
        return mAcceMeasure;
    }

    public float[] getGyroMeasure() {
        return mGyroMeasure;
    }
}