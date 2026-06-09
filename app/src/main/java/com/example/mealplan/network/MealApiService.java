package com.example.mealplan.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealApiService {

    // Ambil semua kategori
    // GET https://www.themealdb.com/api/json/v1/1/categories.php
    @GET("categories.php")
    Call<JsonObject> getCategories();

    // Ambil daftar resep berdasarkan kategori
    // GET https://www.themealdb.com/api/json/v1/1/filter.php?c=Seafood
    @GET("filter.php")
    Call<JsonObject> getMealsByCategory(@Query("c") String category);

    // Cari resep berdasarkan nama
    // GET https://www.themealdb.com/api/json/v1/1/search.php?s=chicken
    @GET("search.php")
    Call<JsonObject> searchMeals(@Query("s") String query);

    // Detail lengkap satu resep berdasarkan ID
    // GET https://www.themealdb.com/api/json/v1/1/lookup.php?i=52772
    @GET("lookup.php")
    Call<JsonObject> getMealDetail(@Query("i") String mealId);

    // Satu resep acak
    // GET https://www.themealdb.com/api/json/v1/1/random.php
    @GET("random.php")
    Call<JsonObject> getRandomMeal();
}
