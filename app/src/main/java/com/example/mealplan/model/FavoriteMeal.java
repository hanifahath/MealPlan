package com.example.mealplan.model;

// Representasi satu baris di tabel SQLite favorites
public class FavoriteMeal {

    private int id;           // primary key SQLite
    private String mealId;    // idMeal dari TheMealDB
    private String mealName;
    private String mealThumb;
    private String mealCategory;
    private String ingredients;   // JSON string
    private String instructions;

    public FavoriteMeal() {}

    public FavoriteMeal(String mealId, String mealName, String mealThumb,
                        String mealCategory, String ingredients, String instructions) {
        this.mealId = mealId;
        this.mealName = mealName;
        this.mealThumb = mealThumb;
        this.mealCategory = mealCategory;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getMealId()       { return mealId; }
    public void setMealId(String v) { this.mealId = v; }

    public String getMealName()         { return mealName; }
    public void setMealName(String v)   { this.mealName = v; }

    public String getMealThumb()        { return mealThumb; }
    public void setMealThumb(String v)  { this.mealThumb = v; }

    public String getMealCategory()         { return mealCategory; }
    public void setMealCategory(String v)   { this.mealCategory = v; }

    public String getIngredients()          { return ingredients; }
    public void setIngredients(String v)    { this.ingredients = v; }

    public String getInstructions()         { return instructions; }
    public void setInstructions(String v)   { this.instructions = v; }
}
