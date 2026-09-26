package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MobileNumberActivity extends AppCompatActivity {

    private EditText mobileInput;
    private TextView mobileError;
    private TextView sendOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mobile_number);

        mobileInput =
                findViewById(R.id.mobileInput);

        mobileError =
                findViewById(R.id.mobileError);

        sendOtpButton =
                findViewById(R.id.sendOtpButton);

        sendOtpButton.setOnClickListener(
                v -> continueToProfile()
        );
    }

    private void continueToProfile() {

        String mobileNumber =
                mobileInput.getText()
                        .toString()
                        .trim();

        mobileError.setVisibility(
                TextView.GONE
        );

        // --------------------------------
        // EMPTY NUMBER
        // --------------------------------

        if (TextUtils.isEmpty(mobileNumber)) {

            showError(
                    getString(R.string.invalid_mobile)
            );

            return;
        }

        // --------------------------------
        // INDIAN MOBILE NUMBER
        // --------------------------------

        if (!mobileNumber.matches(
                "[6-9][0-9]{9}"
        )) {

            showError(
                    getString(R.string.invalid_mobile)
            );

            return;
        }

        // --------------------------------
        // OPEN PROFILE
        // --------------------------------

        Intent intent =
                new Intent(
                        MobileNumberActivity.this,
                        ProfileActivity.class
                );

        intent.putExtra(
                "mobile_number",
                mobileNumber
        );

        startActivity(intent);

        /*
         * We intentionally do NOT call finish().
         *
         * This allows:
         *
         * Mobile Number
         *       ↓
         * Profile
         *       ↓
         * Dashboard
         *
         * The dashboard is then placed above the
         * previous activities using CLEAR_TOP.
         */
    }

    private void showError(String message) {

        mobileError.setText(message);

        mobileError.setVisibility(
                TextView.VISIBLE
        );
    }
}