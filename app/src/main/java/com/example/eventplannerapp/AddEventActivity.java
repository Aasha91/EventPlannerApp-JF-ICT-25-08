package com.example.eventplannerapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventDescriptionEditText;
    private Button selectDateButton;
    private Button selectTimeButton;
    private Button saveEventButton;

    private String selectedDate = "";
    private String selectedTime = "";

    private int eventId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        saveEventButton = findViewById(R.id.saveEventButton);

        // Check whether an existing event is being edited
        Intent intent = getIntent();

        if (intent.hasExtra("event_id")) {
            isEditMode = true;

            eventId = intent.getIntExtra("event_id", -1);

            eventNameEditText.setText(
                    intent.getStringExtra("event_name")
            );

            selectedDate = intent.getStringExtra("event_date");
            selectedTime = intent.getStringExtra("event_time");

            eventDescriptionEditText.setText(
                    intent.getStringExtra("event_description")
            );

            if (selectedDate != null) {
                selectDateButton.setText(selectedDate);
            }

            if (selectedTime != null) {
                selectTimeButton.setText(selectedTime);
            }

            saveEventButton.setText("Update Event");
        }

        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        saveEventButton.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    selectDateButton.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    selectTimeButton.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void saveEvent() {
        String name = eventNameEditText.getText().toString().trim();

        String description = eventDescriptionEditText
                .getText().toString().trim();

        if (name.isEmpty()) {
            eventNameEditText.setError("Please enter event name");
            eventNameEditText.requestFocus();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select event date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select event time",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (description.isEmpty()) {
            eventDescriptionEditText.setError(
                    "Please enter event description"
            );
            eventDescriptionEditText.requestFocus();
            return;
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra("event_id", eventId);
        resultIntent.putExtra("event_name", name);
        resultIntent.putExtra("event_date", selectedDate);
        resultIntent.putExtra("event_time", selectedTime);
        resultIntent.putExtra("event_description", description);
        resultIntent.putExtra("is_edit_mode", isEditMode);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}