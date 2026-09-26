package com.example.kisansathi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ExpertProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "KisanSathiPrefs";

    private TextView expertProfileBackButton;
    private TextView expertProfileName;
    private TextView expertProfileUserType;
    private TextView expertProfileMobile;
    private TextView expertProfileVillage;
    private TextView expertProfileDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expert_profile);

        initializeViews();
        loadExpertProfile();
        setupClicks();
    }

    private void initializeViews() {

        expertProfileBackButton =
                findViewById(R.id.expertProfileBackButton);

        expertProfileName =
                findViewById(R.id.expertProfileName);

        expertProfileUserType =
                findViewById(R.id.expertProfileUserType);

        expertProfileMobile =
                findViewById(R.id.expertProfileMobile);

        expertProfileVillage =
                findViewById(R.id.expertProfileVillage);

        expertProfileDistrict =
                findViewById(R.id.expertProfileDistrict);
    }

    private void loadExpertProfile() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        String name =
                preferences.getString(
                        "user_name",
                        "Expert"
                );

        String mobile =
                preferences.getString(
                        "mobile_number",
                        "Not available"
                );

        String village =
                preferences.getString(
                        "village",
                        "Not available"
                );

        String district =
                preferences.getString(
                        "district",
                        "Not available"
                );

        String userType =
                preferences.getString(
                        "user_type",
                        "Expert"
                );

        expertProfileName.setText(name);
        expertProfileUserType.setText(userType);
        expertProfileMobile.setText("+91 " + mobile);
        expertProfileVillage.setText(village);
        expertProfileDistrict.setText(district);
    }

    private void setupClicks() {

        expertProfileBackButton.setOnClickListener(
                v -> finish()
        );
    }
}