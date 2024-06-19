package com.example.multinotes;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    public interface OnClickItemListener {
        void onClickItem(int position);
        boolean onLongClick(int position);
    }
    private final List<Note> noteList;
    private final Context context;
    private OnClickItemListener listener;

    public CustomAdapter(Context context, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position, listener);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView contentTextView;
        public Button reminderDateButton;
        public TextView lastUpdateTextView;

        public ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.textViewTitle);
            contentTextView = view.findViewById(R.id.textViewContent);
            reminderDateButton = view.findViewById(R.id.buttonReminder);
            lastUpdateTextView = view.findViewById(R.id.textViewLastUpdate);
        }
        public void bind(final int position, final OnClickItemListener listener) {
            Note note = noteList.get(position);
            if (note.getTitle().isEmpty()) {
                titleTextView.setText("Không có tiêu đề");
            } else {
                titleTextView.setText(note.getTitle());
            }
            contentTextView.setText(note.getContent());

            SimpleDateFormat dateFormat;
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            Calendar cal = Calendar.getInstance();
            if (currentYear == cal.get(Calendar.YEAR)) {
                dateFormat = new SimpleDateFormat("d 'tháng' M, HH:mm", Locale.getDefault());
            } else {
                dateFormat = new SimpleDateFormat("d 'tháng' M, yyyy, HH:mm", Locale.getDefault());
            }
            // Hiển thị ngày nhắc nhở (nếu có)
            if (note.getReminderDate() != null) {
                cal.setTime(note.getReminderDate());
                String reminderDateString = dateFormat.format(note.getReminderDate());
                reminderDateButton.setText(reminderDateString);
            } else {
                reminderDateButton.setVisibility(View.GONE);
            }
            lastUpdateTextView.setText(dateFormat.format(note.getUpdateTime()));

            itemView.setOnClickListener(v -> {
                listener.onClickItem(position);
                Log.d("adapter", "onClickItem");
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return listener.onLongClick(position);
                }
            });
            Log.d("NOTE", note.toString());
        }
    }
}
