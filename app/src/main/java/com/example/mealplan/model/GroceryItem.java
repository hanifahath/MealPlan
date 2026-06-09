package com.example.mealplan.model;

public class GroceryItem {
    private String name;
    private String measure;
    private String sourceMeal;  // dari resep hari apa
    private boolean checked;

    public GroceryItem(String name, String measure, String sourceMeal) {
        this.name = name;
        this.measure = measure;
        this.sourceMeal = sourceMeal;
        this.checked = false;
    }

    public String getName()         { return name; }
    public String getMeasure()      { return measure; }
    public String getSourceMeal()   { return sourceMeal; }
    public boolean isChecked()      { return checked; }
    public void setChecked(boolean v) { this.checked = v; }
}
