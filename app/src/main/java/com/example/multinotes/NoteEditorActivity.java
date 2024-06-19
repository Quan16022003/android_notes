package com.example.multinotes;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NoteEditorActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 123;
    private static final int PICK_IMAGE_REQUEST = 1;
    private int noteId;
    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonReminder;
    private TextView textViewLastUpdate;
    private ImageView imageView;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24);
        }

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);
        buttonReminder = (Button) findViewById(R.id.btnAlarm);
        textViewLastUpdate = (TextView) findViewById(R.id.textViewLastUpdate);
        imageView = (ImageView) findViewById(R.id.my_image);

        buttonReminder.setVisibility(View.GONE);
        Intent intent = getIntent();

        noteId = intent.getIntExtra("noteId", -1);
        if (noteId != -1) {
            note = MainActivity.notes.get(noteId);
            editTextTitle.setText(note.getTitle());
            editTextContent.setText(note.getContent());
            if (note.getReminderDate() != null) {
                buttonReminder.setVisibility(View.VISIBLE);
                buttonReminder.setText(formatDateTime(note.getReminderDate()));
            }
        } else {
            note = createNewNote();
        }
        Log.d("NOTE", note.toString());
        buttonReminder.setOnClickListener(view -> {
            showReminderDialog();
        });
        if (note.getUpdateTime() != null) {
            textViewLastUpdate.setText("Last Update: " + formatDateTime(note.getUpdateTime()));
        } else {
            textViewLastUpdate.setVisibility(View.GONE);
        }
        if (note.getImagePath() != null) {
            Uri imageUri = Uri.parse(note.getImagePath());
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(imageUri);
        } else {
            imageView.setVisibility(View.GONE);
        }
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteEditorActivity.this);
                builder.setMessage("Bạn có muốn xóa hình ảnh này không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Xóa hình ảnh
                                imageView.setVisibility(View.GONE);
                                imageView.setImageDrawable(null);
                                note.setImagePath(null);
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                return true;
            }
        });
    }

    private Note createNewNote() {
        Note newNote = new Note();
        newNote.setId(UUID.randomUUID().hashCode());
        return newNote;
    }

    private void save() {
        DatabaseHandler db = new DatabaseHandler(this);
        if (editTextTitle.getText().toString().equals("") &&
                editTextContent.getText().toString().equals("")) return;

        if (noteId != -1) {
            note.setTitle(String.valueOf(editTextTitle.getText()));
            note.setContent(String.valueOf(editTextContent.getText()));
            db.updateNote(note);

            if (note.getReminderDate() != null) {
                createOrUpdateNotification(note.getId(), "Notification Title", "Notification content", note.getReminderDate());
            } else {
                cancelNotification(note.getId());
            }
        } else {
            note.setTitle(editTextTitle.getText().toString());
            note.setContent(editTextContent.getText().toString());
            db.addNote(note);
            if (note.getReminderDate() != null) {
                createOrUpdateNotification(note.getId(), "Notification Title", "Notification content", note.getReminderDate());
            }
        }
        MainActivity.needReload = true;
    }

    private void cancelNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(id);
    }

    private void createOrUpdateNotification(int id, String title, String content, Date reminderDate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_24)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reminderDate);

        Intent intent = new Intent(this, NoteEditorActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS);
            return;
        }
        notificationManager.notify(id, builder.build());

    }

    @Override
    public void finish() {
        save();
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_add_alert) {
            showReminderDialog();
            return true;
        } else if (item.getItemId() == R.id.action_add_photo) {
            addPhoto();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void updateDateTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        Log.d("Time", String.valueOf(year));
        Log.d("Time", String.valueOf(month));
        Log.d("Time", String.valueOf(dayOfMonth));
        Log.d("Time", String.valueOf(hourOfDay));
        Log.d("Time", String.valueOf(minute));
    }
    public void showReminderDialog() {
        NoteReminderDialog reminderDialog = new NoteReminderDialog(this, note.getReminderDate());
        reminderDialog.setListener(new NoteReminderDialog.OnClickButtonListener() {
            @Override
            public void onClickOkButton(Date date) {
                note.setReminderDate(date);
                buttonReminder.setVisibility(View.VISIBLE);
                buttonReminder.setText(formatDateTime(date));
            }

            @Override
            public void onClickDeleteButton() {
                note.setReminderDate(null);
                buttonReminder.setVisibility(View.GONE);
            }
        });
        reminderDialog.show();
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat;
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(java.util.Calendar.YEAR);

        if (year == currentYear) {
            dateFormat = new SimpleDateFormat("d 'tháng' M", Locale.getDefault());
        } else {
            dateFormat = new SimpleDateFormat("d 'tháng' M, yyyy", Locale.getDefault());
        }

        return dateFormat.format(date) + ", " + timeFormat.format(date);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS: {
                // Kiểm tra xem quyền đã được cấp hay không
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Quyền đã được cấp, thực hiện các hành động cần thiết ở đây
                } else {
                    // Người dùng từ chối cấp quyền, bạn có thể thông báo hoặc xử lý theo cách khác
                }
                return;
            }
            // Các trường hợp xử lý quyền khác nếu cần
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Save the image URI to the current note
            note.setImagePath(imageUri.toString());

            // Display the selected image
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(imageUri);
        }
    }
}
