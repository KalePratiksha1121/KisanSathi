package com.example.kisansathi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class LanguageActivity extends AppCompatActivity {

    private TextView englishButton;
    private TextView marathiButton;
    private TextView hindiButton;
    private TextView gujaratiButton;
    private TextView punjabiButton;
    private TextView continueButton;

    private String selectedLanguage = "en";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "KisanSathiPreferences",
                MODE_PRIVATE
        );

        selectedLanguage = preferences.getString(
                "selected_language",
                "en"
        );

        setContentView(R.layout.activity_language);

        englishButton = findViewById(R.id.englishButton);
        marathiButton = findViewById(R.id.marathiButton);
        hindiButton = findViewById(R.id.hindiButton);
        gujaratiButton = findViewById(R.id.gujaratiButton);
        punjabiButton = findViewById(R.id.punjabiButton);
        continueButton = findViewById(R.id.continueButton);

        englishButton.setOnClickListener(v -> selectLanguage("en"));

        marathiButton.setOnClickListener(v -> selectLanguage("mr"));

        hindiButton.setOnClickListener(v -> selectLanguage("hi"));

        gujaratiButton.setOnClickListener(v -> selectLanguage("gu"));

        punjabiButton.setOnClickListener(v -> selectLanguage("pa"));

        continueButton.setOnClickListener(v -> continueToProfile());
    }

    private void selectLanguage(String languageCode) {

        selectedLanguage = languageCode;

        preferences.edit()
                .putString("selected_language", languageCode)
                .apply();

        applyLanguage(languageCode);
    }

    private void applyLanguage(String languageCode) {

        LocaleListCompat localeList =
                LocaleListCompat.forLanguageTags(languageCode);

        AppCompatDelegate.setApplicationLocales(localeList);
    }

    private void continueToProfile() {

        preferences.edit()
                .putString("selected_language", selectedLanguage)
                .apply();

        Intent intent = new Intent(
                LanguageActivity.this,
                MobileNumberActivity.class
        );

        startActivity(intent);
        finish();
    }
}