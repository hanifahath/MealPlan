package com.example.mealplan.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Kamus lokal untuk menerjemahkan kosakata TETAP dari TheMealDB (Inggris) ke Indonesia.
 * Hanya dipakai untuk TAMPILAN (setText). Nilai asli (Inggris) tetap dipakai untuk
 * pemanggilan API / filter, sehingga tidak mengubah perilaku jaringan.
 */
public final class LocaleMapper {

    private LocaleMapper() {}

    private static final Map<String, String> CATEGORY = new HashMap<>();
    private static final Map<String, String> AREA = new HashMap<>();

    static {
        // Kategori (categories.php)
        CATEGORY.put("Beef", "Daging Sapi");
        CATEGORY.put("Breakfast", "Sarapan");
        CATEGORY.put("Chicken", "Ayam");
        CATEGORY.put("Dessert", "Hidangan Penutup");
        CATEGORY.put("Goat", "Daging Kambing");
        CATEGORY.put("Lamb", "Daging Domba");
        CATEGORY.put("Miscellaneous", "Lainnya");
        CATEGORY.put("Pasta", "Pasta");
        CATEGORY.put("Pork", "Daging Babi");
        CATEGORY.put("Seafood", "Makanan Laut");
        CATEGORY.put("Side", "Hidangan Pendamping");
        CATEGORY.put("Starter", "Hidangan Pembuka");
        CATEGORY.put("Vegan", "Vegan");
        CATEGORY.put("Vegetarian", "Vegetarian");

        // Asal / area (list.php?a=list)
        AREA.put("American", "Amerika");
        AREA.put("British", "Inggris");
        AREA.put("Canadian", "Kanada");
        AREA.put("Chinese", "Tiongkok");
        AREA.put("Croatian", "Kroasia");
        AREA.put("Dutch", "Belanda");
        AREA.put("Egyptian", "Mesir");
        AREA.put("Filipino", "Filipina");
        AREA.put("French", "Prancis");
        AREA.put("Greek", "Yunani");
        AREA.put("Indian", "India");
        AREA.put("Irish", "Irlandia");
        AREA.put("Italian", "Italia");
        AREA.put("Jamaican", "Jamaika");
        AREA.put("Japanese", "Jepang");
        AREA.put("Kenyan", "Kenya");
        AREA.put("Malaysian", "Malaysia");
        AREA.put("Mexican", "Meksiko");
        AREA.put("Moroccan", "Maroko");
        AREA.put("Polish", "Polandia");
        AREA.put("Portuguese", "Portugal");
        AREA.put("Russian", "Rusia");
        AREA.put("Spanish", "Spanyol");
        AREA.put("Thai", "Thailand");
        AREA.put("Tunisian", "Tunisia");
        AREA.put("Turkish", "Turki");
        AREA.put("Ukrainian", "Ukraina");
        AREA.put("Unknown", "Lainnya");
        AREA.put("Vietnamese", "Vietnam");
    }

    public static String category(String en) {
        if (en == null) return null;
        String key = en.trim();
        return CATEGORY.containsKey(key) ? CATEGORY.get(key) : en;
    }

    public static String area(String en) {
        if (en == null) return null;
        String key = en.trim();
        return AREA.containsKey(key) ? AREA.get(key) : en;
    }
}