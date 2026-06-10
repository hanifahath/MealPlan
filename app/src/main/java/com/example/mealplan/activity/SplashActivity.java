package com.example.mealplan.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealplan.utils.ThemeUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme SEBELUM super agar windowBackground splash langsung muncul
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        // Tidak perlu setContentView — windowBackground dari theme yang handle tampilan

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            // Transisi halus: fade in ke MainActivity
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION_MS);
    }

    @Override
    public void onBackPressed() {
        // Nonaktifkan back press di splash agar tidak bisa di-cancel
    }
}
