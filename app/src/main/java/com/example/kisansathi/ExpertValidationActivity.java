package com.example.kisansathi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Locale;

public class ExpertValidationActivity extends AppCompatActivity {

    private ImageView expertCropImage;

    private LinearLayout imageCard;
    private LinearLayout resultCard;
    private LinearLayout statusCard;

    private TextView expertBackButton;

    private TextView resultTitle;
    private TextView resultConfidence;
    private TextView resultDescription;

    private TextView validationStatus;
    private TextView validationDescription;

    private TextView sendForReviewButton;

    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_expert_validation);

        initializeViews();
        loadDetectionData();
        setupClickListeners();
        startEntranceAnimation();
    }

    private void initializeViews() {

        expertCropImage =
                findViewById(R.id.expertCropImage);

        imageCard = findViewById(R.id.expertImageCard);

        resultCard =
                findViewById(R.id.expertResultCard);

        statusCard =
                findViewById(R.id.expertStatusCard);

        expertBackButton =
                findViewById(R.id.expertBackButton);

        resultTitle =
                findViewById(R.id.expertResultTitle);

        resultConfidence =
                findViewById(R.id.expertResultConfidence);

        resultDescription =
                findViewById(R.id.expertResultDescription);

        validationStatus =
                findViewById(R.id.validationStatus);

        validationDescription =
                findViewById(R.id.validationDescription);

        sendForReviewButton =
                findViewById(R.id.sendForReviewButton);
    }

    private void loadDetectionData() {

        String imageUriString =
                getIntent().getStringExtra(
                        "captured_image_uri"
                );

        String disease =
                getIntent().getStringExtra(
                        "disease"
                );

        float confidence =
                getIntent().getFloatExtra(
                        "confidence",
                        0f
                );

        boolean unknownDisease =
                getIntent().getBooleanExtra(
                        "unknown_disease",
                        false
                );

        /*
         * Load captured image.
         */
        if (imageUriString != null
                && !imageUriString.trim().isEmpty()) {

            try {

                Uri imageUri =
                        Uri.parse(imageUriString);

                String imagePath =
                        imageUri.getPath();

                if (imagePath != null) {

                    File imageFile =
                            new File(imagePath);

                    if (imageFile.exists()) {

                        capturedBitmap =
                                BitmapFactory.decodeFile(
                                        imageFile.getAbsolutePath()
                                );

                        if (capturedBitmap != null) {

                            expertCropImage.setImageBitmap(
                                    capturedBitmap
                            );
                        }
                    }
                }

            } catch (Exception e) {

                android.util.Log.e(
                        "ExpertValidation",
                        "Could not load captured image",
                        e
                );
            }
        }

        /*
         * Display AI result.
         */
        if (unknownDisease) {

            resultTitle.setText(
                    "Disease Not Identified"
            );

            resultConfidence.setText(
                    String.format(
                            Locale.US,
                            "Model confidence: %.1f%%",
                            confidence
                    )
            );

            resultDescription.setText(
                    "No known disease could be confidently "
                            + "identified from this image. "
                            + "Expert review is required."
            );

        } else {

            if (disease == null
                    || disease.trim().isEmpty()) {

                disease =
                        "Disease Not Identified";
            }

            resultTitle.setText(
                    disease
            );

            resultConfidence.setText(
                    String.format(
                            Locale.US,
                            "Model confidence: %.1f%%",
                            confidence
                    )
            );

            resultDescription.setText(
                    "This is a preliminary AI result. "
                            + "An expert must review the case "
                            + "before the result is confirmed."
            );
        }

        /*
         * Initial validation status.
         */
        validationStatus.setText(
                "Pending Expert Review"
        );

        validationDescription.setText(
                "Your crop image and preliminary AI result "
                        + "are ready to be reviewed by an expert."
        );

        sendForReviewButton.setText(
                "Send for Expert Review   →"
        );
    }

    private void setupClickListeners() {

        expertBackButton.setOnClickListener(v ->
                finish()
        );

        sendForReviewButton.setOnClickListener(v -> {

            /*
             * MVP status update.
             *
             * Backend/expert synchronization will be connected
             * later. For the current MVP, we show the case as
             * successfully queued for expert review.
             */

            validationStatus.setText(
                    "Sent for Expert Review"
            );

            validationDescription.setText(
                    "Your case has been marked for expert review. "
                            + "The expert will validate the preliminary "
                            + "AI result."
            );

            sendForReviewButton.setText(
                    "✓  Sent for Expert Review"
            );

            sendForReviewButton.setEnabled(
                    false
            );

            sendForReviewButton.setAlpha(
                    0.75f
            );

            statusCard.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start();
        });
    }

    private void startEntranceAnimation() {

        imageCard.setAlpha(0f);
        imageCard.setTranslationY(30f);

        resultCard.setAlpha(0f);
        resultCard.setTranslationY(35f);

        statusCard.setAlpha(0f);
        statusCard.setTranslationY(35f);

        sendForReviewButton.setAlpha(0f);
        sendForReviewButton.setTranslationY(35f);

        animateView(
                imageCard,
                80,
                450
        );

        animateView(
                resultCard,
                180,
                450
        );

        animateView(
                statusCard,
                280,
                450
        );

        animateView(
                sendForReviewButton,
                380,
                450
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

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (capturedBitmap != null
                && !capturedBitmap.isRecycled()) {

            capturedBitmap.recycle();
        }
    }
}
