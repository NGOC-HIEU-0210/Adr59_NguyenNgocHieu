package com.hieu.baitap;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hieu.baitap.fragments.FragmentA;
import com.hieu.baitap.fragments.FragmentB;
import com.hieu.baitap.fragments.FragmentC;
import com.hieu.baitap.fragments.FragmentD;

public class MainActivity extends AppCompatActivity {
    private Button btnTagA, btnTagB, btnTagC, btnTagD;
    private static final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Log.d(TAG, "onCreate - Khởi tạo");
        setContentView(R.layout.activity_main);
        btnTagA = findViewById(R.id.btnTagA);
        btnTagB = findViewById(R.id.btnTagB);
        btnTagC = findViewById(R.id.btnTagC);
        btnTagD = findViewById(R.id.btnTagD);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnTagA.setOnClickListener(v -> {
            fragmentA();
            setSelected(btnTagA);
        });
        btnTagB.setOnClickListener(v -> {
            fragmentB();
            setSelected(btnTagB);
        });
        btnTagC.setOnClickListener(v -> {
            fragmentC();
            setSelected(btnTagC);
        });
        btnTagD.setOnClickListener(v -> {
            fragmentD();
            setSelected(btnTagD);
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart - Khởi tạo xong, đang start Activity");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - start xong, sẵn sàng nhận tương tác");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - đã dừng Activity");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop - not visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - shutdown");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart - khởi động lại");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState - lưu trạng thái");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState - load trạng thái đã lưu");
    }
    private void fragmentA(){
        FragmentA fragmentA = new FragmentA();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragmentA,"MY_TAG")
                .addToBackStack(null)
                .commit();

    }
    private void fragmentB(){
        FragmentB fragmentB = new FragmentB();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragmentB,"MY_TAG")
                .addToBackStack(null)
                .commit();

    }
    private void fragmentC(){
        FragmentC fragmentC = new FragmentC();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragmentC,"MY_TAG")
                .addToBackStack(null)
                .commit();

    }
    private void fragmentD(){
        FragmentD fragmentD = new FragmentD();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragmentD,"MY_TAG")
                .addToBackStack(null)
                .commit();

    }

    private void setSelected(Button selected){
        Button[] buttons = {btnTagA, btnTagB, btnTagC, btnTagD};
        for (Button button : buttons) {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2396F3")));

    }
}