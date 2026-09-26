package com.example.kisansathi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Locale;

public class ExpertCaseDetailActivity extends AppCompatActivity {

    private TextView caseBackButton;
    private TextView caseIdText;
    private TextView casePriorityText;

    private FrameLayout imageCard;
    private ImageView caseImageView;
    private LinearLayout imagePlaceholder;

    private LinearLayout caseAiResultCard;
    private LinearLayout caseFarmerInfoCard;
    private LinearLayout caseAssessmentCard;
    private LinearLayout caseSubmittedCard;

    private TextView caseAiResultTitle;
    private TextView caseAiConfidence;
    private TextView caseAiDescription;

    private TextView caseFarmerName;
    private TextView caseFarmerVillage;
    private TextView caseFarmerDistrict;

    private TextView confirmDiseaseButton;
    private TextView correctDiseaseButton;
    private TextView unableToIdentifyButton;

    private EditText expertDiseaseInput;
    private EditText expertRemarksInput;

    private TextView submitExpertResultButton;

    private String caseId = "";
    private String imageUri = "";
    private String aiDisease = "";
    private float aiConfidence = 0f;
    private boolean unknownDisease = false;

    private boolean resultSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expert_case_detail);

        initializeViews();
        setupButtons();
        loadCaseData();
    }

    private void initializeViews() {

        caseBackButton =
                findViewById(R.id.caseBackButton);

        caseIdText =
                findViewById(R.id.caseIdText);

        casePriorityText =
                findViewById(R.id.casePriorityText);

        imageCard =
                findViewById(R.id.caseImageCard);

        caseImageView =
                findViewById(R.id.caseImageView);

        imagePlaceholder =
                findViewById(R.id.imagePlaceholder);

        caseAiResultCard =
                findViewById(R.id.caseAiResultCard);

        caseFarmerInfoCard =
                findViewById(R.id.caseFarmerInfoCard);

        caseAssessmentCard =
                findViewById(R.id.caseAssessmentCard);

        caseSubmittedCard =
                findViewById(R.id.caseSubmittedCard);

        caseAiResultTitle =
                findViewById(R.id.caseAiResultTitle);

        caseAiConfidence =
                findViewById(R.id.caseAiConfidence);

        caseAiDescription =
                findViewById(R.id.caseAiDescription);

        caseFarmerName =
                findViewById(R.id.caseFarmerName);

        caseFarmerVillage =
                findViewById(R.id.caseFarmerVillage);

        caseFarmerDistrict =
                findViewById(R.id.caseFarmerDistrict);

        confirmDiseaseButton =
                findViewById(R.id.confirmDiseaseButton);

        correctDiseaseButton =
                findViewById(R.id.correctDiseaseButton);

        unableToIdentifyButton =
                findViewById(R.id.unableToIdentifyButton);

        expertDiseaseInput =
                findViewById(R.id.expertDiseaseInput);

        expertRemarksInput =
                findViewById(R.id.expertRemarksInput);

        submitExpertResultButton =
                findViewById(R.id.submitExpertResultButton);

        caseSubmittedCard.setVisibility(View.GONE);
    }

    private void setupButtons() {

        caseBackButton.setOnClickListener(v -> finish());

        confirmDiseaseButton.setOnClickListener(v -> {

            if (!aiDisease.isEmpty()
                    && !"Disease Not Identified".equalsIgnoreCase(aiDisease)) {

                expertDiseaseInput.setText(aiDisease);

                expertRemarksInput.setText(
                        "AI result reviewed and confirmed based on the visible leaf symptoms."
                );

            } else {

                expertDiseaseInput.setText("");

                expertRemarksInput.setText(
                        "The AI result could not be confirmed. Expert assessment is required."
                );
            }
        });

        correctDiseaseButton.setOnClickListener(v -> {

            expertDiseaseInput.requestFocus();

            expertRemarksInput.setText(
                    "AI result was reviewed and corrected based on the observed leaf symptoms."
            );
        });

        unableToIdentifyButton.setOnClickListener(v -> {

            expertDiseaseInput.setText(
                    "Disease could not be confirmed"
            );

            expertRemarksInput.setText(
                    "Disease could not be confirmed from the available image. Further observation and expert review are recommended."
            );
        });

        submitExpertResultButton.setOnClickListener(
                v -> submitExpertResult()
        );
    }

    private void loadCaseData() {

        caseId =
                getIntent().getStringExtra("case_id");

        if (caseId == null || caseId.trim().isEmpty()) {
            caseId = "KS-CASE-001";
        }

        caseIdText.setText(
                "Case " + caseId
        );

        JSONObject storedCase =
                ExpertCaseStorage.getCaseById(
                        this,
                        caseId
                );

        if (storedCase != null) {

            loadStoredCaseData(storedCase);

        } else {

            loadDemoCaseData();
        }

        loadSubmittedImage();
    }

    private void loadStoredCaseData(
            JSONObject storedCase
    ) {

        try {

            imageUri =
                    storedCase.optString(
                            "image_uri",
                            ""
                    );

            aiDisease =
                    storedCase.optString(
                            "ai_disease",
                            "Disease Not Identified"
                    );

            aiConfidence =
                    (float) storedCase.optDouble(
                            "ai_confidence",
                            0
                    );

            unknownDisease =
                    storedCase.optBoolean(
                            "unknown_disease",
                            false
                    );

            String priority =
                    storedCase.optString(
                            "priority",
                            "NORMAL"
                    );

            String farmerName =
                    storedCase.optString(
                            "farmer_name",
                            "Farmer"
                    );

            String village =
                    storedCase.optString(
                            "village",
                            "Not available"
                    );

            String district =
                    storedCase.optString(
                            "district",
                            "Not available"
                    );

            casePriorityText.setText(
                    priority
            );

            caseFarmerName.setText(
                    farmerName
            );

            caseFarmerVillage.setText(
                    "Village: " + village
            );

            caseFarmerDistrict.setText(
                    "District: " + district
            );

            displayAiResult();

        } catch (Exception e) {

            e.printStackTrace();

            loadDemoCaseData();
        }
    }

    private void loadDemoCaseData() {

        imageUri = "";

        aiDisease =
                "Disease Not Identified";

        aiConfidence = 54.2f;

        unknownDisease = true;

        casePriorityText.setText(
                "HIGH"
        );

        caseFarmerName.setText(
                "Demo Farmer"
        );

        caseFarmerVillage.setText(
                "Village: Demo Village"
        );

        caseFarmerDistrict.setText(
                "District: Demo District"
        );

        displayAiResult();
    }

    private void displayAiResult() {

        caseAiResultTitle.setText(
                aiDisease
        );

        caseAiConfidence.setText(
                String.format(
                        Locale.getDefault(),
                        "AI confidence: %.1f%%",
                        aiConfidence
                )
        );

        if (unknownDisease
                || aiConfidence < 70.0f
                || "Disease Not Identified"
                .equalsIgnoreCase(aiDisease)) {

            caseAiDescription.setText(
                    "The AI result requires expert assessment before a final diagnosis."
            );

        } else {

            caseAiDescription.setText(
                    "This is the preliminary AI assessment. Please review the submitted crop image and validate the result."
            );
        }
    }

    private void loadSubmittedImage() {

        if (imageUri == null
                || imageUri.trim().isEmpty()) {

            showImagePlaceholder();
            return;
        }

        try {

            Uri uri =
                    Uri.parse(imageUri);

            Bitmap bitmap = null;

            /*
             * First try ContentResolver.
             * This handles content:// and file:// URIs.
             */

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

            /*
             * If the URI could not be opened,
             * try the local file path.
             */

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

                caseImageView.setImageBitmap(
                        bitmap
                );

                caseImageView.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

                caseImageView.setVisibility(
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

        caseImageView.setVisibility(
                View.GONE
        );

        imagePlaceholder.setVisibility(
                View.VISIBLE
        );
    }

    private void submitExpertResult() {

        if (resultSubmitted) {
            return;
        }

        String disease =
                expertDiseaseInput
                        .getText()
                        .toString()
                        .trim();

        String remarks =
                expertRemarksInput
                        .getText()
                        .toString()
                        .trim();

        if (disease.isEmpty()) {

            expertDiseaseInput.setError(
                    "Enter the expert assessment"
            );

            expertDiseaseInput.requestFocus();

            return;
        }

        if (remarks.isEmpty()) {

            expertRemarksInput.setError(
                    "Add expert remarks"
            );

            expertRemarksInput.requestFocus();

            return;
        }

        boolean updated =
                ExpertCaseStorage.updateExpertResult(
                        this,
                        caseId,
                        disease,
                        remarks
                );

        if (!updated) {

            Toast.makeText(
                    this,
                    "Unable to update this case",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        resultSubmitted = true;

        caseAssessmentCard.setVisibility(
                View.GONE
        );

        casePriorityText.setText(
                "REVIEWED"
        );

        caseSubmittedCard.setVisibility(
                View.VISIBLE
        );

        Toast.makeText(
                this,
                "Expert Result Submitted",
                Toast.LENGTH_SHORT
        ).show();
    }
}