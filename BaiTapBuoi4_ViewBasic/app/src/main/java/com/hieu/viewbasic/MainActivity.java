package com.hieu.viewbasic;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "123456";

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
        EditText editUsername = findViewById(R.id.editTextText);
        EditText editPassword = findViewById(R.id.editTextTextPassword);
        TextView tvError = findViewById(R.id.tvError);
        Button btnLogin = findViewById(R.id.button);

        Typeface currentTypeface = editPassword.getTypeface();
        boolean[] isPasswordVisible = {false};

        iconEye.setOnClickListener(v -> {
            if (isPasswordVisible[0]) {
                editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                iconEye.setImageResource(R.drawable.eye_off);
            } else {
                editPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                iconEye.setImageResource(R.drawable.eye_on);
            }
            editPassword.setTypeface(currentTypeface);
            isPasswordVisible[0] = !isPasswordVisible[0];
            editPassword.setSelection(editPassword.length());
        });


        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showError(tvError, "Vui lòng nhập đầy đủ thông tin");
                return;
            }

            if (username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD)) {
                tvError.setVisibility(View.GONE);

            } else {
                showError(tvError, "Sai tên đăng nhập hoặc mật khẩu");
            }
        });


        editUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvError.setVisibility(View.GONE);
        });
        editPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tvError.setVisibility(View.GONE);
        });
    }

    private void showError(TextView tvError, String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}