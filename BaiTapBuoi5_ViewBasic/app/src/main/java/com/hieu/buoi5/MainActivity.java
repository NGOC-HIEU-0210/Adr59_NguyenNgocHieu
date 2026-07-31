package com.hieu.buoi5;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnPre, btnNext;
    private ImageView imageView;
    private TextView textView;
    private int currentImage = 0;
    private int[] images = {R.drawable.anhdau, R.drawable.anhhai, R.drawable.anhba};
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

        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        if(currentImage <= 0) btnPre.setVisibility(View.INVISIBLE);
        if(currentImage >= images.length - 1) btnNext.setVisibility(View.INVISIBLE);
        btnNext.setOnClickListener(v -> {
            if (currentImage >= images.length - 1) return;

            currentImage++;
            imageView.setImageResource(images[currentImage]);
            textView.setText((currentImage + 1) + "/" + images.length);

            btnPre.setVisibility(View.VISIBLE);
            if (currentImage == images.length - 1) {
                btnNext.setVisibility(View.INVISIBLE);
            }
        });

        btnPre.setOnClickListener(v -> {
            if (currentImage <= 0) return;

            currentImage--;
            imageView.setImageResource(images[currentImage]);
            textView.setText((currentImage + 1) + "/" + images.length);

            btnNext.setVisibility(View.VISIBLE);
            if (currentImage == 0) {
                btnPre.setVisibility(View.INVISIBLE);
            }
        });
    }
}