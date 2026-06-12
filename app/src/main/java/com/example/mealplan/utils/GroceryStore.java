package com.example.mealplan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mealplan.model.GroceryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroceryStore {

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int addItems(Context c, String source, List<String[]> items) {
        int added = 0;
        try {
            JSONArray arr = load(c);
            for (String[] it : items) {
                if (it == null || it.length == 0) continue;
                String name = it[0] == null ? "" : it[0].trim();
                if (name.isEmpty()) continue;
                String measure = (it.length > 1 && it[1] != null) ? it[1].trim() : "";
                String src = source == null ? "" : source.trim();
                if (contains(arr, name, measure, src)) continue;
                JSONObject o = new JSONObject();
                o.put("name", name);
                o.put("measure", measure);
                o.put("source", src);
                arr.put(o);
                added++;
            }
            prefs(c).edit().putString(Constants.KEY_EXTRA_GROCERY, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return added;
    }

    public static List<GroceryItem> getItems(Context c) {
        List<GroceryItem> list = new ArrayList<>();
        try {
            JSONArray arr = load(c);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String name = o.optString("name", "");
                String measure = o.optString("measure", "");
                String source = o.optString("source", "");
                if (source.isEmpty()) source = "Ditambahkan manual";
                if (!name.trim().isEmpty())
                    list.add(new GroceryItem(name, measure, source));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean hasItems(Context c) {
        try {
            return load(c).length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void clear(Context c) {
        prefs(c).edit().remove(Constants.KEY_EXTRA_GROCERY).apply();
    }

    private static boolean contains(JSONArray arr, String name, String measure, String source)
            throws Exception {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (name.equalsIgnoreCase(o.optString("name", ""))
                    && measure.equalsIgnoreCase(o.optString("measure", ""))
                    && source.equalsIgnoreCase(o.optString("source", ""))) {
                return true;
            }
        }
        return false;
    }

    private static JSONArray load(Context c) throws Exception {
        String json = prefs(c).getString(Constants.KEY_EXTRA_GROCERY, "[]");
        return new JSONArray(json);
    }
}