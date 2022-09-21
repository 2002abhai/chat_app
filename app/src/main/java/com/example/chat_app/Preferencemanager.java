package com.example.chat_app;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencemanager {

    private final SharedPreferences preferences;


    public Preferencemanager(Context context) {
        preferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public Boolean getBoolean(String key){
       return preferences.getBoolean(key,false);
    }

    public void putString(String key, String value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public String getString(String value){
        return preferences.getString(value,null);
    }

    void clear(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
