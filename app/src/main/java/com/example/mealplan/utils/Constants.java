package com.example.mealplan.utils;

public class Constants {

    // --- API ---
    public static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    // --- Database ---
    public static final String DB_NAME = "mealplan.db";
    public static final int DB_VERSION = 1;

    // Tabel favorites
    public static final String TABLE_FAVORITES = "favorites";
    public static final String FAV_COL_ID = "id";
    public static final String FAV_COL_MEAL_ID = "meal_id";
    public static final String FAV_COL_NAME = "meal_name";
    public static final String FAV_COL_THUMB = "meal_thumb";
    public static final String FAV_COL_CATEGORY = "meal_category";
    public static final String FAV_COL_INGREDIENTS = "ingredients";
    public static final String FAV_COL_INSTRUCTIONS = "instructions";

    // Tabel planner
    public static final String TABLE_PLANNER = "planner";
    public static final String PLAN_COL_ID = "id";
    public static final String PLAN_COL_DAY = "day_of_week";
    public static final String PLAN_COL_MEAL_ID = "meal_id";
    public static final String PLAN_COL_MEAL_NAME = "meal_name";
    public static final String PLAN_COL_MEAL_THUMB = "meal_thumb";

    // --- SharedPreferences ---
    public static final String PREF_NAME = "mealplan_prefs";
    public static final String PREF_THEME = "theme_mode";   // "light" | "dark"

    // --- Intent Keys ---
    public static final String INTENT_MEAL_ID = "meal_id";
    public static final String INTENT_MEAL_NAME = "meal_name";
    public static final String INTENT_MEAL_THUMB = "meal_thumb";
    public static final String INTENT_MEAL_CATEGORY = "meal_category";

    // --- Hari untuk Planner ---
    public static final String[] DAYS_OF_WEEK = {
            "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
    };

    private Constants() {}
}
