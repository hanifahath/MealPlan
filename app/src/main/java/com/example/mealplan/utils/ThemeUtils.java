package com.example.mealplan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {

    // Panggil ini di onCreate() setiap Activity sebelum setContentView()
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        String theme = prefs.getString(Constants.PREF_THEME, "light");

        if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Toggle antara dark dan light, lalu simpan ke SharedPreferences
    public static void toggleTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        String current = prefs.getString(Constants.PREF_THEME, "light");
        String next = "dark".equals(current) ? "light" : "dark";

        prefs.edit().putString(Constants.PREF_THEME, next).apply();

        if ("dark".equals(next)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Cek apakah saat ini mode gelap
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_NAME, Context.MODE_PRIVATE);
        return "dark".equals(prefs.getString(Constants.PREF_THEME, "light"));
    }

    private ThemeUtils() {}
}
