package com.example.soundmotionlogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SubjectDetailsActivity extends WearableActivity {

    ImageButton mButtonNext;
    EditText mTextSubjectName;
    TextView mLabelNameError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_details);

        mButtonNext = (ImageButton) findViewById(R.id.button_next1);
        mTextSubjectName = (EditText) findViewById(R.id.text_subject_name);
        mLabelNameError = (TextView) findViewById(R.id.label_name_error);

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTextSubjectName.getText().toString().length() != 0){
                    Intent intent = new Intent(v.getContext(), SelectActionActivity.class);
                    intent.putExtra("SUBJECT_NAME", mTextSubjectName.getText().toString());
                    v.getContext().startActivity(intent);
                }
                else{
                    mLabelNameError.setVisibility(View.VISIBLE);
                }
            }
        });

        // Enables Always-on
//        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLabelNameError.setVisibility(View.INVISIBLE);
    }
}