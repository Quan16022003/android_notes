package com.example.multinotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static List<Note> notes = new ArrayList<>();
    static CustomAdapter customAdapter;
    static boolean needReload = false;

    private DatabaseHandler db = new DatabaseHandler(this);
    private RecyclerView recyclerView;
    static String NOTIFICATION_CHANNEL_ID = "multinoteheheh";
    static String NOTIFICATION_CHANNEL_NAME = "Multi notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        recyclerView = findViewById(R.id.recyclerView);
        db.deleteAll();
        notes = db.getAllNotes();
        for (Note note : notes) {
            Log.d("Name: ", note.getTitle());
        }
        setupRecyclerView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        if (needReload) {
            notes = db.getAllNotes();
            setupRecyclerView();
            needReload = false;
        }
        super.onStart();
    }

    private void setupRecyclerView() {
        customAdapter = new CustomAdapter(this, notes);
        customAdapter.setOnClickItemListener(new CustomAdapter.OnClickItemListener() {

            @Override
            public void onClickItem(int position) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteId", position);
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(int position) {
                // To delete the data from the App
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (position >= 0 && position < notes.size()) {
                                    Note note = notes.get(position);
                                    notes.remove(position);
                                    customAdapter.notifyDataSetChanged();
                                    db.deleteNote(note.getId());
                                }
                            }
                        }).setNegativeButton("No", null).show();
                return true;
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(customAdapter);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_note) {
            Intent intent = new Intent(this, NoteEditorActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}