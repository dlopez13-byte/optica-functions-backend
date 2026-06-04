package com.example.optica.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * DevOps Resilience Utility: Gestión de caché local para mitigar Cold Start de Render.
 * Almacena respuestas JSON de forma persistente.
 */
public class CacheManager {

    private static final String PREF_NAME = "OpticaCache";
    private static final String KEY_PRODUCTS = "cached_products";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public CacheManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public <T> void saveCache(String key, List<T> data) {
        String json = gson.toJson(data);
        sharedPreferences.edit().putString(key, json).apply();
    }

    public <T> List<T> getCache(String key, Type type) {
        String json = sharedPreferences.getString(key, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, type);
    }

    public static Type getProductListType() {
        return new TypeToken<List<com.example.optica.model.Product>>() {}.getType();
    }
    
    public static final String KEY_CATALOG = "catalog_cache";
}
