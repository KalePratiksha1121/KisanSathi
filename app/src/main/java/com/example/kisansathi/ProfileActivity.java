package com.example.kisansathi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText mobileInput;
    private EditText villageInput;
    private EditText districtInput;

    private TextView farmerOption;
    private TextView expertOption;
    private TextView profileContinueButton;
    private TextView mobileVerifiedText;

    private String selectedUserType = "";

    private static final String PREFS_NAME = "KisanSathiPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        initializeViews();
        loadMobileNumber();
        setupUserTypeSelection();

        profileContinueButton.setOnClickListener(v -> validateAndContinue());
    }

    private void initializeViews() {

        nameInput = findViewById(R.id.nameInput);
        mobileInput = findViewById(R.id.mobileInput);
        villageInput = findViewById(R.id.villageInput);
        districtInput = findViewById(R.id.districtInput);

        farmerOption = findViewById(R.id.farmerOption);
        expertOption = findViewById(R.id.expertOption);

        profileContinueButton = findViewById(R.id.profileContinueButton);
        mobileVerifiedText = findViewById(R.id.mobileVerifiedText);
    }

    private void loadMobileNumber() {

        String mobileNumber =
                getIntent().getStringExtra("mobile_number");

        if (mobileNumber != null) {

            mobileNumber = mobileNumber.trim();

            if (mobileNumber.startsWith("+91")) {
                mobileNumber = mobileNumber.substring(3);
            }

            mobileInput.setText(mobileNumber);
        }

        mobileInput.setEnabled(false);
        mobileInput.setFocusable(false);
        mobileInput.setClickable(false);

        if (mobileVerifiedText != null) {
            mobileVerifiedText.setVisibility(View.GONE);
        }
    }

    private void setupUserTypeSelection() {

        farmerOption.setOnClickListener(v -> {

            selectedUserType = "Farmer";

            farmerOption.setBackgroundResource(
                    R.drawable.continue_button
            );

            expertOption.setBackgroundResource(
                    R.drawable.language_card
            );
        });

        expertOption.setOnClickListener(v -> {

            selectedUserType = "Expert";

            expertOption.setBackgroundResource(
                    R.drawable.continue_button
            );

            farmerOption.setBackgroundResource(
                    R.drawable.language_card
            );
        });
    }

    private void validateAndContinue() {

        String name =
                nameInput.getText().toString().trim();

        String mobile =
                mobileInput.getText().toString().trim();

        String village =
                villageInput.getText().toString().trim();

        String district =
                districtInput.getText().toString().trim();

        // User type
        if (TextUtils.isEmpty(selectedUserType)) {

            Toast.makeText(
                    this,
                    "Please select Farmer or Expert",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Name
        if (TextUtils.isEmpty(name) || name.length() < 2) {

            nameInput.setError("Enter a valid name");
            nameInput.requestFocus();

            return;
        }

        // Mobile
        if (TextUtils.isEmpty(mobile)
                || !mobile.matches("[6-9][0-9]{9}")) {

            Toast.makeText(
                    this,
                    "Invalid mobile number",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Village
        if (TextUtils.isEmpty(village)
                || village.matches("[0-9]+")) {

            villageInput.setError("Enter a valid village");
            villageInput.requestFocus();

            return;
        }

        // District
        if (TextUtils.isEmpty(district)
                || district.matches("[0-9]+")) {

            districtInput.setError("Enter a valid district");
            districtInput.requestFocus();

            return;
        }

        // Save profile
        saveProfile(
                name,
                mobile,
                village,
                district,
                selectedUserType
        );

        // Open correct dashboard
        openDashboard(
                name,
                mobile,
                village,
                district
        );
    }

    private void saveProfile(
            String name,
            String mobile,
            String village,
            String district,
            String userType
    ) {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        preferences.edit()
                .putString("user_name", name)
                .putString("mobile_number", mobile)
                .putString("village", village)
                .putString("district", district)
                .putString("user_type", userType)
                .putBoolean("profile_completed", true)
                .apply();
    }

    private void openDashboard(
            String name,
            String mobile,
            String village,
            String district
    ) {

        Intent intent;

        if ("Expert".equals(selectedUserType)) {

            intent = new Intent(
                    ProfileActivity.this,
                    ExpertDashboardActivity.class
            );

            intent.putExtra("expert_name", name);
            intent.putExtra("mobile_number", mobile);
            intent.putExtra("village", village);
            intent.putExtra("district", district);

        } else {

            intent = new Intent(
                    ProfileActivity.this,
                    FarmerDashboardActivity.class
            );

            intent.putExtra("farmer_name", name);
            intent.putExtra("mobile_number", mobile);
            intent.putExtra("village", village);
            intent.putExtra("district", district);
        }

        startActivity(intent);

        /*
         * Remove ProfileActivity from the current navigation stack.
         *
         * The dashboard becomes the new active screen.
         */
        finish();
    }

}