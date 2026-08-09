package com.hieu.datatranfer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hieu.datatranfer.model.UserProfile;

public class Profile extends AppCompatActivity {

    public static final String EXTRA_PROFILE = "EXTRA_PROFILE";

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_AVATAR_URI = "avatar_uri";

    private TextView tvName, tvEmail;
    private ImageView imgAvatar;


    private UserProfile userProfile = new UserProfile(
            "Albert Florest",
            "albertflorest@email.com",
            "Male",
            "+44 1632 960860",
            "",
            null
    );


    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    UserProfile updated = IntentCompat.getSerializableExtra(
                            result.getData(), EXTRA_PROFILE, UserProfile.class);

                    if (updated != null) {
                        userProfile = updated;
                        bindProfileToViews();
                    }
                }
            });

    private final ActivityResultLauncher<String[]> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;


                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {

                }

                userProfile.setAvatarUri(uri.toString());
                imgAvatar.setImageURI(uri);
                saveAvatarUri(uri.toString());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        imgAvatar = findViewById(R.id.imgAvatar);

        loadAvatarIfSaved();
        bindProfileToViews();

        findViewById(R.id.btnEditAvatar).setOnClickListener(v ->
                pickAvatarLauncher.launch(new String[]{"image/*"}));

        View.OnClickListener openEditProfile = v -> openEditProfile();
        findViewById(R.id.layoutEditProfile).setOnClickListener(openEditProfile);
        findViewById(R.id.btnEditProfileArrow).setOnClickListener(openEditProfile);

        View.OnClickListener notImplemented = v ->
                Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();

        findViewById(R.id.layoutNotification).setOnClickListener(notImplemented);
        findViewById(R.id.btnNotification).setOnClickListener(notImplemented);

        findViewById(R.id.layoutShippingAddress).setOnClickListener(notImplemented);
        findViewById(R.id.btnShippingAddress).setOnClickListener(notImplemented);

        findViewById(R.id.layoutChangePassword).setOnClickListener(notImplemented);
        findViewById(R.id.btnChangePassword).setOnClickListener(notImplemented);
    }

    private void openEditProfile() {
        Intent intent = new Intent(Profile.this, EditProfile.class);
        intent.putExtra(EXTRA_PROFILE, userProfile);
        editProfileLauncher.launch(intent);
    }

    private void bindProfileToViews() {
        tvName.setText(userProfile.getName());
        tvEmail.setText(userProfile.getEmail());
    }

    private void saveAvatarUri(String uriString) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_AVATAR_URI, uriString).apply();
    }

    private void loadAvatarIfSaved() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String uriString = prefs.getString(KEY_AVATAR_URI, null);
        if (uriString == null) return;

        try {
            imgAvatar.setImageURI(Uri.parse(uriString));
            userProfile.setAvatarUri(uriString);
        } catch (SecurityException e) {

        }
    }
}