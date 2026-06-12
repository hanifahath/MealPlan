package com.example.mealplan.network;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealApiService {

    // GET https://www.themealdb.com/api/json/v1/1/categories.php
    @GET("categories.php")
    Call<JsonObject> getCategories();

    // GET https://www.themealdb.com/api/json/v1/1/filter.php?c=Seafood
    @GET("filter.php")
    Call<JsonObject> getMealsByCategory(@Query("c") String category);

    // GET https://www.themealdb.com/api/json/v1/1/search.php?s=chicken
    @GET("search.php")
    Call<JsonObject> searchMeals(@Query("s") String query);

    // GET https://www.themealdb.com/api/json/v1/1/lookup.php?i=52772
    @GET("lookup.php")
    Call<JsonObject> getMealDetail(@Query("i") String mealId);

    // GET https://www.themealdb.com/api/json/v1/1/random.php
    @GET("random.php")
    Call<JsonObject> getRandomMeal();
}
