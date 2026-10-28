package com.example.eventplannerapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private TextView emptyTextView;
    private RecyclerView eventsRecyclerView;
    private Button addEventButton;

    private TextView pastEmptyTextView;
    private RecyclerView pastEventsRecyclerView;

    private ArrayList<Event> eventList;
    private ArrayList<Event> upcomingEvents;
    private ArrayList<Event> pastEvents;

    private EventAdapter upcomingAdapter;
    private EventAdapter pastAdapter;

    private int nextEventId = 1;

    private ActivityResultLauncher<Intent> addEventLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }

        searchEditText = findViewById(R.id.searchEditText);
        emptyTextView = findViewById(R.id.emptyTextView);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        addEventButton = findViewById(R.id.addEventButton);

        pastEmptyTextView = findViewById(R.id.pastEmptyTextView);
        pastEventsRecyclerView = findViewById(R.id.pastEventsRecyclerView);

        eventList = EventStorage.loadEvents(this);

        for (Event event : eventList) {
            if (event.getId() >= nextEventId) {
                nextEventId = event.getId() + 1;
            }
        }

        upcomingEvents = new ArrayList<>();
        pastEvents = new ArrayList<>();

        upcomingAdapter = new EventAdapter(
                upcomingEvents,
                event -> openEditEvent(event)
        );

        pastAdapter = new EventAdapter(
                pastEvents,
                event -> openEditEvent(event)
        );

        // Connect delete callbacks
        upcomingAdapter.setOnDeleteClickListener(this::deleteEvent);
        pastAdapter.setOnDeleteClickListener(this::deleteEvent);

        eventsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        eventsRecyclerView.setAdapter(upcomingAdapter);

        pastEventsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        pastEventsRecyclerView.setAdapter(pastAdapter);

        addEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {

                        Intent data = result.getData();

                        String name = data.getStringExtra("event_name");
                        String date = data.getStringExtra("event_date");
                        String time = data.getStringExtra("event_time");
                        String description =
                                data.getStringExtra("event_description");

                        boolean isEditMode =
                                data.getBooleanExtra("is_edit_mode", false);

                        if (name == null || date == null
                                || time == null || description == null) {

                            Toast.makeText(
                                    this,
                                    "Unable to save event details",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (isEditMode) {

                            int eventId =
                                    data.getIntExtra("event_id", -1);

                            boolean updated = updateEventInList(
                                    eventId,
                                    name,
                                    date,
                                    time,
                                    description
                            );

                            if (updated) {

                                ReminderScheduler.scheduleReminder(
                                        this,
                                        eventId,
                                        name,
                                        date,
                                        time
                                );

                                EventStorage.saveEvents(this, eventList);

                                Toast.makeText(
                                        this,
                                        "Event updated successfully!",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                Toast.makeText(
                                        this,
                                        "Event not found",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {

                            Event event = new Event(
                                    nextEventId++,
                                    name,
                                    date,
                                    time,
                                    description
                            );

                            eventList.add(event);

                            ReminderScheduler.scheduleReminder(
                                    this,
                                    event.getId(),
                                    event.getName(),
                                    event.getDate(),
                                    event.getTime()
                            );

                            EventStorage.saveEvents(this, eventList);

                            Toast.makeText(
                                    this,
                                    "Event created successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        separateEvents();
                    }
                }
        );

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                applySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addEventButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AddEventActivity.class
            );

            addEventLauncher.launch(intent);
        });

        separateEvents();
    }

    // Delete an event from the main list and storage
    private void deleteEvent(Event event) {

        ReminderScheduler.cancelReminder(this, event.getId());

        eventList.removeIf(item -> item.getId() == event.getId());

        EventStorage.saveEvents(this, eventList);

        separateEvents();

        Toast.makeText(
                this,
                "Event deleted successfully!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // Update an existing event
    private boolean updateEventInList(
            int eventId,
            String name,
            String date,
            String time,
            String description) {

        for (Event event : eventList) {

            if (event.getId() == eventId) {

                event.setName(name);
                event.setDate(date);
                event.setTime(time);
                event.setDescription(description);

                return true;
            }
        }

        return false;
    }

    // Open the edit screen
    private void openEditEvent(Event event) {

        Intent intent = new Intent(
                MainActivity.this,
                AddEventActivity.class
        );

        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_name", event.getName());
        intent.putExtra("event_date", event.getDate());
        intent.putExtra("event_time", event.getTime());
        intent.putExtra("event_description", event.getDescription());

        addEventLauncher.launch(intent);
    }

    // Separate upcoming and past events
    private void separateEvents() {

        if (eventList == null || upcomingAdapter == null
                || pastAdapter == null) {
            return;
        }

        upcomingEvents.clear();
        pastEvents.clear();

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
        );

        format.setLenient(false);

        Date now = new Date();

        for (Event event : eventList) {

            try {

                Date eventDateTime = format.parse(
                        event.getDate() + " " + event.getTime()
                );

                if (eventDateTime != null && eventDateTime.after(now)) {
                    upcomingEvents.add(event);
                } else {
                    pastEvents.add(event);
                }

            } catch (ParseException e) {
                // Skip events with invalid date/time.
            }
        }

        Comparator<Event> byDateTime = (event1, event2) -> {

            String dateTime1 =
                    event1.getDate() + " " + event1.getTime();

            String dateTime2 =
                    event2.getDate() + " " + event2.getTime();

            return dateTime1.compareTo(dateTime2);
        };

        Collections.sort(upcomingEvents, byDateTime);
        Collections.sort(pastEvents, byDateTime.reversed());

        upcomingAdapter.setEvents(upcomingEvents);
        pastAdapter.setEvents(pastEvents);

        applySearch(searchEditText.getText().toString());
    }

    // Search upcoming and past events
    private void applySearch(String query) {

        if (upcomingAdapter == null || pastAdapter == null) {
            return;
        }

        upcomingAdapter.filterEvents(query);
        pastAdapter.filterEvents(query);

        updateEmptyMessage();
    }

    // Show or hide empty-list messages
    private void updateEmptyMessage() {

        if (upcomingAdapter == null || pastAdapter == null) {
            return;
        }

        boolean noUpcoming = upcomingAdapter.getItemCount() == 0;
        boolean noPast = pastAdapter.getItemCount() == 0;

        emptyTextView.setVisibility(
                noUpcoming ? View.VISIBLE : View.GONE
        );

        eventsRecyclerView.setVisibility(
                noUpcoming ? View.GONE : View.VISIBLE
        );

        pastEmptyTextView.setVisibility(
                noPast ? View.VISIBLE : View.GONE
        );

        pastEventsRecyclerView.setVisibility(
                noPast ? View.GONE : View.VISIBLE
        );
    }
}

