package com.example.multinotes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteReminderDialog extends AppCompatDialog{
    Calendar calendar = Calendar.getInstance();
    private TextView dialogTitle;
    private Button selectDateBtn;
    private Button selectTimeBtn;
    private LinearLayout buttonContainer;
    private Button okBtn;
    private Button cancelBtn;
    private Button deleteBtn;
    private OnClickButtonListener listener;
    private boolean isEditMode;

    public NoteReminderDialog(@NonNull Context context, Date date) {
        super(context);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        if (date != null) {
            calendar.setTime(date);
            isEditMode = true;
        } else {
            isEditMode = false;
        }
    }

    public void setListener(OnClickButtonListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_dialog);

        dialogTitle = findViewById(R.id.dialogTitle);
        selectDateBtn = findViewById(R.id.selectDateBtn);
        selectTimeBtn = findViewById(R.id.selectTimeBtn);
        buttonContainer = findViewById(R.id.buttonContainer);
        okBtn = findViewById(R.id.okBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        if (isEditMode) {
            dialogTitle.setText("Chỉnh sửa nhắc nhở");
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            dialogTitle.setText("Thêm nhắc nhở");
            deleteBtn.setVisibility(View.GONE);
        }

        selectDateBtn.setText(formatDate(calendar.getTime()));
        selectTimeBtn.setText(formatTime(calendar.getTime()));

        selectDateBtn.setOnClickListener(v -> showDatePicker());

        selectTimeBtn.setOnClickListener(v -> showTimePicker());

        okBtn.setOnClickListener(v -> {
            // Xử lý khi nhấn nút OK
            listener.onClickOkButton(calendar.getTime());
            dismiss();
        });

        cancelBtn.setOnClickListener(v -> {
            // Xử lý khi nhấn nút Hủy
            dismiss();
        });

        deleteBtn.setOnClickListener(v -> {
            // Xử lý khi nhấn nút Xóa (chỉ hiển thị khi ở chế độ chỉnh sửa)
            listener.onClickDeleteButton();
            dismiss();
        });
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectDateBtn.setText(formatDate(calendar.getTime()));
                    }
                }, year, month, day);

        // Hiển thị DatePickerDialog
        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // Tạo TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // Xử lý khi người dùng chọn xong thời gian
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        selectTimeBtn.setText(formatTime(calendar.getTime()));
                    }
                }, hour, minute, true);

        // Hiển thị TimePickerDialog
        timePickerDialog.show();
    }
    public static String formatTime(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(date);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat;

        // Lấy năm hiện tại
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        // Lấy năm của ngày định dạng
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(java.util.Calendar.YEAR);

        // Kiểm tra điều kiện và chọn định dạng ngày
        if (year == currentYear) {
            dateFormat = new SimpleDateFormat("d 'tháng' M", Locale.getDefault());
        } else {
            dateFormat = new SimpleDateFormat("d 'tháng' M, yyyy", Locale.getDefault());
        }

        return dateFormat.format(date);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    interface OnClickButtonListener {
        void onClickOkButton(Date date);
        void onClickDeleteButton();
    }
}
