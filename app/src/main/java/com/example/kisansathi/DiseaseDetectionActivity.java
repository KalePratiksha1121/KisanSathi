package com.example.kisansathi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class DiseaseDetectionActivity extends AppCompatActivity {

    private static final String MODEL_FILE =
            "kisan_sathi_disease_model.tflite";

    private static final int IMAGE_SIZE = 224;

    /*
     * Below this confidence, the result is treated as
     * "Disease Not Identified" and sent for expert validation.
     */
    private static final float CONFIDENCE_THRESHOLD = 70.0f;

    private ImageView diseaseCropImage;

    private FrameLayout diseaseImageCard;

    private LinearLayout diseaseOverlay;
    private LinearLayout diseaseResultCard;
    private LinearLayout diseaseJourneyCard;

    private ProgressBar diseaseProgress;
    private ProgressBar diseaseConfidenceBar;

    private TextView diseaseBackButton;
    private TextView diseaseProcessingText;

    private TextView diseaseIcon;
    private TextView diseaseTitle;
    private TextView diseaseConfidence;
    private TextView diseaseDescription;

    private TextView diseaseStatusStep;
    private TextView diseaseResultStep;
    private TextView expertValidationStep;

    private TextView continueToExpertButton;

    private Handler handler;

    private Bitmap capturedBitmap;
    private Interpreter interpreter;

    /*
     * Original image URI/path passed from ScanCropActivity.
     * This is saved with the expert case.
     */
    private String capturedImageUri;

    /*
     * Stores the final AI result shown on screen.
     * These values are used when creating the expert case.
     */
    private String finalDiseaseResult = "Disease Not Identified";
    private float finalConfidence = 0.0f;
    private boolean finalResultIsUnknown = true;

    /*
     * Prevents accidental double submission if the farmer
     * taps the expert-validation button multiple times.
     */
    private boolean caseSubmitted = false;

    private static final String[] DISEASE_LABELS = {
            "Corn Common Rust",
            "Corn Healthy",
            "Corn Northern Leaf Blight",
            "Potato Early Blight",
            "Potato Healthy",
            "Potato Late Blight",
            "Tomato Early Blight",
            "Tomato Healthy",
            "Tomato Late Blight"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_disease_detection);

        handler = new Handler(Looper.getMainLooper());

        initializeViews();
        setupInitialState();
        setupClickListeners();

        loadTFLiteModel();
        loadCapturedImage();

        startEntranceAnimation();
    }

    private void initializeViews() {

        diseaseCropImage =
                findViewById(R.id.diseaseCropImage);

        diseaseImageCard =
                findViewById(R.id.diseaseImageCard);

        diseaseOverlay =
                findViewById(R.id.diseaseOverlay);

        diseaseResultCard =
                findViewById(R.id.diseaseResultCard);

        diseaseJourneyCard =
                findViewById(R.id.diseaseJourneyCard);

        diseaseProgress =
                findViewById(R.id.diseaseProgress);

        diseaseConfidenceBar =
                findViewById(R.id.diseaseConfidenceBar);

        diseaseBackButton =
                findViewById(R.id.diseaseBackButton);

        diseaseProcessingText =
                findViewById(R.id.diseaseProcessingText);

        diseaseIcon =
                findViewById(R.id.diseaseIcon);

        diseaseTitle =
                findViewById(R.id.diseaseTitle);

        diseaseConfidence =
                findViewById(R.id.diseaseConfidence);

        diseaseDescription =
                findViewById(R.id.diseaseDescription);

        diseaseStatusStep =
                findViewById(R.id.diseaseStatusStep);

        diseaseResultStep =
                findViewById(R.id.diseaseResultStep);

        expertValidationStep =
                findViewById(R.id.expertValidationStep);

        continueToExpertButton =
                findViewById(R.id.continueToExpertButton);
    }

    private void setupInitialState() {

        diseaseResultCard.setAlpha(0f);
        diseaseJourneyCard.setAlpha(0f);
        continueToExpertButton.setAlpha(0f);

        diseaseConfidenceBar.setProgress(0);

        diseaseConfidence.setText("--");

        diseaseTitle.setText("Analyzing...");

        diseaseDescription.setText(
                "The AI model is analyzing your crop leaf."
        );
    }

    private void setupClickListeners() {

        diseaseBackButton.setOnClickListener(v -> finish());

        continueToExpertButton.setOnClickListener(
                v -> submitCaseForExpertValidation()
        );
    }

    /*
     * Creates the local expert-review case.
     *
     * This connects:
     *
     * Farmer Scan
     *      ↓
     * Disease Detection
     *      ↓
     * ExpertCaseStorage
     *      ↓
     * Expert Dashboard
     */
    private void submitCaseForExpertValidation() {

        if (caseSubmitted) {
            return;
        }

        /*
         * Make sure we still have the captured image.
         */
        if (capturedImageUri == null
                || capturedImageUri.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Captured image is unavailable.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Disable the button immediately.
         * This prevents duplicate expert cases.
         */
        caseSubmitted = true;

        continueToExpertButton.setEnabled(false);

        continueToExpertButton.setAlpha(0.7f);

        String caseId =
                ExpertCaseStorage.createCase(
                        DiseaseDetectionActivity.this,
                        capturedImageUri,
                        finalDiseaseResult,
                        finalConfidence,
                        finalResultIsUnknown
                );

        if (caseId == null || caseId.trim().isEmpty()) {

            caseSubmitted = false;

            continueToExpertButton.setEnabled(true);

            continueToExpertButton.setAlpha(1f);

            Toast.makeText(
                    this,
                    "Unable to create expert review case.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        /*
         * The case is successfully stored locally.
         */
        Toast.makeText(
                this,
                "Case " + caseId
                        + " sent for expert validation.",
                Toast.LENGTH_LONG
        ).show();

        /*
         * Return the farmer to the dashboard.
         *
         * Expert can now see this case from
         * ExpertDashboardActivity.
         */
        Intent intent =
                new Intent(
                        DiseaseDetectionActivity.this,
                        FarmerDashboardActivity.class
                );

        /*
         * Clear the scan/detection screens from the
         * back stack so pressing Back does not reopen
         * the submitted detection.
         */
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);

        finish();
    }

    private void startEntranceAnimation() {

        diseaseImageCard.setAlpha(0f);
        diseaseImageCard.setTranslationY(30f);

        animateView(
                diseaseImageCard,
                100,
                500
        );
    }

    private void animateResultCards() {

        diseaseResultCard.setTranslationY(35f);

        animateView(
                diseaseResultCard,
                0,
                500
        );

        diseaseJourneyCard.setTranslationY(35f);

        animateView(
                diseaseJourneyCard,
                120,
                500
        );

        continueToExpertButton.setTranslationY(35f);

        animateView(
                continueToExpertButton,
                240,
                500
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

    private void loadTFLiteModel() {

        try {

            ByteBuffer modelBuffer =
                    loadModelFromAssets(MODEL_FILE);

            interpreter =
                    new Interpreter(modelBuffer);

        } catch (Exception e) {

            interpreter = null;

            /*
             * Keep technical details out of the farmer-facing UI.
             * The real error is available in Logcat during development.
             */
            android.util.Log.e(
                    "DiseaseDetection",
                    "Disease model could not be loaded",
                    e
            );
        }
    }

    private ByteBuffer loadModelFromAssets(
            String modelFile
    ) throws IOException {

        InputStream inputStream =
                getAssets().open(modelFile);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];

        int bytesRead;

        while ((bytesRead =
                inputStream.read(buffer)) != -1) {

            outputStream.write(
                    buffer,
                    0,
                    bytesRead
            );
        }

        inputStream.close();

        byte[] modelBytes =
                outputStream.toByteArray();

        ByteBuffer modelBuffer =
                ByteBuffer.allocateDirect(
                        modelBytes.length
                );

        modelBuffer.order(
                ByteOrder.nativeOrder()
        );

        modelBuffer.put(modelBytes);

        modelBuffer.rewind();

        return modelBuffer;
    }

    private void loadCapturedImage() {

        capturedImageUri =
                getIntent().getStringExtra(
                        "captured_image_uri"
                );

        if (capturedImageUri == null
                || capturedImageUri.trim().isEmpty()) {

            showDetectionError(
                    "Image unavailable",
                    "Please capture the crop leaf again."
            );

            return;
        }

        try {

            Uri imageUri =
                    Uri.parse(capturedImageUri);

            String imagePath =
                    imageUri.getPath();

            if (imagePath == null) {

                showDetectionError(
                        "Image unavailable",
                        "Please capture the crop leaf again."
                );

                return;
            }

            File imageFile =
                    new File(imagePath);

            if (!imageFile.exists()) {

                showDetectionError(
                        "Image unavailable",
                        "Please capture the crop leaf again."
                );

                return;
            }

            capturedBitmap =
                    BitmapFactory.decodeFile(
                            imageFile.getAbsolutePath()
                    );

            if (capturedBitmap == null) {

                showDetectionError(
                        "Image could not be loaded",
                        "Please capture the crop again."
                );

                return;
            }

            diseaseCropImage.setImageBitmap(
                    capturedBitmap
            );

            startDiseaseDetection();

        } catch (Exception e) {

            showDetectionError(
                    "Image could not be loaded",
                    "Please capture the crop again."
            );
        }
    }

    private void startDiseaseDetection() {

        diseaseOverlay.setVisibility(
                View.VISIBLE
        );

        diseaseProgress.setVisibility(
                View.VISIBLE
        );

        diseaseProcessingText.setText(
                "Analyzing crop leaf..."
        );

        diseaseIcon.setText("🧪");

        diseaseTitle.setText(
                "Detecting possible disease"
        );

        diseaseConfidence.setText("--");

        diseaseConfidenceBar.setProgress(0);

        diseaseDescription.setText(
                "The on-device AI model is analyzing "
                        + "the captured crop leaf."
        );

        diseaseStatusStep.setText(
                "✓  Crop leaf validated"
        );

        diseaseResultStep.setText(
                "◐  Disease detection"
        );

        expertValidationStep.setText(
                "○  Expert validation required"
        );

        continueToExpertButton.setVisibility(
                View.GONE
        );

        handler.postDelayed(
                this::runDiseaseModel,
                800
        );
    }

    private void runDiseaseModel() {

        if (capturedBitmap == null) {

            showDetectionError(
                    "Image unavailable",
                    "Please capture the crop leaf again."
            );

            return;
        }

        if (interpreter == null) {

            showDetectionError(
                    "Disease Not Identified",
                    "The image could not be confidently analyzed. "
                            + "Please send it for expert validation."
            );

            return;
        }

        try {

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            capturedBitmap,
                            IMAGE_SIZE,
                            IMAGE_SIZE,
                            true
                    );

            ByteBuffer inputBuffer =
                    ByteBuffer.allocateDirect(
                            IMAGE_SIZE
                                    * IMAGE_SIZE
                                    * 3
                                    * 4
                    );

            inputBuffer.order(
                    ByteOrder.nativeOrder()
            );

            int[] pixels =
                    new int[
                            IMAGE_SIZE * IMAGE_SIZE
                            ];

            resizedBitmap.getPixels(
                    pixels,
                    0,
                    IMAGE_SIZE,
                    0,
                    0,
                    IMAGE_SIZE,
                    IMAGE_SIZE
            );

            for (int pixel : pixels) {

                float red =
                        (pixel >> 16) & 0xff;

                float green =
                        (pixel >> 8) & 0xff;

                float blue =
                        pixel & 0xff;

                inputBuffer.putFloat(red);
                inputBuffer.putFloat(green);
                inputBuffer.putFloat(blue);
            }

            inputBuffer.rewind();

            float[][] output =
                    new float[
                            1
                            ][
                            DISEASE_LABELS.length
                            ];

            interpreter.run(
                    inputBuffer,
                    output
            );

            int predictedIndex = 0;

            float highestProbability =
                    output[0][0];

            for (
                    int i = 1;
                    i < DISEASE_LABELS.length;
                    i++
            ) {

                if (
                        output[0][i]
                                > highestProbability
                ) {

                    highestProbability =
                            output[0][i];

                    predictedIndex = i;
                }
            }

            float confidence =
                    highestProbability * 100f;

            String predictedDisease =
                    DISEASE_LABELS[
                            predictedIndex
                            ];

            resizedBitmap.recycle();

            /*
             * IMPORTANT:
             *
             * The existing model always selects one of its
             * 9 known classes. We therefore use a confidence
             * threshold before presenting that class as a
             * meaningful preliminary result.
             */
            if (confidence < CONFIDENCE_THRESHOLD) {

                showUnknownDiseaseResult(
                        confidence
                );

            } else {

                showDiseaseResult(
                        predictedDisease,
                        confidence
                );
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "DiseaseDetection",
                    "Disease detection failed",
                    e
            );

            showDetectionError(
                    "Disease Not Identified",
                    "The image could not be confidently analyzed. "
                            + "Please send it for expert validation."
            );
        }
    }

    private void showDiseaseResult(
            String disease,
            float confidence
    ) {

        /*
         * Store the exact result that is shown to the farmer.
         * This will be sent to ExpertCaseStorage.
         */
        finalDiseaseResult = disease;
        finalConfidence = confidence;
        finalResultIsUnknown = false;

        diseaseOverlay.setVisibility(
                View.GONE
        );

        diseaseProgress.setVisibility(
                View.GONE
        );

        diseaseStatusStep.setText(
                "✓  Crop leaf validated"
        );

        diseaseResultStep.setText(
                "✓  AI result generated"
        );

        expertValidationStep.setText(
                "◐  Expert validation required"
        );

        diseaseIcon.setText("🌿");

        diseaseTitle.setText(disease);

        diseaseDescription.setText(
                "Preliminary AI result. "
                        + "Expert validation is required before "
                        + "treating this as a confirmed diagnosis."
        );

        continueToExpertButton.setVisibility(
                View.VISIBLE
        );

        /*
         * Allow submission after a fresh result.
         */
        continueToExpertButton.setEnabled(true);
        continueToExpertButton.setAlpha(0f);

        caseSubmitted = false;

        animateResultCards();

        animateConfidence(
                Math.max(
                        0,
                        Math.min(
                                100,
                                Math.round(confidence)
                        )
                ),
                confidence
        );
    }

    private void showUnknownDiseaseResult(
            float confidence
    ) {

        /*
         * Store the unknown result.
         *
         * This will automatically make the case HIGH priority
         * inside ExpertCaseStorage because finalResultIsUnknown
         * is true.
         */
        finalDiseaseResult =
                "Disease Not Identified";

        finalConfidence = confidence;

        finalResultIsUnknown = true;

        diseaseOverlay.setVisibility(
                View.GONE
        );

        diseaseProgress.setVisibility(
                View.GONE
        );

        diseaseStatusStep.setText(
                "✓  Crop leaf validated"
        );

        diseaseResultStep.setText(
                "⚠  Disease not identified"
        );

        expertValidationStep.setText(
                "◐  Expert validation required"
        );

        diseaseIcon.setText("🔍");

        diseaseTitle.setText(
                "Disease Not Identified"
        );

        diseaseDescription.setText(
                "No known disease could be confidently "
                        + "identified from this image. "
                        + "The case should be reviewed by an expert."
        );

        continueToExpertButton.setVisibility(
                View.VISIBLE
        );

        /*
         * Allow submission after a fresh result.
         */
        continueToExpertButton.setEnabled(true);
        continueToExpertButton.setAlpha(0f);

        caseSubmitted = false;

        animateResultCards();

        animateConfidence(
                Math.max(
                        0,
                        Math.min(
                                100,
                                Math.round(confidence)
                        )
                ),
                confidence
        );
    }

    private void animateConfidence(
            int targetProgress,
            float targetConfidence
    ) {

        diseaseConfidenceBar.setProgress(0);

        diseaseConfidence.setText("0.0%");

        final int[] current =
                {0};

        final Handler confidenceHandler =
                new Handler(
                        Looper.getMainLooper()
                );

        Runnable confidenceRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        current[0] += 2;

                        if (
                                current[0]
                                        > targetProgress
                        ) {

                            current[0] =
                                    targetProgress;
                        }

                        diseaseConfidenceBar
                                .setProgress(
                                        current[0]
                                );

                        float displayed =
                                targetConfidence
                                        * current[0]
                                        / Math.max(
                                        1f,
                                        targetProgress
                                );

                        diseaseConfidence.setText(
                                String.format(
                                        Locale.US,
                                        "%.1f%%",
                                        displayed
                                )
                        );

                        if (
                                current[0]
                                        < targetProgress
                        ) {

                            confidenceHandler.postDelayed(
                                    this,
                                    18
                            );
                        }
                    }
                };

        confidenceHandler.post(
                confidenceRunnable
        );
    }

    private void showDetectionError(
            String title,
            String description
    ) {

        /*
         * Since this is an error/unknown state, we treat it
         * as an unknown expert-review case.
         */
        finalDiseaseResult =
                "Disease Not Identified";

        finalConfidence = 0.0f;

        finalResultIsUnknown = true;

        diseaseOverlay.setVisibility(
                View.GONE
        );

        diseaseProgress.setVisibility(
                View.GONE
        );

        diseaseIcon.setText("⚠️");

        diseaseTitle.setText(title);

        diseaseConfidence.setText("--");

        diseaseConfidenceBar.setProgress(0);

        diseaseDescription.setText(
                description
        );

        diseaseStatusStep.setText(
                "✓  Crop leaf validated"
        );

        diseaseResultStep.setText(
                "⚠  Disease not identified"
        );

        expertValidationStep.setText(
                "◐  Expert validation required"
        );

        /*
         * Even when the model cannot produce a reliable result,
         * the case should still be available for expert review.
         */
        continueToExpertButton.setVisibility(
                View.VISIBLE
        );

        continueToExpertButton.setEnabled(true);
        continueToExpertButton.setAlpha(0f);

        caseSubmitted = false;

        animateResultCards();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (handler != null) {

            handler.removeCallbacksAndMessages(
                    null
            );
        }

        if (interpreter != null) {

            interpreter.close();

            interpreter = null;
        }

        if (
                capturedBitmap != null
                        && !capturedBitmap.isRecycled()
        ) {

            capturedBitmap.recycle();
        }
    }
}