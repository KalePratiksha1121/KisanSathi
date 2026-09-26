package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class FarmerCasesActivity extends AppCompatActivity {

    private LinearLayout casesContainer;
    private TextView backButton;
    private TextView casesSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_farmer_cases
        );

        initializeViews();
        setupListeners();
        loadCases();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (casesContainer != null) {
            loadCases();
        }
    }

    private void initializeViews() {

        backButton =
                findViewById(
                        R.id.farmerCasesBackButton
                );

        casesSummary =
                findViewById(
                        R.id.farmerCasesSummary
                );

        casesContainer =
                findViewById(
                        R.id.casesContainer
                );
    }

    private void setupListeners() {

        backButton.setOnClickListener(
                v -> finish()
        );
    }

    private void loadCases() {

        casesContainer.removeAllViews();

        JSONArray cases =
                ExpertCaseStorage.getAllCases(
                        this
                );

        int totalCases =
                cases.length();

        int reviewedCases = 0;
        int pendingCases = 0;

        for (int i = 0;
             i < cases.length();
             i++) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                String status =
                        currentCase.optString(
                                "status",
                                "PENDING"
                        );

                if ("REVIEWED".equals(status)) {
                    reviewedCases++;
                } else {
                    pendingCases++;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        casesSummary.setText(
                totalCases
                        + " case"
                        + (totalCases == 1 ? "" : "s")
                        + "  •  "
                        + reviewedCases
                        + " reviewed  •  "
                        + pendingCases
                        + " pending"
        );

        if (cases.length() == 0) {

            showEmptyState();

            return;
        }

        /*
         * Newest cases first.
         */
        for (int i = cases.length() - 1;
             i >= 0;
             i--) {

            try {

                JSONObject currentCase =
                        cases.getJSONObject(i);

                addCaseCard(currentCase);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private void addCaseCard(
            JSONObject currentCase
    ) {

        String caseId =
                currentCase.optString(
                        "case_id",
                        "Unknown"
                );

        String disease =
                currentCase.optString(
                        "ai_disease",
                        "Disease Not Identified"
                );

        float confidence =
                (float) currentCase.optDouble(
                        "ai_confidence",
                        0
                );

        String status =
                currentCase.optString(
                        "status",
                        "PENDING"
                );

        String expertDisease =
                currentCase.optString(
                        "expert_disease",
                        ""
                );

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                18,
                17,
                18,
                17
        );

        card.setBackgroundResource(
                R.drawable.language_card
        );

        card.setElevation(3f);

        LinearLayout.LayoutParams
                cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        card.setLayoutParams(
                cardParams
        );

        TextView caseIdText =
                createTextView(
                        caseId,
                        16,
                        true
                );

        card.addView(
                caseIdText
        );

        TextView aiText =
                createTextView(
                        "AI: " + disease,
                        13,
                        false
                );

        LinearLayout.LayoutParams
                aiParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        aiParams.setMargins(
                0,
                7,
                0,
                0
        );

        aiText.setLayoutParams(
                aiParams
        );

        card.addView(
                aiText
        );

        TextView confidenceText =
                createTextView(
                        String.format(
                                java.util.Locale.getDefault(),
                                "AI confidence: %.1f%%",
                                confidence
                        ),
                        12,
                        false
                );

        LinearLayout.LayoutParams
                confidenceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        confidenceParams.setMargins(
                0,
                4,
                0,
                0
        );

        confidenceText.setLayoutParams(
                confidenceParams
        );

        card.addView(
                confidenceText
        );

        TextView statusText;

        if ("REVIEWED".equals(status)) {

            String result =
                    expertDisease.isEmpty()
                            ? "Expert result available"
                            : "Expert: " + expertDisease;

            statusText =
                    createTextView(
                            "✓  EXPERT REVIEWED\n" + result,
                            13,
                            true
                    );

        } else {

            statusText =
                    createTextView(
                            "⏳  WAITING FOR EXPERT REVIEW",
                            13,
                            true
                    );
        }

        LinearLayout.LayoutParams
                statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.setMargins(
                0,
                12,
                0,
                0
        );

        statusText.setLayoutParams(
                statusParams
        );

        card.addView(
                statusText
        );

        TextView openText =
                createTextView(
                        "View Case  →",
                        12,
                        true
                );

        openText.setGravity(
                Gravity.END
        );

        LinearLayout.LayoutParams
                openParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        openParams.setMargins(
                0,
                13,
                0,
                0
        );

        openText.setLayoutParams(
                openParams
        );

        card.addView(
                openText
        );

        card.setOnClickListener(
                v -> openCase(caseId)
        );

        casesContainer.addView(card);
    }

    private TextView createTextView(
            String text,
            int size,
            boolean bold
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);

        textView.setTextColor(
                getColor(
                        R.color.dark_green
                )
        );

        textView.setTextSize(size);

        if (bold) {

            textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return textView;
    }

    private void showEmptyState() {

        TextView emptyText =
                createTextView(
                        "📋\n\nNo crop cases yet.\n\nScan a crop to create your first case.",
                        15,
                        false
                );

        emptyText.setGravity(
                Gravity.CENTER
        );

        emptyText.setPadding(
                25,
                50,
                25,
                50
        );

        casesContainer.addView(
                emptyText
        );
    }

    private void openCase(
            String caseId
    ) {

        Intent intent =
                new Intent(
                        FarmerCasesActivity.this,
                        FarmerCaseDetailActivity.class
                );

        intent.putExtra(
                "case_id",
                caseId
        );

        startActivity(intent);
    }
}