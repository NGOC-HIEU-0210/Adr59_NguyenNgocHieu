package com.hieu.list_buoi6;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hieu.list_buoi6.adapters.ImageAdapter;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
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

        recyclerView = findViewById(R.id.rcv);
        configRecyclerView();
    }

    public void  configRecyclerView(){
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        List<Integer> images = Arrays.asList(R.drawable.anh1, R.drawable.anh2, R.drawable.anh3,R.drawable.anh4,
                                                R.drawable.anh5,R.drawable.anh6,R.drawable.anh7,R.drawable.anh8);

        recyclerView.setAdapter(new ImageAdapter(images, (position, view) -> {
            int imageResId = images.get(position);
            Intent intent = new Intent(MainActivity.this, Full_Image.class);
            intent.putExtra("image_res_id", imageResId);
            startActivity(intent);
        }));
    }
}