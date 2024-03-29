package oscmansan.calendar;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {
    
    private static final String LOG_TAG = AddTaskActivity.class.getSimpleName();

    private Calendar date;
    private EditText edit_task_title;
    private EditText edit_task_description;
    private RadioButton daily_radio;
    private TextView select_week;
    private Button save_button;
    private long calID;
    private ArrayList<Event> events;
    private long eventID = -1;
    private String eventTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        calID = getIntent().getLongExtra("calID",-1);
        Log.d(LOG_TAG, "calID: " + calID);
        
        edit_task_title = (EditText)findViewById(R.id.edit_task_title);
        edit_task_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_title && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        
        edit_task_description = (EditText)findViewById(R.id.edit_task_description);
        edit_task_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_title && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        Spinner linked_event = (Spinner)findViewById(R.id.linked_event_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        adapter.add("None");
        events = getAllEvents();
        for (Event e : events) {
            adapter.add(e.TITLE);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        linked_event.setAdapter(adapter);

        linked_event.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    eventID = events.get(position-1)._ID;
                    eventTitle = events.get(position-1).TITLE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

       date = Calendar.getInstance();
        String s = "Week " + date.get(Calendar.WEEK_OF_MONTH) + " of " +
                date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + date.get(Calendar.YEAR);
        select_week = (TextView)findViewById(R.id.select_week);
        select_week.setText(s);
        select_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = date.get(Calendar.YEAR);
                int month = date.get(Calendar.MONTH);
                int day_of_month = date.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(AddTaskActivity.this, dateSetListener, year, month, day_of_month);
                datePicker.show();
            }
        });

        daily_radio = (RadioButton)findViewById(R.id.daily_radio);
        daily_radio.setChecked(true);
        daily_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked)
                    select_week.setVisibility(View.VISIBLE);
                else
                    select_week.setVisibility(View.GONE);
            }
        });
        
        save_button = (Button)findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertTask();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            date.set(year, monthOfYear, dayOfMonth);

            String s = "Week " + date.get(Calendar.WEEK_OF_MONTH) + " of " +
                    date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + date.get(Calendar.YEAR);
            select_week.setText(s);
        }
    };

    ArrayList<Event> getAllEvents() {
        String[] projection = {Events._ID, Events.TITLE};
        String selection = Events.CALENDAR_ID + " = ? AND " + Events.SYNC_DATA1 + " = ?";
        String[] selectionArgs = {String.valueOf(calID), "event"};

        Cursor cur = getContentResolver().query(Events.CONTENT_URI,projection,selection,selectionArgs, Events.DTSTART + " ASC");
        ArrayList<Event> events = new ArrayList<>();
        while (cur.moveToNext()) {
            Event event = new Event();
            event._ID = cur.getLong(0);
            event.TITLE = cur.getString(1);
            events.add(event);
        }
        cur.close();

        return events;
    }

    private void insertTask() {
        if (edit_task_title.getText().toString().equals("")) {
            Toast.makeText(this,"Error: title cannot be blank",Toast.LENGTH_LONG).show();
            return;
        }

        Boolean daily = daily_radio.isChecked();

        ContentValues values = new ContentValues();
        if (daily) {
            values.put(Events.DTSTART, 0);
            values.put(Events.DTEND, 0);
            values.put(Events.SYNC_DATA2, "daily");
        }
        else {
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            rollToMonday(date);

            values.put(Events.DTSTART, date.getTimeInMillis());
            values.put(Events.DTEND, date.getTimeInMillis());
            values.put(Events.SYNC_DATA2, "weekly");
        }

        values.put(Events.TITLE, edit_task_title.getText().toString());
        values.put(Events.DESCRIPTION, edit_task_description.getText().toString());
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "Europe/Madrid");
        values.put(Events.STATUS, Events.STATUS_TENTATIVE);
        values.put(Events.SYNC_DATA1, "task");
        if (eventID >= 0)
            values.put(Events.SYNC_DATA3, eventTitle);

        Uri.Builder builder = Events.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(Events.ACCOUNT_NAME,"some.account@googlemail.com");
        builder.appendQueryParameter(Events.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true");

        Uri uri = getContentResolver().insert(builder.build(), values);
        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());
        Log.d(LOG_TAG, "Task added: " + eventID);
        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void rollToMonday(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        int delta = day_of_week - Calendar.MONDAY;
        if (delta < 0) delta += 7;
        c.add(Calendar.DAY_OF_WEEK, -delta);
    }

    private class Event {
        long _ID;
        String TITLE;
    }
}
