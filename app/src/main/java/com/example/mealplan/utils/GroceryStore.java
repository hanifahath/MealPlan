package com.example.mealplan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mealplan.model.GroceryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // ===== Status centang (checked) bahan belanja =====
    // Disimpan terpisah sebagai kumpulan "key" unik agar status "sudah dibeli"
    // tetap bertahan walau daftar di-generate ulang setiap onResume().
    private static String keyOf(String name, String measure, String source) {
        return (name == null ? "" : name.trim()) + "|"
                + (measure == null ? "" : measure.trim()) + "|"
                + (source == null ? "" : source.trim());
    }

    public static String keyOf(GroceryItem item) {
        if (item == null) return "";
        return keyOf(item.getName(), item.getMeasure(), item.getSourceMeal());
    }

    public static Set<String> loadCheckedKeys(Context c) {
        Set<String> set = new HashSet<>();
        try {
            String json = prefs(c).getString(Constants.KEY_GROCERY_CHECKED, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String k = arr.optString(i, "");
                if (!k.isEmpty()) set.add(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    private static void saveCheckedKeys(Context c, Set<String> keys) {
        JSONArray arr = new JSONArray();
        for (String k : keys) arr.put(k);
        prefs(c).edit().putString(Constants.KEY_GROCERY_CHECKED, arr.toString()).apply();
    }

    public static boolean isChecked(Context c, GroceryItem item) {
        return loadCheckedKeys(c).contains(keyOf(item));
    }

    public static void setChecked(Context c, GroceryItem item, boolean checked) {
        String key = keyOf(item);
        if (key.isEmpty()) return;
        Set<String> keys = loadCheckedKeys(c);
        if (checked) keys.add(key); else keys.remove(key);
        saveCheckedKeys(c, keys);
    }

    public static void clearCheckedKeys(Context c) {
        prefs(c).edit().remove(Constants.KEY_GROCERY_CHECKED).apply();
    }

    public static Set<String> loadRemovedKeys(Context c) {
        Set<String> set = new HashSet<>();
        try {
            String json = prefs(c).getString(Constants.KEY_GROCERY_REMOVED, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String k = arr.optString(i, "");
                if (!k.isEmpty()) set.add(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    private static void saveRemovedKeys(Context c, Set<String> keys) {
        JSONArray arr = new JSONArray();
        for (String k : keys) arr.put(k);
        prefs(c).edit().putString(Constants.KEY_GROCERY_REMOVED, arr.toString()).apply();
    }

    public static void addRemovedKey(Context c, GroceryItem item) {
        String key = keyOf(item);
        if (key.isEmpty()) return;
        Set<String> keys = loadRemovedKeys(c);
        keys.add(key);
        saveRemovedKeys(c, keys);
    }

    public static void clearRemovedKeys(Context c) {
        prefs(c).edit().remove(Constants.KEY_GROCERY_REMOVED).apply();
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