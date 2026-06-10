package com.example.mealplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mealplan.model.PlannerItem;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class PlannerDao {

    private final DatabaseHelper dbHelper;

    public PlannerDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Insert resep ke hari tertentu - TANPA hapus yang lama (support multi-meal)
    public boolean insert(PlannerItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Constants.PLAN_COL_DAY,        item.getDayOfWeek());
        cv.put(Constants.PLAN_COL_MEAL_ID,    item.getMealId());
        cv.put(Constants.PLAN_COL_MEAL_NAME,  item.getMealName());
        cv.put(Constants.PLAN_COL_MEAL_THUMB, item.getMealThumb());
        long result = db.insert(Constants.TABLE_PLANNER, null, cv);
        return result != -1;
    }

    // Hapus resep spesifik berdasarkan id row
    public boolean deleteById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(Constants.TABLE_PLANNER,
                Constants.PLAN_COL_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Hapus semua resep di satu hari
    public boolean deleteByDay(String day) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(Constants.TABLE_PLANNER,
                Constants.PLAN_COL_DAY + "=?", new String[]{day});
        return rows > 0;
    }

    // Ambil semua planner
    public List<PlannerItem> getAll() {
        List<PlannerItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_PLANNER,
                null, null, null, null, null,
                Constants.PLAN_COL_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do { list.add(cursorToItem(cursor)); }
            while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // Ambil semua resep di satu hari
    public List<PlannerItem> getByDay(String day) {
        List<PlannerItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_PLANNER,
                null,
                Constants.PLAN_COL_DAY + "=?",
                new String[]{day}, null, null,
                Constants.PLAN_COL_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do { list.add(cursorToItem(cursor)); }
            while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private PlannerItem cursorToItem(Cursor cursor) {
        PlannerItem item = new PlannerItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.PLAN_COL_ID)));
        item.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(Constants.PLAN_COL_DAY)));
        item.setMealId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.PLAN_COL_MEAL_ID)));
        item.setMealName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.PLAN_COL_MEAL_NAME)));
        item.setMealThumb(cursor.getString(cursor.getColumnIndexOrThrow(Constants.PLAN_COL_MEAL_THUMB)));
        return item;
    }
}
