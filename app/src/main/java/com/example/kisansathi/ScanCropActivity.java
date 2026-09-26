package com.example.kisansathi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanCropActivity extends AppCompatActivity {

    private PreviewView cameraPreview;
    private TextView scanBackButton;
    private TextView flashButton;
    private TextView captureButton;
    private TextView scanStatusText;
    private TextView scanInstruction;
    private View scanningLine;

    private ImageCapture imageCapture;
    private Camera camera;

    private ExecutorService cameraExecutor;

    private boolean flashEnabled = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {
                            startCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Camera permission is required to scan a crop.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_crop);

        initializeViews();
        setupClickListeners();

        cameraExecutor = Executors.newSingleThreadExecutor();

        checkCameraPermission();

        startScanningAnimation();
    }

    private void initializeViews() {

        cameraPreview = findViewById(R.id.cameraPreview);

        scanBackButton = findViewById(R.id.scanBackButton);
        flashButton = findViewById(R.id.flashButton);
        captureButton = findViewById(R.id.captureButton);

        scanStatusText = findViewById(R.id.scanStatusText);
        scanInstruction = findViewById(R.id.scanInstruction);

        scanningLine = findViewById(R.id.scanningLine);
    }

    private void setupClickListeners() {

        scanBackButton.setOnClickListener(v -> finish());

        flashButton.setOnClickListener(v -> toggleFlash());

        captureButton.setOnClickListener(v -> captureCropImage());
    }

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(
                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build();

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

                preview.setSurfaceProvider(
                        cameraPreview.getSurfaceProvider()
                );

                scanStatusText.setText("Camera ready");

            } catch (Exception e) {

                scanStatusText.setText("Camera unavailable");

                Toast.makeText(
                        this,
                        "Unable to start camera.",
                        Toast.LENGTH_LONG
                ).show();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleFlash() {

        if (camera == null
                || !camera.getCameraInfo().hasFlashUnit()) {

            Toast.makeText(
                    this,
                    "Flash is not available on this camera.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        flashEnabled = !flashEnabled;

        camera.getCameraControl()
                .enableTorch(flashEnabled);

        if (flashEnabled) {
            flashButton.setText("💡");
        } else {
            flashButton.setText("⚡");
        }
    }

    private void captureCropImage() {

        if (imageCapture == null) {

            Toast.makeText(
                    this,
                    "Camera is still starting.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        captureButton.setEnabled(false);

        scanStatusText.setText("Capturing leaf...");

        scanInstruction.setText(
                "Hold steady while we capture the image"
        );

        captureButton.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(120)
                .withEndAction(() -> {

                    captureButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();

                    saveCapturedImage();

                })
                .start();
    }

    private void saveCapturedImage() {

        String timestamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.US
                ).format(new Date());

        File imageDirectory = new File(
                getFilesDir(),
                "crop_images"
        );

        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs();
        }

        File photoFile = new File(
                imageDirectory,
                "crop_" + timestamp + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        photoFile
                ).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            ImageCapture.OutputFileResults outputFileResults
                    ) {

                        Uri imageUri =
                                Uri.fromFile(photoFile);

                        openAnalysisScreen(imageUri);
                    }

                    @Override
                    public void onError(
                            ImageCaptureException exception
                    ) {

                        captureButton.setEnabled(true);

                        scanStatusText.setText(
                                "Capture failed"
                        );

                        scanInstruction.setText(
                                "Please try taking the photo again"
                        );

                        Toast.makeText(
                                ScanCropActivity.this,
                                "Unable to capture image.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void openAnalysisScreen(Uri imageUri) {

        Intent intent = new Intent(
                ScanCropActivity.this,
                CropAnalysisActivity.class
        );

        intent.putExtra(
                "captured_image_uri",
                imageUri.toString()
        );

        startActivity(intent);
    }

    private void startScanningAnimation() {

        scanningLine.post(() -> {

            View parent =
                    (View) scanningLine.getParent();

            float frameHeight =
                    parent.getHeight();

            scanningLine.setTranslationY(0);

            scanningLine.animate()
                    .translationY(frameHeight - 70)
                    .setDuration(1800)
                    .setInterpolator(
                            new LinearInterpolator()
                    )
                    .withEndAction(() -> {

                        scanningLine.animate()
                                .translationY(0)
                                .setDuration(1800)
                                .setInterpolator(
                                        new LinearInterpolator()
                                )
                                .withEndAction(
                                        this::startScanningAnimation
                                )
                                .start();

                    })
                    .start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}