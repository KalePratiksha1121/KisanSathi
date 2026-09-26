package com.example.kisansathi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class CropAnalysisActivity extends AppCompatActivity {

    private ImageView capturedCropImage;

    private LinearLayout processingOverlay;

    private ProgressBar analysisProgress;

    private TextView processingText;
    private TextView analysisBackButton;

    private TextView statusIcon;
    private TextView statusTitle;
    private TextView statusDescription;

    private TextView qualityStep;
    private TextView modelStep;
    private TextView validationStep;

    private TextView continueAnalysisButton;

    private Handler handler;

    private Bitmap capturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crop_analysis);

        handler = new Handler(Looper.getMainLooper());

        initializeViews();
        setupClickListeners();
        loadCapturedImage();
    }

    private void initializeViews() {

        capturedCropImage =
                findViewById(R.id.capturedCropImage);

        processingOverlay =
                findViewById(R.id.processingOverlay);

        analysisProgress =
                findViewById(R.id.analysisProgress);

        processingText =
                findViewById(R.id.processingText);

        analysisBackButton =
                findViewById(R.id.analysisBackButton);

        statusIcon =
                findViewById(R.id.statusIcon);

        statusTitle =
                findViewById(R.id.statusTitle);

        statusDescription =
                findViewById(R.id.statusDescription);

        qualityStep =
                findViewById(R.id.qualityStep);

        modelStep =
                findViewById(R.id.modelStep);

        validationStep =
                findViewById(R.id.validationStep);

        continueAnalysisButton =
                findViewById(R.id.continueAnalysisButton);
    }

    private void setupClickListeners() {

        analysisBackButton.setOnClickListener(
                v -> finish()
        );

        continueAnalysisButton.setClickable(true);
        continueAnalysisButton.setFocusable(true);

        continueAnalysisButton.setOnClickListener(
                v -> openCropLeafValidation()
        );
    }

    private void loadCapturedImage() {

        String imageUriString =
                getIntent().getStringExtra(
                        "captured_image_uri"
                );

        if (imageUriString == null
                || imageUriString.trim().isEmpty()) {

            showImageError();
            return;
        }

        try {

            Uri imageUri =
                    Uri.parse(imageUriString);

            String imagePath =
                    imageUri.getPath();

            if (imagePath == null) {

                showImageError();
                return;
            }

            File imageFile =
                    new File(imagePath);

            if (!imageFile.exists()) {

                showImageError();
                return;
            }

            capturedBitmap =
                    BitmapFactory.decodeFile(
                            imageFile.getAbsolutePath()
                    );

            if (capturedBitmap == null) {

                showImageError();
                return;
            }

            capturedCropImage.setImageBitmap(
                    capturedBitmap
            );

            startImageQualityCheck();

        } catch (Exception e) {

            showImageError();
        }
    }

    private void showImageError() {

        capturedCropImage.setImageDrawable(null);

        processingOverlay.setVisibility(
                View.GONE
        );

        analysisProgress.setVisibility(
                View.GONE
        );

        statusIcon.setText("⚠️");

        statusTitle.setText(
                "Image could not be loaded"
        );

        statusDescription.setText(
                "Please go back and capture the crop again."
        );

        qualityStep.setText(
                "✕  Image quality check"
        );

        modelStep.setText(
                "○  Crop / leaf validation"
        );

        validationStep.setText(
                "○  Expert validation when required"
        );

        continueAnalysisButton.setVisibility(
                View.VISIBLE
        );

        continueAnalysisButton.setClickable(true);
        continueAnalysisButton.setFocusable(true);

        continueAnalysisButton.setText(
                "Capture Again"
        );

        continueAnalysisButton.setOnClickListener(
                v -> openCamera()
        );
    }

    private void startImageQualityCheck() {

        processingOverlay.setVisibility(
                View.VISIBLE
        );

        analysisProgress.setVisibility(
                View.VISIBLE
        );

        processingText.setText(
                "Checking image quality..."
        );

        statusIcon.setText("🔍");

        statusTitle.setText(
                "Checking your image"
        );

        statusDescription.setText(
                "Checking brightness, clarity and image quality."
        );

        qualityStep.setText(
                "◐  Image quality check"
        );

        modelStep.setText(
                "○  Crop / leaf validation"
        );

        validationStep.setText(
                "○  Expert validation when required"
        );

        continueAnalysisButton.setVisibility(
                View.GONE
        );

        handler.postDelayed(
                this::performImageQualityCheck,
                900
        );
    }

    private void performImageQualityCheck() {

        if (capturedBitmap == null) {

            rejectImage(
                    "Image is unavailable",
                    "Please capture the crop leaf again."
            );

            return;
        }

        if (capturedBitmap.getWidth() < 500
                || capturedBitmap.getHeight() < 500) {

            rejectImage(
                    "Image quality is too low",
                    "Move closer to the crop leaf and capture a clearer photo."
            );

            return;
        }

        double brightness =
                calculateBrightness(capturedBitmap);

        if (brightness < 35) {

            rejectImage(
                    "Image is too dark",
                    "Move to a brighter place and capture the crop leaf again."
            );

            return;
        }

        if (brightness > 245) {

            rejectImage(
                    "Image is too bright",
                    "Avoid very strong direct light and capture the crop leaf again."
            );

            return;
        }

        double blurScore =
                calculateBlurScore(capturedBitmap);

        if (blurScore < 8) {

            rejectImage(
                    "Image is too blurry",
                    "Hold the phone steady and capture the leaf again."
            );

            return;
        }

        acceptImageQuality();
    }

    private double calculateBrightness(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int sampleStep = Math.max(
                1,
                Math.min(width, height) / 100
        );

        long totalBrightness = 0;
        long pixelCount = 0;

        for (
                int y = 0;
                y < height;
                y += sampleStep
        ) {

            for (
                    int x = 0;
                    x < width;
                    x += sampleStep
            ) {

                int pixel =
                        bitmap.getPixel(x, y);

                int red =
                        (pixel >> 16) & 0xff;

                int green =
                        (pixel >> 8) & 0xff;

                int blue =
                        pixel & 0xff;

                int brightness =
                        (red + green + blue) / 3;

                totalBrightness += brightness;

                pixelCount++;
            }
        }

        if (pixelCount == 0) {
            return 0;
        }

        return (double) totalBrightness
                / pixelCount;
    }

    private double calculateBlurScore(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int sampleStep = Math.max(
                2,
                Math.min(width, height) / 120
        );

        double mean = 0;
        double squaredMean = 0;

        long count = 0;

        for (
                int y = sampleStep;
                y < height - sampleStep;
                y += sampleStep
        ) {

            for (
                    int x = sampleStep;
                    x < width - sampleStep;
                    x += sampleStep
            ) {

                int center =
                        getGrayPixel(
                                bitmap.getPixel(x, y)
                        );

                int left =
                        getGrayPixel(
                                bitmap.getPixel(
                                        x - sampleStep,
                                        y
                                )
                        );

                int right =
                        getGrayPixel(
                                bitmap.getPixel(
                                        x + sampleStep,
                                        y
                                )
                        );

                int top =
                        getGrayPixel(
                                bitmap.getPixel(
                                        x,
                                        y - sampleStep
                                )
                        );

                int bottom =
                        getGrayPixel(
                                bitmap.getPixel(
                                        x,
                                        y + sampleStep
                                )
                        );

                double laplacian =
                        left
                                + right
                                + top
                                + bottom
                                - (4 * center);

                mean += laplacian;

                squaredMean +=
                        laplacian * laplacian;

                count++;
            }
        }

        if (count == 0) {
            return 0;
        }

        mean /= count;

        squaredMean /= count;

        return squaredMean
                - (mean * mean);
    }

    private int getGrayPixel(int pixel) {

        int red =
                (pixel >> 16) & 0xff;

        int green =
                (pixel >> 8) & 0xff;

        int blue =
                pixel & 0xff;

        return (
                red * 299
                        + green * 587
                        + blue * 114
        ) / 1000;
    }

    private void acceptImageQuality() {

        processingText.setText(
                "Image quality looks good"
        );

        qualityStep.setText(
                "✓  Image quality check"
        );

        qualityStep.setTextColor(
                getColor(R.color.dark_green)
        );

        statusIcon.setText("✓");

        statusTitle.setText(
                "Image quality accepted"
        );

        statusDescription.setText(
                "The photo is clear enough for crop validation."
        );

        handler.postDelayed(() -> {

            processingOverlay.setVisibility(
                    View.GONE
            );

            analysisProgress.setVisibility(
                    View.GONE
            );

            modelStep.setText(
                    "◐  Crop / leaf validation"
            );

            validationStep.setText(
                    "○  Expert validation when required"
            );

            statusIcon.setText("🌿");

            statusTitle.setText(
                    "Ready for crop validation"
            );

            statusDescription.setText(
                    "The next step will check whether this image contains a supported crop leaf."
            );

            continueAnalysisButton.setVisibility(
                    View.VISIBLE
            );

            continueAnalysisButton.setClickable(true);
            continueAnalysisButton.setFocusable(true);

            continueAnalysisButton.setText(
                    "Continue to Crop Validation"
            );

            continueAnalysisButton.setOnClickListener(
                    v -> openCropLeafValidation()
            );

        }, 1200);
    }

    private void rejectImage(
            String title,
            String description
    ) {

        processingOverlay.setVisibility(
                View.GONE
        );

        analysisProgress.setVisibility(
                View.GONE
        );

        statusIcon.setText("⚠️");

        statusTitle.setText(title);

        statusDescription.setText(
                description
        );

        qualityStep.setText(
                "✕  Image quality check"
        );

        modelStep.setText(
                "○  Crop / leaf validation"
        );

        validationStep.setText(
                "○  Expert validation when required"
        );

        continueAnalysisButton.setVisibility(
                View.VISIBLE
        );

        continueAnalysisButton.setClickable(true);
        continueAnalysisButton.setFocusable(true);

        continueAnalysisButton.setText(
                "Capture Again"
        );

        continueAnalysisButton.setOnClickListener(
                v -> openCamera()
        );
    }

    private void openCropLeafValidation() {

        String imageUriString =
                getIntent().getStringExtra(
                        "captured_image_uri"
                );

        if (imageUriString == null
                || imageUriString.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Image is unavailable. Please capture again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        CropAnalysisActivity.this,
                        CropLeafValidationActivity.class
                );

        intent.putExtra(
                "captured_image_uri",
                imageUriString
        );

        startActivity(intent);
    }

    private void openCamera() {

        Intent intent =
                new Intent(
                        CropAnalysisActivity.this,
                        ScanCropActivity.class
                );

        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handler != null) {

            handler.removeCallbacksAndMessages(
                    null
            );
        }

        if (capturedBitmap != null
                && !capturedBitmap.isRecycled()) {

            capturedBitmap.recycle();
        }
    }
}