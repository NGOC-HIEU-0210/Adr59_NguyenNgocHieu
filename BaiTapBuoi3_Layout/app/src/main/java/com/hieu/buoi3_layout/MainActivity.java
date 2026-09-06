package com.hieu.buoi3_layout;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        ImageView iconEye = findViewById(R.id.iconEye);
        EditText editPassword = findViewById(R.id.editTextTextPassword);
        Typeface currentTypeface = editPassword.getTypeface();
        boolean[] isPasswordVisible = {false};

        iconEye.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                iconEye.setImageResource(R.drawable.eye_off);
            }
            else {
                editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                iconEye.setImageResource(R.drawable.eye_on);
            }
            editPassword.setTypeface(currentTypeface);
            isPasswordVisible[0] = !isPasswordVisible[0];
            editPassword.setSelection(editPassword.length());
        });
    }
}