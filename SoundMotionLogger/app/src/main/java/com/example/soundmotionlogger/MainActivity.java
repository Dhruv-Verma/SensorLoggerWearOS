package com.example.soundmotionlogger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends WearableActivity {

    private final static String LOG_TAG = MainActivity.class.getName();

    private final static int REQUEST_CODE_ANDROID = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String mOutputDirectory = null;
    private String mActivityName = null;
    private String mSubjectName = null;
    private IMUSession mIMUSession;
    private BluetoothSession mBluetoothSession;
    private LocationSession mLocationSession;
    private Handler mHandler = new Handler();
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private PowerManager.WakeLock mWakeLock;
    private PowerManager powerManager;

    private TextView mLabelActivity;
    private ImageButton mStartStopButton;
    private TextView mLabelInterfaceTime;

    private Timer mInterfaceTimer = new Timer();
    private int mSecondCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mOutputDirectory = Environment.getExternalStorageDirectory() + "/SoundMotionStreamer";
        this.mActivityName = getIntent().getStringExtra("ACTIVITY_NAME");
        this.mSubjectName = getIntent().getStringExtra("SUBJECT_NAME");

        Log.e(LOG_TAG, mActivityName + " by " + mSubjectName);

        // initialize screen labels and buttons
        initializeViews();

        // battery power setting
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLockAcquire();

        // setup sessions
        mIMUSession = new IMUSession(this);
        Log.e(LOG_TAG, "IMU Session initialized" );

        mBluetoothSession = new BluetoothSession(this);
        Log.e(LOG_TAG, "Bluetooth Session initialized" );

        mLocationSession = new LocationSession(this);
        Log.e(LOG_TAG, "Location Session initialized" );

        // Enables Always-on
        setAmbientEnabled();
    }

    private void initializeViews() {
        mLabelActivity = (TextView) findViewById(R.id.label_activity);
        mStartStopButton = (ImageButton) findViewById(R.id.button_start_stop);
        mLabelInterfaceTime = (TextView) findViewById(R.id.label_timer);
        mLabelActivity.setText(mActivityName);
        mStartStopButton.setImageResource(R.drawable.baseline_play_circle_light_green_a700_24dp);
        mLabelInterfaceTime.setText(R.string.timer_placeholder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_ANDROID);
        }
        updateConfig();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mIsRecording.get()) {
            stopRecording();
        }
        wakeLockRelease();
//        mIMUSession.unregisterSensors();
        super.onDestroy();
    }

    // methods
    public void startStopRecording(View view) {
        if (!mIsRecording.get()) {

            // start recording sensor measurements when button is pressed
            startRecording();

            // make UI changes
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStartStopButton.setEnabled(true);
                    mStartStopButton.setImageResource(R.drawable.baseline_stop_circle_red_a700_24dp);
                }
            });
            showToast("Recording starts!");

            // start interface timer on display
            mSecondCounter = 0;
            mInterfaceTimer = new Timer();
            mInterfaceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSecondCounter += 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLabelInterfaceTime.setText(interfaceIntTime(mSecondCounter));
                        }
                    });
                }
            }, 0, 1000);

        } else {

            // stop recording sensor measurements when button is pressed
            stopRecording();

            // stop interface timer on display
            mInterfaceTimer.cancel();

            // make UI changes
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("Recording stops!");
                    resetUI();
                }
            });
        }
    }

    private void startRecording() {

        // create output folder if not created
        File mOutputDirectoryFd = new File(mOutputDirectory);
        if(!mOutputDirectoryFd.exists())
            if(!mOutputDirectoryFd.mkdirs())
                Log.e(LOG_TAG, "Directory creation failed");

        String mFileName = mSubjectName + "_" + mActivityName;

        // start IMU session
        mIMUSession.startSession(mOutputDirectory, mFileName);

        // start Audio session
        try {
            SoundReader.getInstance().startReadThread(mOutputDirectory, mFileName);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        // start Bluetooth session
        mBluetoothSession.startSession(mOutputDirectory, mFileName);

        // start Location session
        mLocationSession.startSession(mOutputDirectory, mFileName);

        mIsRecording.set(true);
    }

    protected void stopRecording() {

        // stop IMU session
        mIMUSession.stopSession();

        // stop audio session
        try {
            SoundReader.getInstance().stopReadThread();
        } catch (InterruptedException | IOException e){
            e.printStackTrace();
        }

        // stop Bluetooth session
        mBluetoothSession.stopSession();

        // stop Location session
        mLocationSession.stopSession();

        mIsRecording.set(false);
    }

    private static boolean hasPermissions(Context context, String... permissions) {

        // check Android hardware permissions
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void updateConfig() {
        final int MICRO_TO_SEC = 1000;
    }

    public void showAlertAndStop(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(text)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stopRecording();
                            }
                        }).show();
            }
        });
    }

    private void resetUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartStopButton.setEnabled(true);
                mStartStopButton.setImageResource(R.drawable.baseline_play_circle_light_green_a700_24dp);
                mLabelInterfaceTime.setText(R.string.timer_placeholder);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // nullify back button when recording starts
        if (!mIsRecording.get()) {
            super.onBackPressed();
            wakeLockRelease();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE_ANDROID) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                showToast("Permission not granted");
                finish();
                return;
            }
        }
    }

    private String interfaceIntTime(final int second) {
        // check second input
        if (second < 0) {
            showAlertAndStop("Second cannot be negative.");
        }

        // extract hour, minute, second information from second
        int input = second;
        int hours = input / 3600;
        input = input % 3600;
        int mins = input / 60;
        int secs = input % 60;

        // return interface int time
        return String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs);
    }

    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void wakeLockAcquire(){
        if (mWakeLock != null){
            Log.e(LOG_TAG, "WakeLock already acquired!");
            return;
        }
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensors_data_logger:wakelocktag");
        mWakeLock.acquire(600*60*1000L /*10 hours*/);
        Log.e(LOG_TAG, "WakeLock acquired!");
    }

    private void wakeLockRelease(){
        if (mWakeLock != null && mWakeLock.isHeld()){
            mWakeLock.release();
            Log.e(LOG_TAG, "WakeLock released!");
            mWakeLock = null;
        }
        else{
            Log.e(LOG_TAG, "No wakeLock acquired!");
        }
    }

}