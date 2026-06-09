package com.example.mealplan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    private final DatabaseHelper dbHelper;

    public FavoriteDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Tambah favorit — return true jika berhasil
    public boolean insert(FavoriteMeal meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(Constants.FAV_COL_MEAL_ID,      meal.getMealId());
        cv.put(Constants.FAV_COL_NAME,          meal.getMealName());
        cv.put(Constants.FAV_COL_THUMB,         meal.getMealThumb());
        cv.put(Constants.FAV_COL_CATEGORY,      meal.getMealCategory());
        cv.put(Constants.FAV_COL_INGREDIENTS,   meal.getIngredients());
        cv.put(Constants.FAV_COL_INSTRUCTIONS,  meal.getInstructions());
        long result = db.insertWithOnConflict(
                Constants.TABLE_FAVORITES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    // Hapus favorit berdasarkan mealId
    public boolean delete(String mealId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(Constants.TABLE_FAVORITES,
                Constants.FAV_COL_MEAL_ID + "=?", new String[]{mealId});
        return rows > 0;
    }

    // Ambil semua favorit
    public List<FavoriteMeal> getAll() {
        List<FavoriteMeal> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_FAVORITES,
                null, null, null, null, null,
                Constants.FAV_COL_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                FavoriteMeal meal = cursorToMeal(cursor);
                list.add(meal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // Cek apakah suatu resep sudah jadi favorit
    public boolean isFavorite(String mealId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_FAVORITES,
                new String[]{Constants.FAV_COL_MEAL_ID},
                Constants.FAV_COL_MEAL_ID + "=?",
                new String[]{mealId}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    private FavoriteMeal cursorToMeal(Cursor cursor) {
        FavoriteMeal meal = new FavoriteMeal();
        meal.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.FAV_COL_ID)));
        meal.setMealId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_MEAL_ID)));
        meal.setMealName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_NAME)));
        meal.setMealThumb(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_THUMB)));
        meal.setMealCategory(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_CATEGORY)));
        meal.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_INGREDIENTS)));
        meal.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(Constants.FAV_COL_INSTRUCTIONS)));
        return meal;
    }
}
