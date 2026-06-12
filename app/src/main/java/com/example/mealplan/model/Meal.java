package com.example.mealplan.model;

import com.google.gson.annotations.SerializedName;

public class Meal {

    @SerializedName("idMeal")
    private String id;

    @SerializedName("strMeal")
    private String name;

    @SerializedName("strMealThumb")
    private String thumb;

    private String category;

    public Meal(String id, String name, String thumb, String category) {
        this.id = id;
        this.name = name;
        this.thumb = thumb;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getThumb() { return thumb; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
