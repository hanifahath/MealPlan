package com.example.mealplan.model;


public class PlannerItem {

    private int id;
    private String dayOfWeek;
    private String mealId;
    private String mealName;
    private String mealThumb;

    public PlannerItem() {}

    public PlannerItem(String dayOfWeek, String mealId, String mealName, String mealThumb) {
        this.dayOfWeek = dayOfWeek;
        this.mealId = mealId;
        this.mealName = mealName;
        this.mealThumb = mealThumb;
    }

    public int getId()              { return id; }
    public void setId(int id)       { this.id = id; }

    public String getDayOfWeek()            { return dayOfWeek; }
    public void setDayOfWeek(String v)      { this.dayOfWeek = v; }

    public String getMealId()               { return mealId; }
    public void setMealId(String v)         { this.mealId = v; }

    public String getMealName()             { return mealName; }
    public void setMealName(String v)       { this.mealName = v; }

    public String getMealThumb()            { return mealThumb; }
    public void setMealThumb(String v)      { this.mealThumb = v; }
}
