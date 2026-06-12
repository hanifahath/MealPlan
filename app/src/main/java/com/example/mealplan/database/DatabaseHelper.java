package com.example.mealplan.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mealplan.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance = null;

    private static final String CREATE_TABLE_FAVORITES =
            "CREATE TABLE " + Constants.TABLE_FAVORITES + " (" +
            Constants.FAV_COL_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Constants.FAV_COL_MEAL_ID     + " TEXT UNIQUE NOT NULL, " +
            Constants.FAV_COL_NAME        + " TEXT, " +
            Constants.FAV_COL_THUMB       + " TEXT, " +
            Constants.FAV_COL_CATEGORY    + " TEXT, " +
            Constants.FAV_COL_INGREDIENTS + " TEXT, " +
            Constants.FAV_COL_INSTRUCTIONS+ " TEXT)";

    private static final String CREATE_TABLE_PLANNER =
            "CREATE TABLE " + Constants.TABLE_PLANNER + " (" +
            Constants.PLAN_COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Constants.PLAN_COL_DAY        + " TEXT NOT NULL, " +
            Constants.PLAN_COL_MEAL_ID    + " TEXT NOT NULL, " +
            Constants.PLAN_COL_MEAL_NAME  + " TEXT, " +
            Constants.PLAN_COL_MEAL_THUMB + " TEXT)";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_PLANNER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PLANNER);
        onCreate(db);
    }
}
