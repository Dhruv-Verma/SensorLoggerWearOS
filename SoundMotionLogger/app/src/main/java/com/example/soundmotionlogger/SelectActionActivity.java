package com.example.soundmotionlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SelectActionActivity extends WearableActivity {

    private WearableRecyclerView mRecyclerViewActivities;
    private ActivitiesListAdaptor adaptor;
    private String[] mListActivities;
    public String mSubjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        this.mSubjectName = getIntent().getStringExtra("SUBJECT_NAME");

        initListData();

        mRecyclerViewActivities = (WearableRecyclerView) findViewById(R.id.recycler_launcher_view);
        mRecyclerViewActivities.setAdapter(adaptor);
        mRecyclerViewActivities.setEdgeItemsCenteringEnabled(true);
        mRecyclerViewActivities.setLayoutManager(new WearableLinearLayoutManager(this));
        mRecyclerViewActivities.requestFocus();

        // Enables Always-on
//        setAmbientEnabled();
    }

    private void initListData(){
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
        int selectedItem = 1;

        Intent intent = new Intent(this, MainActivity.class);
//        SelectActionActivity contextInstance = (SelectActionActivity) this;
        intent.putExtra("SUBJECT_NAME", mSubjectName);
        intent.putExtra("ACTIVITY_NAME", mListActivities[selectedItem]);
        startActivity(intent);
    }
}