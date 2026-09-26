package com.example.kisansathi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

public class CropLeafValidationActivity extends AppCompatActivity {

    private static final String TAG =
            "CropLeafValidation";

    private static final String MODEL_FILE =
            "kisan_sathi_crop_leaf_classifier.tflite";

    private static final float CROP_ACCEPTANCE_THRESHOLD =
            0.70f;

    private ImageView validationCropImage;
    private LinearLayout validationOverlay;
    private ProgressBar validationProgress;
    private TextView validationProcessingText;
    private TextView validationBackButton;
    private TextView validationIcon;
    private TextView validationTitle;
    private TextView validationDescription;
    private TextView imageQualityValidationStep;
    private TextView cropValidationStep;
    private TextView diseaseValidationStep;
    private TextView continueToDiseaseButton;
    private TextView captureAgainButton;

    private Handler handler;

    private Bitmap capturedBitmap;
    private Interpreter interpreter;

    private String modelLoadError =
            "Unknown model loading error.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_crop_leaf_validation
        );

        handler =
                new Handler(
                        Looper.getMainLooper()
                );

        initializeViews();
        setupClickListeners();
        loadTFLiteModel();
        loadCapturedImage();
    }

    private void initializeViews() {

        validationCropImage =
                findViewById(
                        R.id.validationCropImage
                );

        validationOverlay =
                findViewById(
                        R.id.validationOverlay
                );

        validationProgress =
                findViewById(
                        R.id.validationProgress
                );

        validationProcessingText =
                findViewById(
                        R.id.validationProcessingText
                );

        validationBackButton =
                findViewById(
                        R.id.validationBackButton
                );

        validationIcon =
                findViewById(
                        R.id.validationIcon
                );

        validationTitle =
                findViewById(
                        R.id.validationTitle
                );

        validationDescription =
                findViewById(
                        R.id.validationDescription
                );

        imageQualityValidationStep =
                findViewById(
                        R.id.imageQualityValidationStep
                );

        cropValidationStep =
                findViewById(
                        R.id.cropValidationStep
                );

        diseaseValidationStep =
                findViewById(
                        R.id.diseaseValidationStep
                );

        continueToDiseaseButton =
                findViewById(
                        R.id.continueToDiseaseButton
                );

        captureAgainButton =
                findViewById(
                        R.id.captureAgainButton
                );
    }

    private void setupClickListeners() {

        validationBackButton.setOnClickListener(
                v -> finish()
        );

        captureAgainButton.setOnClickListener(
                v -> openCamera()
        );

        continueToDiseaseButton.setOnClickListener(
                v -> openDiseaseDetection()
        );
    }

    private void loadTFLiteModel() {

        Log.d(
                TAG,
                "Starting TFLite model loading."
        );

        Log.d(
                TAG,
                "Model filename: "
                        + MODEL_FILE
        );

        try {

            String[] assetFiles =
                    getAssets().list("");

            if (assetFiles != null) {

                Log.d(
                        TAG,
                        "Files found in assets:"
                );

                for (String fileName :
                        assetFiles) {

                    Log.d(
                            TAG,
                            "Asset: "
                                    + fileName
                    );
                }
            }

            ByteBuffer modelBuffer =
                    loadModelFromAssets(
                            MODEL_FILE
                    );

            Log.d(
                    TAG,
                    "Model bytes loaded: "
                            + modelBuffer.capacity()
            );

            Interpreter.Options options =
                    new Interpreter.Options();

            interpreter =
                    new Interpreter(
                            modelBuffer,
                            options
                    );

            Log.d(
                    TAG,
                    "TFLite model loaded successfully."
            );

            int[] inputShape =
                    interpreter
                            .getInputTensor(0)
                            .shape();

            int[] outputShape =
                    interpreter
                            .getOutputTensor(0)
                            .shape();

            Log.d(
                    TAG,
                    "Input shape: "
                            + shapeToString(
                            inputShape
                    )
            );

            Log.d(
                    TAG,
                    "Output shape: "
                            + shapeToString(
                            outputShape
                    )
            );

        } catch (Exception e) {

            interpreter = null;

            modelLoadError =
                    e.getClass().getSimpleName()
                            + ": "
                            + String.valueOf(
                            e.getMessage()
                    );

            Log.e(
                    TAG,
                    "FAILED TO LOAD TFLITE MODEL",
                    e
            );

            Toast.makeText(
                    this,
                    "Crop AI model failed to load.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String shapeToString(
            int[] shape
    ) {

        if (shape == null
                || shape.length == 0) {

            return "unknown";
        }

        StringBuilder builder =
                new StringBuilder();

        builder.append("[");

        for (int i = 0;
             i < shape.length;
             i++) {

            builder.append(
                    shape[i]
            );

            if (i < shape.length - 1) {
                builder.append(", ");
            }
        }

        builder.append("]");

        return builder.toString();
    }

    private ByteBuffer loadModelFromAssets(
            String modelFile
    ) throws IOException {

        InputStream inputStream =
                getAssets().open(modelFile);

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[4096];

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

        if (modelBytes.length == 0) {

            throw new IOException(
                    "Model file is empty."
            );
        }

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

            validationCropImage.setImageBitmap(
                    capturedBitmap
            );

            startCropValidation();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Failed to load captured image.",
                    e
            );

            showImageError();
        }
    }

    private void startCropValidation() {

        validationOverlay.setVisibility(
                View.VISIBLE
        );

        validationProgress.setVisibility(
                View.VISIBLE
        );

        validationProcessingText.setText(
                "Checking crop leaf..."
        );

        validationIcon.setText(
                "🔍"
        );

        validationTitle.setText(
                "Validating your image"
        );

        validationDescription.setText(
                "AI is checking whether the captured image contains a crop leaf."
        );

        imageQualityValidationStep.setText(
                "✓  Image quality check"
        );

        cropValidationStep.setText(
                "◐  Crop / leaf validation"
        );

        diseaseValidationStep.setText(
                "○  Disease detection"
        );

        continueToDiseaseButton.setVisibility(
                View.GONE
        );

        captureAgainButton.setVisibility(
                View.GONE
        );

        handler.postDelayed(
                this::runCropLeafModel,
                700
        );
    }

    private void runCropLeafModel() {

        if (capturedBitmap == null) {

            showValidationError(
                    "Image unavailable",
                    "Please capture the crop leaf again."
            );

            return;
        }

        if (interpreter == null) {

            Log.e(
                    TAG,
                    "Interpreter is null."
                            + " Model error: "
                            + modelLoadError
            );

            showValidationError(
                    "AI model unavailable",
                    "The crop validation model could not be loaded.\n\n"
                            + modelLoadError
            );

            return;
        }

        try {

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            capturedBitmap,
                            224,
                            224,
                            true
                    );

            ByteBuffer inputBuffer =
                    ByteBuffer.allocateDirect(
                            224
                                    * 224
                                    * 3
                                    * 4
                    );

            inputBuffer.order(
                    ByteOrder.nativeOrder()
            );

            int[] pixels =
                    new int[
                            224 * 224
                            ];

            resizedBitmap.getPixels(
                    pixels,
                    0,
                    224,
                    0,
                    0,
                    224,
                    224
            );

            for (int pixel : pixels) {

                float red =
                        (pixel >> 16)
                                & 0xff;

                float green =
                        (pixel >> 8)
                                & 0xff;

                float blue =
                        pixel & 0xff;

                inputBuffer.putFloat(
                        red
                );

                inputBuffer.putFloat(
                        green
                );

                inputBuffer.putFloat(
                        blue
                );
            }

            inputBuffer.rewind();

            float[][] output =
                    new float[1][1];

            interpreter.run(
                    inputBuffer,
                    output
            );

            float nonCropProbability =
                    output[0][0];

            float cropProbability =
                    1.0f
                            - nonCropProbability;

            resizedBitmap.recycle();

            showModelResult(
                    cropProbability,
                    nonCropProbability
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Crop model inference failed.",
                    e
            );

            showValidationError(
                    "Validation failed",
                    "The image could not be processed.\n\n"
                            + e.getClass()
                            .getSimpleName()
                            + ": "
                            + String.valueOf(
                            e.getMessage()
                    )
            );
        }
    }

    private void showModelResult(
            float cropProbability,
            float nonCropProbability
    ) {

        validationOverlay.setVisibility(
                View.GONE
        );

        validationProgress.setVisibility(
                View.GONE
        );

        imageQualityValidationStep.setText(
                "✓  Image quality check"
        );

        int confidence =
                Math.round(
                        Math.max(
                                cropProbability,
                                nonCropProbability
                        )
                                * 100
                );

        if (cropProbability >=
                CROP_ACCEPTANCE_THRESHOLD) {

            cropValidationStep.setText(
                    "✓  Crop leaf detected"
            );

            diseaseValidationStep.setText(
                    "◐  Ready for disease detection"
            );

            validationIcon.setText(
                    "🌿"
            );

            validationTitle.setText(
                    "Crop leaf detected"
            );

            validationDescription.setText(
                    "The image appears to contain a crop leaf. Confidence: "
                            + confidence
                            + "%"
            );

            continueToDiseaseButton.setVisibility(
                    View.VISIBLE
            );

            captureAgainButton.setVisibility(
                    View.GONE
            );

        } else {

            cropValidationStep.setText(
                    "✕  Crop leaf not detected"
            );

            diseaseValidationStep.setText(
                    "○  Disease detection"
            );

            validationIcon.setText(
                    "⚠️"
            );

            validationTitle.setText(
                    "Please capture a crop leaf"
            );

            validationDescription.setText(
                    "The AI could not identify this image as a crop leaf. Confidence: "
                            + confidence
                            + "%"
            );

            continueToDiseaseButton.setVisibility(
                    View.GONE
            );

            captureAgainButton.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void showValidationError(
            String title,
            String description
    ) {

        validationOverlay.setVisibility(
                View.GONE
        );

        validationProgress.setVisibility(
                View.GONE
        );

        validationIcon.setText(
                "⚠️"
        );

        validationTitle.setText(
                title
        );

        validationDescription.setText(
                description
        );

        imageQualityValidationStep.setText(
                "✓  Image quality check"
        );

        cropValidationStep.setText(
                "✕  Crop / leaf validation"
        );

        diseaseValidationStep.setText(
                "○  Disease detection"
        );

        continueToDiseaseButton.setVisibility(
                View.GONE
        );

        captureAgainButton.setVisibility(
                View.VISIBLE
        );
    }

    private void showImageError() {

        validationCropImage.setImageDrawable(
                null
        );

        showValidationError(
                "Image could not be loaded",
                "Please go back and capture the crop again."
        );
    }

    private void openDiseaseDetection() {

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
                        CropLeafValidationActivity.this,
                        DiseaseDetectionActivity.class
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
                        CropLeafValidationActivity.this,
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

        if (interpreter != null) {

            interpreter.close();

            interpreter = null;
        }

        if (capturedBitmap != null
                && !capturedBitmap.isRecycled()) {

            capturedBitmap.recycle();
        }
    }
}