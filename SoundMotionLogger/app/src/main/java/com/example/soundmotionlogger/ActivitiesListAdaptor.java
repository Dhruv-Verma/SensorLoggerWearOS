package com.example.soundmotionlogger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.wear.widget.WearableRecyclerView;

public class ActivitiesListAdaptor extends WearableRecyclerView.Adapter<ActivitiesListAdaptor.ViewHolder> {

    private String[] localDataSet;
    private Context context;
    private int selectedItem;

    public static class ViewHolder extends WearableRecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.label_activity_list_item);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public ActivitiesListAdaptor(Context context, String[] dataSet) {
        this.context = context;
        this.localDataSet = dataSet;
        selectedItem = -1;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet[position]);

        viewHolder.itemView.setBackgroundColor(Color.parseColor("#000000"));
        viewHolder.getTextView().setTextColor(Color.parseColor("#FFFFFF"));

        if (selectedItem == position){
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#64DD17"));
            viewHolder.getTextView().setTextColor(Color.parseColor("#000000"));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousItem = selectedItem;
                selectedItem = position;
                notifyItemChanged(previousItem);
                notifyItemChanged(position);

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                SelectActionActivity contextInstance = (SelectActionActivity) context;
                intent.putExtra("SUBJECT_NAME", contextInstance.mSubjectName);
                intent.putExtra("ACTIVITY_NAME", localDataSet[selectedItem]);
                v.getContext().startActivity(intent);

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}
