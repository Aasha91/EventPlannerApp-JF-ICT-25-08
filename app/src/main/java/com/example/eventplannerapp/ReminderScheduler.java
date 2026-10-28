package com.example.eventplannerapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {

public static void scheduleReminder(
            Context context,
            int eventId,
            String eventName,
            String eventDate,
            String eventTime) {

        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            format.setLenient(false);

            Date eventDateTime =
                    format.parse(eventDate + " " + eventTime);

            if (eventDateTime == null) {
                return;
            }

            long reminderTime = eventDateTime.getTime();

            // Don't schedule reminders for past events
            if (reminderTime <= System.currentTimeMillis()) {
                cancelReminder(context, eventId);
                return;
            }

            Intent intent = new Intent(context, ReminderReceiver.class);

            intent.putExtra("event_id", eventId);
            intent.putExtra("event_name", eventName);
            intent.putExtra("event_date", eventDate);
            intent.putExtra("event_time", eventTime);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    eventId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= 31
                        && alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );

                } else {
                    // Fallback when exact-alarm access is unavailable
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, int eventId) {

        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                eventId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }

            pendingIntent.cancel();
        }
    }

}
