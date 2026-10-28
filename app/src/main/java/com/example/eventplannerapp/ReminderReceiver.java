package com.example.eventplannerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int eventId = intent.getIntExtra("event_id", 0);
        String eventName = intent.getStringExtra("event_name");
        String eventDate = intent.getStringExtra("event_date");
        String eventTime = intent.getStringExtra("event_time");

        if (eventName == null) {
            eventName = "Your Event";
        }

        if (eventDate == null) {
            eventDate = "";
        }

        if (eventTime == null) {
            eventTime = "";
        }

        NotificationHelper.createNotificationChannel(context);

        NotificationHelper.showNotification(
                context,
                eventId,
                eventName,
                eventDate,
                eventTime
        );
    }
}

