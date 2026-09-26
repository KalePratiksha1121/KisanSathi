package com.example.kisansathi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Locale;

public class FarmerCaseDetailActivity
        extends AppCompatActivity {

    private TextView backButton;
    private TextView caseIdText;
    private TextView statusText;

    private ImageView cropImageView;
    private LinearLayout imagePlaceholder;

    private TextView aiDiseaseText;
    private TextView aiConfidenceText;

    private TextView expertDiseaseText;
    private TextView expertRemarksText;

    private LinearLayout expertResultCard;
    private TextView progressButton;

    private String caseId = "";

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_farmer_case_detail
        );

        initializeViews();
        setupListeners();
        loadCase();
    }

    private void initializeViews() {

        backButton =
                findViewById(
                        R.id.farmerCaseBackButton
                );

        caseIdText =
                findViewById(
                        R.id.farmerCaseIdText
                );

        statusText =
                findViewById(
                        R.id.farmerCaseStatusText
                );

        cropImageView =
                findViewById(
                        R.id.farmerCropImage
                );

        imagePlaceholder =
                findViewById(
                        R.id.farmerImagePlaceholder
                );

        aiDiseaseText =
                findViewById(
                        R.id.farmerAiDisease
                );

        aiConfidenceText =
                findViewById(
                        R.id.farmerAiConfidence
                );

        expertResultCard =
                findViewById(
                        R.id.farmerExpertResultCard
                );

        expertDiseaseText =
                findViewById(
                        R.id.farmerExpertDisease
                );

        expertRemarksText =
                findViewById(
                        R.id.farmerExpertRemarks
                );

        progressButton =
                findViewById(
                        R.id.farmerProgressButton
                );
    }

    private void setupListeners() {

        backButton.setOnClickListener(
                v -> finish()
        );

        progressButton.setOnClickListener(
                v -> {

                    /*
                     * Continuous crop tracking will be
                     * connected here in the next stage.
                     */
                    android.widget.Toast.makeText(
                            this,
                            "Crop progress tracking will open here.",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    private void loadCase() {

        caseId =
                getIntent().getStringExtra(
                        "case_id"
                );

        if (caseId == null
                || caseId.trim().isEmpty()) {

            finish();

            return;
        }

        JSONObject currentCase =
                ExpertCaseStorage.getCaseById(
                        this,
                        caseId
                );

        if (currentCase == null) {

            finish();

            return;
        }

        caseIdText.setText(
                "Case " + caseId
        );

        String status =
                currentCase.optString(
                        "status",
                        "PENDING"
                );

        String imageUri =
                currentCase.optString(
                        "image_uri",
                        ""
                );

        String aiDisease =
                currentCase.optString(
                        "ai_disease",
                        "Disease Not Identified"
                );

        float aiConfidence =
                (float) currentCase.optDouble(
                        "ai_confidence",
                        0
                );

        String expertDisease =
                currentCase.optString(
                        "expert_disease",
                        ""
                );

        String expertRemarks =
                currentCase.optString(
                        "expert_remarks",
                        ""
                );

        aiDiseaseText.setText(
                aiDisease
        );

        aiConfidenceText.setText(
                String.format(
                        Locale.getDefault(),
                        "AI confidence: %.1f%%",
                        aiConfidence
                )
        );

        loadImage(imageUri);

        if ("REVIEWED".equals(status)) {

            statusText.setText(
                    "✓  EXPERT REVIEW COMPLETED"
            );

            expertResultCard.setVisibility(
                    View.VISIBLE
            );

            expertDiseaseText.setText(
                    expertDisease.isEmpty()
                            ? "Assessment provided"
                            : expertDisease
            );

            expertRemarksText.setText(
                    expertRemarks.isEmpty()
                            ? "No remarks provided."
                            : expertRemarks
            );

            /*
             * Opening this screen marks the alert
             * as read.
             */
            ExpertCaseStorage.markCaseAsViewed(
                    this,
                    caseId
            );

        } else {

            statusText.setText(
                    "⏳  WAITING FOR EXPERT REVIEW"
            );

            expertResultCard.setVisibility(
                    View.GONE
            );
        }
    }

    private void loadImage(
            String imageUri
    ) {

        if (imageUri == null
                || imageUri.trim().isEmpty()) {

            showImagePlaceholder();

            return;
        }

        try {

            Uri uri =
                    Uri.parse(imageUri);

            Bitmap bitmap = null;

            try {

                InputStream inputStream =
                        getContentResolver()
                                .openInputStream(uri);

                if (inputStream != null) {

                    bitmap =
                            BitmapFactory.decodeStream(
                                    inputStream
                            );

                    inputStream.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            if (bitmap == null) {

                String path =
                        uri.getPath();

                if (path != null) {

                    bitmap =
                            BitmapFactory.decodeFile(
                                    path
                            );
                }
            }

            if (bitmap != null) {

                cropImageView.setImageBitmap(
                        bitmap
                );

                cropImageView.setVisibility(
                        View.VISIBLE
                );

                imagePlaceholder.setVisibility(
                        View.GONE
                );

            } else {

                showImagePlaceholder();
            }

        } catch (Exception e) {

            e.printStackTrace();

            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {

        cropImageView.setVisibility(
                View.GONE
        );

        imagePlaceholder.setVisibility(
                View.VISIBLE
        );
    }
}