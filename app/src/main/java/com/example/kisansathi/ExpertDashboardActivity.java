package com.example.kisansathi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class ExpertDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "KisanSathiPrefs";

    private LinearLayout headerSection;
    private LinearLayout pendingCasesCard;
    private LinearLayout priorityCard;
    private LinearLayout recentCasesSection;

    private TextView greetingText;
    private TextView expertNameText;
    private TextView pendingCountText;
    private TextView priorityCountText;
    private TextView profileIcon;

    private TextView caseCardOne;
    private TextView caseCardTwo;

    private String caseCardOneId;
    private String caseCardTwoId;
    private String firstPendingCaseId;
    private String firstPriorityCaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expert_dashboard);

        initializeViews();
        setupClickListeners();
        loadExpertData();
        startEntranceAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pendingCountText != null) {
            loadExpertData();
        }
    }

    private void initializeViews() {

        headerSection =
                findViewById(R.id.expertHeaderSection);

        pendingCasesCard =
                findViewById(R.id.pendingCasesCard);

        priorityCard =
                findViewById(R.id.priorityCard);

        recentCasesSection =
                findViewById(R.id.recentCasesSection);

        greetingText =
                findViewById(R.id.expertGreetingText);

        expertNameText =
                findViewById(R.id.expertNameText);

        pendingCountText =
                findViewById(R.id.pendingCountText);

        priorityCountText =
                findViewById(R.id.priorityCountText);

        profileIcon =
                findViewById(R.id.expertProfileIcon);

        caseCardOne =
                findViewById(R.id.expertCaseCardOne);

        caseCardTwo =
                findViewById(R.id.expertCaseCardTwo);
    }

    private void loadExpertData() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        String expertName =
                preferences.getString(
                        "user_name",
                        "Expert"
                );

        if (expertName == null
                || expertName.trim().isEmpty()) {

            expertName = "Expert";
        }

        expertName = expertName.trim();

        expertNameText.setText(expertName);

        greetingText.setText(
                "Hello, " + expertName + " 👋"
        );

        JSONArray cases =
                ExpertCaseStorage.getAllCases(this);

        int pendingCount = 0;
        int priorityCount = 0;

        firstPendingCaseId = null;
        firstPriorityCaseId = null;

        caseCardOneId = null;
        caseCardTwoId = null;

        JSONObject firstRecentCase = null;
        JSONObject secondRecentCase = null;

        for (int i = cases.length() - 1; i >= 0; i--) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String status =
                        currentCase.optString(
                                "status",
                                "PENDING"
                        );

                String priority =
                        currentCase.optString(
                                "priority",
                                "NORMAL"
                        );

                String caseId =
                        currentCase.optString(
                                "case_id",
                                ""
                        );

                if ("PENDING".equals(status)) {

                    pendingCount++;

                    if (firstPendingCaseId == null
                            && !caseId.isEmpty()) {

                        firstPendingCaseId = caseId;
                    }

                    if ("HIGH".equals(priority)) {

                        priorityCount++;

                        if (firstPriorityCaseId == null
                                && !caseId.isEmpty()) {

                            firstPriorityCaseId = caseId;
                        }
                    }

                    if (firstRecentCase == null) {

                        firstRecentCase =
                                currentCase;

                    } else if (secondRecentCase == null) {

                        secondRecentCase =
                                currentCase;
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        pendingCountText.setText(
                String.valueOf(pendingCount)
        );

        priorityCountText.setText(
                String.valueOf(priorityCount)
        );

        updateRecentCaseCards(
                firstRecentCase,
                secondRecentCase
        );
    }

    private void updateRecentCaseCards(
            JSONObject firstCase,
            JSONObject secondCase
    ) {

        if (firstCase != null) {

            caseCardOneId =
                    firstCase.optString(
                            "case_id",
                            ""
                    );

            String farmerName =
                    firstCase.optString(
                            "farmer_name",
                            "Farmer"
                    );

            String disease =
                    firstCase.optString(
                            "ai_disease",
                            "Disease Not Identified"
                    );

            float confidence =
                    (float) firstCase.optDouble(
                            "ai_confidence",
                            0.0
                    );

            String priority =
                    firstCase.optString(
                            "priority",
                            "NORMAL"
                    );

            caseCardOne.setText(
                    buildCaseCardText(
                            caseCardOneId,
                            farmerName,
                            disease,
                            confidence,
                            priority
                    )
            );

            caseCardOne.setEnabled(true);
            caseCardOne.setAlpha(1f);

        } else {

            caseCardOneId = null;

            caseCardOne.setText(
                    "No pending cases yet\n" +
                            "New farmer submissions will appear here."
            );

            caseCardOne.setEnabled(false);
            caseCardOne.setAlpha(0.65f);
        }

        if (secondCase != null) {

            caseCardTwoId =
                    secondCase.optString(
                            "case_id",
                            ""
                    );

            String farmerName =
                    secondCase.optString(
                            "farmer_name",
                            "Farmer"
                    );

            String disease =
                    secondCase.optString(
                            "ai_disease",
                            "Disease Not Identified"
                    );

            float confidence =
                    (float) secondCase.optDouble(
                            "ai_confidence",
                            0.0
                    );

            String priority =
                    secondCase.optString(
                            "priority",
                            "NORMAL"
                    );

            caseCardTwo.setText(
                    buildCaseCardText(
                            caseCardTwoId,
                            farmerName,
                            disease,
                            confidence,
                            priority
                    )
            );

            caseCardTwo.setEnabled(true);
            caseCardTwo.setAlpha(1f);

        } else {

            caseCardTwoId = null;

            caseCardTwo.setText(
                    "No second pending case\n" +
                            "Waiting for another farmer submission."
            );

            caseCardTwo.setEnabled(false);
            caseCardTwo.setAlpha(0.65f);
        }
    }

    private String buildCaseCardText(
            String caseId,
            String farmerName,
            String disease,
            float confidence,
            String priority
    ) {

        String priorityLabel;

        if ("HIGH".equals(priority)) {

            priorityLabel = "⚠ HIGH PRIORITY";

        } else {

            priorityLabel = "● NORMAL";
        }

        return priorityLabel
                + "\n"
                + farmerName
                + "  •  "
                + caseId
                + "\n"
                + disease
                + "\n"
                + String.format(
                Locale.US,
                "AI confidence: %.1f%%",
                confidence
        );
    }

    private void setupClickListeners() {

        profileIcon.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ExpertDashboardActivity.this,
                            ExpertProfileActivity.class
                    );

            startActivity(intent);
        });

        pendingCasesCard.setOnClickListener(v -> {

            if (firstPendingCaseId == null) {
                return;
            }

            openCase(
                    firstPendingCaseId,
                    "pending"
            );
        });

        priorityCard.setOnClickListener(v -> {

            if (firstPriorityCaseId == null) {
                return;
            }

            openCase(
                    firstPriorityCaseId,
                    "priority"
            );
        });

        caseCardOne.setOnClickListener(v -> {

            if (caseCardOneId == null
                    || caseCardOneId.trim().isEmpty()) {

                return;
            }

            openCase(
                    caseCardOneId,
                    "pending"
            );
        });

        caseCardTwo.setOnClickListener(v -> {

            if (caseCardTwoId == null
                    || caseCardTwoId.trim().isEmpty()) {

                return;
            }

            openCase(
                    caseCardTwoId,
                    "pending"
            );
        });
    }

    private void openCase(
            String caseId,
            String caseType
    ) {

        Intent intent =
                new Intent(
                        ExpertDashboardActivity.this,
                        ExpertCaseDetailActivity.class
                );

        intent.putExtra(
                "case_id",
                caseId
        );

        intent.putExtra(
                "case_type",
                caseType
        );

        startActivity(intent);
    }

    private void startEntranceAnimations() {

        headerSection.setAlpha(0f);
        headerSection.setTranslationY(-25f);

        pendingCasesCard.setAlpha(0f);
        pendingCasesCard.setTranslationY(30f);

        priorityCard.setAlpha(0f);
        priorityCard.setTranslationY(30f);

        recentCasesSection.setAlpha(0f);
        recentCasesSection.setTranslationY(30f);

        animateView(
                headerSection,
                0,
                450
        );

        animateView(
                pendingCasesCard,
                100,
                500
        );

        animateView(
                priorityCard,
                200,
                500
        );

        animateView(
                recentCasesSection,
                300,
                550
        );
    }

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