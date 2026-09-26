package com.example.kisansathi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingProgress;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private int progress = 0;

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {

            progress += 2;

            if (progress <= 100) {

                loadingProgress.setProgress(progress);

                // Continue moving the line
                handler.postDelayed(this, 50);

            } else {

                // Loading completed → open Language screen
                Intent intent = new Intent(
                        MainActivity.this,
                        LanguageActivity.class
                );

                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadingProgress = findViewById(R.id.loadingProgress);

        loadingProgress.setProgress(0);

        // Start moving loading line
        handler.postDelayed(progressRunnable, 50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop callbacks when Activity is destroyed
        handler.removeCallbacks(progressRunnable);
    }
}
