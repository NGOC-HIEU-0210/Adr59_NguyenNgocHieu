package com.hieu.baitapbuoi9;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private EditText edtGhiChu;
    private Button btnGui;
    private RecyclerView rcvGhiChu;

    private NoteAdapter noteAdapter;
    private NoteDatabase noteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        edtGhiChu = findViewById(R.id.edtGhiChu);
        btnGui = findViewById(R.id.btnGui);
        rcvGhiChu = findViewById(R.id.rcvGhiChu);


        noteDatabase = NoteDatabase.getInstance(this);


        noteAdapter = new NoteAdapter();
        rcvGhiChu.setLayoutManager(new LinearLayoutManager(this));
        rcvGhiChu.setAdapter(noteAdapter);


        noteDatabase.noteDao().getAllNotes().observe(this, notes -> {
            noteAdapter.setNotes(notes);
        });

        btnGui.setOnClickListener(v -> {
            String content = edtGhiChu.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "Vui lòng nhập ghi chú", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Note note = new Note(content);
                noteDatabase.noteDao().insert(note);
            }).start();

            edtGhiChu.setText("");
        });
    }
}