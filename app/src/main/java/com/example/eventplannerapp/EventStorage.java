package com.example.eventplannerapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class EventStorage {

    private static final String PREF_NAME = "EventPlannerPreferences";
    private static final String KEY_EVENTS = "saved_events";

    // Save all events
    public static void saveEvents(Context context, ArrayList<Event> events) {

        SharedPreferences preferences = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE
        );

        JSONArray jsonArray = new JSONArray();

        try {
            for (Event event : events) {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("id", event.getId());
                jsonObject.put("name", event.getName());
                jsonObject.put("date", event.getDate());
                jsonObject.put("time", event.getTime());
                jsonObject.put("description", event.getDescription());

                jsonArray.put(jsonObject);
            }

            preferences.edit()
                    .putString(KEY_EVENTS, jsonArray.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load saved events
    public static ArrayList<Event> loadEvents(Context context) {

        ArrayList<Event> events = new ArrayList<>();

        SharedPreferences preferences = context.getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE
        );

        String savedData = preferences.getString(KEY_EVENTS, "[]");

        try {
            JSONArray jsonArray = new JSONArray(savedData);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                String name = jsonObject.getString("name");
                String date = jsonObject.getString("date");
                String time = jsonObject.getString("time");
                String description = jsonObject.getString("description");

                Event event = new Event(
                        id, name, date, time, description
                );

                events.add(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return events;
    }
}
