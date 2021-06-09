package com.example.soundmotionlogger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;

public class NextActivity extends WearableActivity {

    private String[] mListActivities;
    private String mSubjectName;
    private int counter;
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mSubjectName = getIntent().getStringExtra("SUBJECT_NAME");
        counter = 0;
        status = 1;
    }

    public void onClickNextButton(View v){
        // TODO: Delete later
        Log.d("message", "Hi from the Next button!" + mSubjectName + String.valueOf(counter));
        counter += 1;

        // go the next action
        int nextActivity = getNextActivity(mSubjectName, status);

        // copied from SelectActionActivity
        mListActivities = new String[]{"Phone Ringing", "Vacuum in Use", "Speech",
                "Chopping", "Door in Use", "Knocking", "Coughing", "Water Running", "Shaver in Use", "Toothbrushing",
                "Toilet Flushing", "Hairdryer in Use", "Laughing", "Typing", "Blender in Use", "Brushing Hair",
                "Clapping", "Scratching", "Twisting Jar", "Pouring", "Drinking", "Grating", "Wiping with Rag",
                "Washing Utensils", "Washing Hands"};
        Arrays.sort(mListActivities);
        // adaptor = new ActivitiesListAdaptor(this, mListActivities);

        // Choose a random activity and jump straight to data collection
        // TODO: include the way to randomize the order of activities to perform
        // TODO: replace this with a getNextItem(pID, state) function
        int selectedItem = nextActivity;

        Intent intent = new Intent(this, MainActivity.class);
//        SelectActionActivity contextInstance = (SelectActionActivity) this;
        intent.putExtra("SUBJECT_NAME", mSubjectName);
        intent.putExtra("ACTIVITY_NAME", mListActivities[selectedItem]);
        startActivity(intent);

    }

    private int getNextActivity(String pID, int status){

        int max = 20;
        int min = 0;
        return (int)((Math.random() * (max - min)) + min);
    }
}