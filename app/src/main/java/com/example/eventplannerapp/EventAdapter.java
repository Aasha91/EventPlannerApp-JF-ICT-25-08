package com.example.eventplannerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final ArrayList<Event> eventList;
    private final ArrayList<Event> fullEventList;

    // Interface to send the selected event to MainActivity for editing
    public interface OnEditClickListener {
        void onEditClick(Event event);
    }

    private final OnEditClickListener editClickListener;


    // Delete event callback
    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    private OnDeleteClickListener deleteClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }


    public EventAdapter(ArrayList<Event> eventList,
                        OnEditClickListener editClickListener) {

        this.eventList = eventList;
        this.fullEventList = new ArrayList<>(eventList);
        this.editClickListener = editClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EventViewHolder holder, int position) {

        Event event = eventList.get(position);

        holder.eventNameTextView.setText(event.getName());

        holder.eventDateTimeTextView.setText(
                event.getDate() + " • " + event.getTime()
        );

        holder.eventDescriptionTextView.setText(event.getDescription());

        // Edit event
        holder.editEventButton.setOnClickListener(v -> {

            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Event selectedEvent = eventList.get(currentPosition);

            if (editClickListener != null) {
                editClickListener.onEditClick(selectedEvent);
            }
        });

        // Delete event with confirmation
        holder.deleteEventButton.setOnClickListener(v -> {

            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            Event selectedEvent = eventList.get(currentPosition);

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        if (deleteClickListener != null) {
                            deleteClickListener.onDeleteClick(selectedEvent);
                        }

                    })
                    .setNegativeButton("Cancel", (dialog, which) ->
                            dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public ArrayList<Event> getAllEvents() {
        return new ArrayList<>(fullEventList);
    }

    public void setEvents(ArrayList<Event> events) {

        ArrayList<Event> newEvents = new ArrayList<>(events);

        fullEventList.clear();
        fullEventList.addAll(newEvents);

        eventList.clear();
        eventList.addAll(newEvents);

        notifyDataSetChanged();
    }


    // Search events by name, date, time or description
    public void filterEvents(String query) {

        String searchText = query.toLowerCase(Locale.ROOT).trim();

        eventList.clear();

        if (searchText.isEmpty()) {

            eventList.addAll(fullEventList);

        } else {

            for (Event event : fullEventList) {

                if (event.getName().toLowerCase(Locale.ROOT).contains(searchText)
                        || event.getDate().contains(searchText)
                        || event.getTime().contains(searchText)
                        || event.getDescription()
                        .toLowerCase(Locale.ROOT).contains(searchText)) {

                    eventList.add(event);
                }
            }
        }

        notifyDataSetChanged();
    }

    // Add a new event
    public void addEvent(Event event) {

        fullEventList.add(event);
        eventList.add(event);

        notifyDataSetChanged();
    }

    // Update an existing event using its ID
    public boolean updateEvent(int eventId, String name,
                               String date, String time,
                               String description) {

        for (Event event : fullEventList) {

            if (event.getId() == eventId) {

                event.setName(name);
                event.setDate(date);
                event.setTime(time);
                event.setDescription(description);

                // Re-sort after updating the event
                sortFullEventList();

                // Refresh the visible list
                filterEvents("");

                return true;
            }
        }

        return false;
    }

    // Sort events by date and time
    private void sortFullEventList() {

        Collections.sort(fullEventList, new Comparator<Event>() {
            @Override
            public int compare(Event event1, Event event2) {

                String dateTime1 =
                        event1.getDate() + " " + event1.getTime();

                String dateTime2 =
                        event2.getDate() + " " + event2.getTime();

                return dateTime1.compareTo(dateTime2);
            }
        });
    }

    public void sortEvents() {

        sortFullEventList();
        filterEvents("");
    }

    public void refreshEvents() {
        notifyDataSetChanged();
    }

    // ViewHolder
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventNameTextView;
        TextView eventDateTimeTextView;
        TextView eventDescriptionTextView;

        Button editEventButton;
        Button deleteEventButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventNameTextView =
                    itemView.findViewById(R.id.eventNameTextView);

            eventDateTimeTextView =
                    itemView.findViewById(R.id.eventDateTimeTextView);

            eventDescriptionTextView =
                    itemView.findViewById(R.id.eventDescriptionTextView);

            editEventButton =
                    itemView.findViewById(R.id.editEventButton);

            deleteEventButton =
                    itemView.findViewById(R.id.deleteEventButton);
        }
    }
}