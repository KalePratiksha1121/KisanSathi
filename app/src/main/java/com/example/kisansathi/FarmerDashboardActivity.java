package com.example.kisansathi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FarmerDashboardActivity extends AppCompatActivity {

    private LinearLayout headerSection;
    private LinearLayout scanDiseaseCard;
    private LinearLayout weatherCard;
    private LinearLayout farmCard;
    private LinearLayout quickActions;

    private LinearLayout myScansCard;
    private LinearLayout followUpCard;

    private TextView greetingText;
    private TextView farmLocationValue;
    private TextView profileIcon;

    private TextView scanCountText;
    private TextView alertCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_farmer_dashboard
        );

        initializeViews();

        loadFarmerData();

        loadDashboardCounts();

        setupClickListeners();

        startEntranceAnimations();
    }

    // ============================================================
    // REFRESH DASHBOARD
    // ============================================================

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Refresh the numbers whenever the farmer returns
         * to the dashboard.
         */
        if (scanCountText != null
                && alertCountText != null) {

            loadDashboardCounts();
        }
    }

    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews() {

        headerSection =
                findViewById(
                        R.id.headerSection
                );

        scanDiseaseCard =
                findViewById(
                        R.id.scanDiseaseCard
                );

        weatherCard =
                findViewById(
                        R.id.weatherCard
                );

        farmCard =
                findViewById(
                        R.id.farmCard
                );

        quickActions =
                findViewById(
                        R.id.quickActions
                );

        myScansCard =
                findViewById(
                        R.id.myScansCard
                );

        followUpCard =
                findViewById(
                        R.id.followUpCard
                );

        greetingText =
                findViewById(
                        R.id.greetingText
                );

        farmLocationValue =
                findViewById(
                        R.id.farmLocationValue
                );

        profileIcon =
                findViewById(
                        R.id.profileIcon
                );

        scanCountText =
                findViewById(
                        R.id.scanCountText
                );

        alertCountText =
                findViewById(
                        R.id.alertCountText
                );
    }

    // ============================================================
    // LOAD FARMER PROFILE
    // ============================================================

    private void loadFarmerData() {

        String farmerName =
                getIntent().getStringExtra(
                        "farmer_name"
                );

        String village =
                getIntent().getStringExtra(
                        "village"
                );

        String district =
                getIntent().getStringExtra(
                        "district"
                );

        /*
         * If dashboard was opened later and no Intent
         * information is available, recover the profile
         * from SharedPreferences.
         */
        if (farmerName == null
                || farmerName.trim().isEmpty()) {

            SharedPreferences preferences =
                    getSharedPreferences(
                            "KisanSathiPrefs",
                            MODE_PRIVATE
                    );

            farmerName =
                    preferences.getString(
                            "user_name",
                            getString(R.string.farmer_default_name)
                    );

            village =
                    preferences.getString(
                            "village",
                            ""
                    );

            district =
                    preferences.getString(
                            "district",
                            ""
                    );
        }

        // --------------------------------------------------------
        // GREETING
        // --------------------------------------------------------

        if (farmerName != null
                && !farmerName.trim().isEmpty()) {

            greetingText.setText(
                    getString(
                            R.string.farmer_greeting,
                            farmerName.trim()
                    )
            );
        }

        // --------------------------------------------------------
        // FARM LOCATION
        // --------------------------------------------------------

        if (village != null
                && district != null
                && !village.trim().isEmpty()
                && !district.trim().isEmpty()) {

            farmLocationValue.setText(
                    village.trim()
                            + " • "
                            + district.trim()
            );
        }
    }

    // ============================================================
    // LOAD FARMER-SPECIFIC COUNTS
    // ============================================================

    private void loadDashboardCounts() {

        /*
         * The farmer dashboard only sees cases belonging
         * to the currently logged-in farmer.
         */

        int totalScans =
                ExpertCaseStorage
                        .getCurrentFarmerCaseCount(
                                this
                        );

        int reviewedCases =
                ExpertCaseStorage
                        .getCurrentFarmerAlertCount(
                                this
                        );

        scanCountText.setText(
                String.valueOf(totalScans)
        );

        alertCountText.setText(
                String.valueOf(reviewedCases)
        );
    }

    // ============================================================
    // CLICK LISTENERS
    // ============================================================

    private void setupClickListeners() {

        // --------------------------------------------------------
        // PROFILE
        // --------------------------------------------------------

        profileIcon.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            FarmerDashboardActivity.this,
                            FarmerProfileActivity.class
                    );

            startActivity(intent);
        });

        // --------------------------------------------------------
        // SCAN CROP
        // --------------------------------------------------------

        scanDiseaseCard.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            FarmerDashboardActivity.this,
                            ScanCropActivity.class
                    );

            startActivity(intent);
        });

        // --------------------------------------------------------
        // MY SCANS
        // --------------------------------------------------------

        myScansCard.setOnClickListener(v -> {

            openFarmerCases();
        });

        // --------------------------------------------------------
        // ALERT COUNT
        // --------------------------------------------------------

        alertCountText.setOnClickListener(v -> {

            openFarmerCases();
        });

        /*
         * Also make the parent of the alert count clickable.
         */
        View alertParent =
                (View) alertCountText.getParent();

        if (alertParent != null) {

            alertParent.setOnClickListener(
                    v -> openFarmerCases()
            );
        }

        // --------------------------------------------------------
        // FOLLOW-UP / CROP PROGRESS
        // --------------------------------------------------------

        followUpCard.setOnClickListener(v -> {

            openFarmerCases();
        });
    }

    // ============================================================
    // OPEN FARMER CASES
    // ============================================================

    private void openFarmerCases() {

        Intent intent =
                new Intent(
                        FarmerDashboardActivity.this,
                        FarmerCasesActivity.class
                );

        startActivity(intent);
    }

    // ============================================================
    // ENTRANCE ANIMATION
    // ============================================================

    private void startEntranceAnimations() {

        headerSection.setAlpha(0f);
        headerSection.setTranslationY(-25f);

        scanDiseaseCard.setAlpha(0f);
        scanDiseaseCard.setTranslationY(35f);

        weatherCard.setAlpha(0f);
        weatherCard.setTranslationY(35f);

        farmCard.setAlpha(0f);
        farmCard.setTranslationY(35f);

        quickActions.setAlpha(0f);
        quickActions.setTranslationY(35f);

        animateView(
                headerSection,
                0,
                450
        );

        animateView(
                scanDiseaseCard,
                100,
                550
        );

        animateView(
                weatherCard,
                200,
                550
        );

        animateView(
                farmCard,
                300,
                550
        );

        animateView(
                quickActions,
                400,
                550
        );
    }

    // ============================================================
    // ANIMATE VIEW
    // ============================================================

    private void animateView(
            View view,
            long delay,
            long duration
    ) {

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(duration)
                .setInterpolator(
                        new AccelerateDecelerateInterpolator()
                )
                .start();
    }
}