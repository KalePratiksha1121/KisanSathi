package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    private EditText otpInput;
    private TextView verifyOtpButton;
    private TextView resendOtpButton;

    private FirebaseAuth mAuth;

    private String verificationId;
    private String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp);

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);

        mAuth = FirebaseAuth.getInstance();

        mobileNumber = getIntent().getStringExtra("mobile_number");
        verificationId = getIntent().getStringExtra("verification_id");

        boolean autoVerified =
                getIntent().getBooleanExtra("auto_verified", false);

        // If Firebase already verified the user automatically,
        // continue directly.
        if (autoVerified) {
            goToProfile();
            return;
        }

        verifyOtpButton.setOnClickListener(v -> verifyOtp());

        resendOtpButton.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {

        String code = otpInput
                .getText()
                .toString()
                .trim();

        if (TextUtils.isEmpty(code)) {

            Toast.makeText(
                    this,
                    getString(R.string.enter_otp_error),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (code.length() != 6) {

            Toast.makeText(
                    this,
                    getString(R.string.enter_valid_otp),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(verificationId)) {

            Toast.makeText(
                    this,
                    getString(R.string.otp_session_expired),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        verifyOtpButton.setEnabled(false);
        verifyOtpButton.setAlpha(0.6f);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId,
                        code
                );

        signInWithCredential(credential);
    }

    private void signInWithCredential(
            PhoneAuthCredential credential
    ) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                getString(R.string.phone_verified),
                                Toast.LENGTH_SHORT
                        ).show();

                        goToProfile();

                    } else {

                        verifyOtpButton.setEnabled(true);
                        verifyOtpButton.setAlpha(1.0f);

                        Toast.makeText(
                                this,
                                getString(R.string.invalid_otp),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void resendOtp() {

        Toast.makeText(
                this,
                getString(R.string.resend_otp_later),
                Toast.LENGTH_SHORT
        ).show();

        /*
         * We will connect the Firebase ForceResendingToken
         * in the next improvement after the first real OTP
         * flow is successfully tested.
         */
    }

    private void goToProfile() {

        Intent intent = new Intent(
                OtpActivity.this,
                ProfileActivity.class
        );

        intent.putExtra(
                "mobile_number",
                mobileNumber
        );

        startActivity(intent);

        finish();
    }
}