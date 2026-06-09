package com.example.mealplan.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("strCategory")
    private String name;

    @SerializedName("strCategoryThumb")
    private String thumb;

    @SerializedName("strCategoryDescription")
    private String description;

    public Category(String name, String thumb, String description) {
        this.name = name;
        this.thumb = thumb;
        this.description = description;
    }

    public String getName() { return name; }
    public String getThumb() { return thumb; }
    public String getDescription() { return description; }
}
