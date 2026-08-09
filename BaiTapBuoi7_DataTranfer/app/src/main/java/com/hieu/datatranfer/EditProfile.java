package com.hieu.datatranfer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hieu.datatranfer.model.UserProfile;

public class EditProfile extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtAddress;
    private TextView tvGender;

    private UserProfile userProfile = new UserProfile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        tvGender = findViewById(R.id.tvGender);

        UserProfile received = IntentCompat.getSerializableExtra(
                getIntent(), Profile.EXTRA_PROFILE, UserProfile.class);

        if (received != null) {
            userProfile = received;
            edtName.setText(userProfile.getName());
            edtEmail.setText(userProfile.getEmail());
            tvGender.setText(userProfile.getGender());
            edtPhone.setText(userProfile.getPhone());
            edtAddress.setText(userProfile.getAddress());
        }

        findViewById(R.id.layoutGender).setOnClickListener(v -> {
            if ("Male".equals(tvGender.getText().toString())) {
                tvGender.setText("Female");
            } else {
                tvGender.setText("Male");
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String gender = tvGender.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên");
            edtName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        userProfile.setName(name);
        userProfile.setEmail(email);
        userProfile.setGender(gender);
        userProfile.setPhone(phone);
        userProfile.setAddress(address);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Profile.EXTRA_PROFILE, userProfile);

        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        finish();
    }
}