package com.example.mealplan.model;

public class GroceryItem {
    public static final int TYPE_HEADER  = 0;
    public static final int TYPE_ITEM    = 1;
    public static final int TYPE_CHECKED_HEADER = 2;

    private int viewType;
    private String name;
    private String measure;
    private String sourceMeal;
    private boolean checked;

    // Constructor untuk header section
    public GroceryItem(String sectionTitle, int viewType) {
        this.name = sectionTitle;
        this.viewType = viewType;
    }

    // Constructor untuk item biasa
    public GroceryItem(String name, String measure, String sourceMeal) {
        this.name = name;
        this.measure = measure;
        this.sourceMeal = sourceMeal;
        this.viewType = TYPE_ITEM;
        this.checked = false;
    }

    public int getViewType()              { return viewType; }
    public String getName()               { return name; }
    public String getMeasure()            { return measure != null ? measure : ""; }
    public String getSourceMeal()         { return sourceMeal; }
    public boolean isChecked()            { return checked; }
    public void setChecked(boolean v)     { this.checked = v; }
}