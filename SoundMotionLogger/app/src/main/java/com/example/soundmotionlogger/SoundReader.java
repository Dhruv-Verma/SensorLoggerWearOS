package com.example.soundmotionlogger;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.soundmotionlogger.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SoundReader {
    private static final String TAG = SoundReader.class.getName();
    private static final SoundReader instance = new SoundReader();

    private boolean readThreadRunning = false;
    private Thread readThread;
    private String filePath;
    private OutputStream outputStream;

    private AudioRecord audioRecorder;
    int audioBufferSize = Constants.SOUNDSTREAMER_BUFFER_CHUNK_SIZE*2;
    int audioSamplingRate = Constants.SOUNDSTREAMER_SAMPLING_RATE;
    byte[] audioBuffer = new byte[audioBufferSize];

    private Thread postProcessor = null;

    private SoundReader() {
        this.readThreadRunning = false;
        this.readThread = null;
    }

    public static SoundReader getInstance() {
        return instance;
    }

    private void readThreadFunction() {

        // Initialize Audio Recorder
        if (audioRecorder == null) {
            audioRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    audioSamplingRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBufferSize
            );
        }
        // Start Recording
        if (audioRecorder != null) {
            while(audioRecorder.getState() != AudioRecord.STATE_INITIALIZED){
                //busy waiting
            }
            audioRecorder.startRecording();
        }

        while(readThreadRunning) {
            try {
                //////////////////////////////////////////////
                // Audio Buffer Recording
                //////////////////////////////////////////////
                if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    int pos = 0;
//                    while(pos<audioBufferSize) {
                    int n_read = audioRecorder.read(audioBuffer, pos, audioBufferSize - pos);
                    if (n_read < 0) {
                        Log.d("Stopping thread", "read " + n_read);
                        stopReadThread();
                    } else {
                        pos += n_read;
                    }
//                    }
                    outputStream.write(audioBuffer);
                }

            } catch(Exception e) {
                e.printStackTrace();
                break;
            }
        }

        Log.w(TAG, "Read thread shutting down");
        readThreadRunning = false;
        readThread = null;

    }

    public void startReadThread(String streamFolder, String fileName) throws FileNotFoundException {

        if(readThread != null) {
            Log.w(TAG, "Read thread already started");
            return;
        }
        Log.d(TAG, "Starting read thread");

        this.filePath = streamFolder + "/" + fileName;
        this.outputStream = new FileOutputStream(this.filePath + "_audio.raw", false);
        readThreadRunning = true;

        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readThreadFunction();
            }
        });

        readThread.setName("SoundReaderThread");
        readThread.setDaemon(true);
        readThread.start();
        Log.d(TAG, "Started read thread");
    }

    public void stopReadThread() throws IOException, InterruptedException {

        if(readThread == null) {
            Log.w(TAG, "Read thread already stopped");
            return;
        }
        readThreadRunning = false;

        if (audioRecorder != null) {
            audioRecorder.stop();
        }
//        -- was here
        Log.d(TAG, "Stopping read thread");

        if(readThread != null) {
            readThread.join();
            Log.d(TAG, "Waiting for read thread to complete...");
        }

        Log.d(TAG, "Stopped read thread");

        audioRecorder.release();
        audioRecorder = null;

        Log.d(TAG, "Audio recorder released");

        outputStream.close();
        outputStream = null;
        Log.d(TAG, "File closed");

//        postProcessor = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                postProcessing();
//            }
//        };
//        postProcessor.start();
    }

    public void postProcessing(){
        try {
            PcmToWav.convertAudioFiles( this.filePath + "_audio.raw", this.filePath + "_audio.wav", this.audioSamplingRate);
            Log.d(TAG, "Converted to Wav file");
        } catch (Exception e){
            e.printStackTrace();
        }

        File rawAudioFile = new File(filePath+"_audio.raw");
        if(!rawAudioFile.delete()){
            Log.e(TAG, "Error while deleting PCM file");
        }
    }

}
