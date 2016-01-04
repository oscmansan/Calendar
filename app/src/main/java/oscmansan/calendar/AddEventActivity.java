package oscmansan.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddEventActivity.class.getSimpleName();

    private EditText edit_event_title;
    private Calendar beginDate;
    private TextView edit_date;
    private TextView edit_begin_time;
    private Calendar endDate;
    private TextView edit_end_time;
    private EditText edit_event_description;
    private Button save_button;
    private long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        calID = getIntent().getLongExtra("calID",-1);
        Log.d(LOG_TAG, "calID: " + calID);

        edit_event_title = (EditText)findViewById(R.id.edit_event_title);
        edit_event_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_title && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        beginDate = Calendar.getInstance();
        beginDate.add(Calendar.HOUR_OF_DAY, 1);
        beginDate.set(Calendar.MINUTE, 0);

        edit_date = (TextView)findViewById(R.id.edit_date);
        SimpleDateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);
        edit_date.setText(df.format(beginDate.getTime()));
        edit_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = beginDate.get(Calendar.YEAR);
                int month = beginDate.get(Calendar.MONTH);
                int day_of_month = beginDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(AddEventActivity.this, beginDateSetListener, year, month, day_of_month);
                datePicker.show();
            }
        });

        edit_begin_time = (TextView)findViewById(R.id.edit_begin_time);
        edit_begin_time.setText(String.format("%02d", beginDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format("%02d", beginDate.get(Calendar.MINUTE)));
        edit_begin_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = beginDate.get(Calendar.HOUR_OF_DAY);
                int minute = beginDate.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(AddEventActivity.this, beginTimeSetListener, hour, minute, true);
                timePicker.show();
            }
        });

        endDate = Calendar.getInstance();
        endDate.add(Calendar.HOUR_OF_DAY, 2);
        endDate.set(Calendar.MINUTE, 0);

        edit_end_time = (TextView)findViewById(R.id.edit_end_time);
        edit_end_time.setText(String.format("%02d", endDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format("%02d", endDate.get(Calendar.MINUTE)));
        edit_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = endDate.get(Calendar.HOUR_OF_DAY);
                int minute = endDate.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(AddEventActivity.this, endTimeSetListener, hour, minute, true);
                timePicker.show();
            }
        });

        edit_event_description = (EditText) findViewById(R.id.edit_event_description);
        edit_event_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_description && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        save_button = (Button)findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertEvent();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
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

    private DatePickerDialog.OnDateSetListener beginDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            SimpleDateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);

            beginDate.set(year, monthOfYear, dayOfMonth);
            edit_date.setText(df.format(beginDate.getTime()));
            endDate.set(year, monthOfYear, dayOfMonth);
        }
    };

    private TimePickerDialog.OnTimeSetListener beginTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            beginDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            beginDate.set(Calendar.MINUTE, minute);
            edit_begin_time.setText(String.format("%02d", beginDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                    String.format("%02d", beginDate.get(Calendar.MINUTE)));

            endDate.set(Calendar.HOUR_OF_DAY, hourOfDay+1);
            endDate.set(Calendar.MINUTE,minute);
            edit_end_time.setText(String.format("%02d", endDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                    String.format("%02d", endDate.get(Calendar.MINUTE)));
        }
    };

    private TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            endDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            endDate.set(Calendar.MINUTE, minute);
            edit_end_time.setText(String.format("%02d", endDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                    String.format("%02d", endDate.get(Calendar.MINUTE)));

            if (beginDate.getTimeInMillis() > endDate.getTimeInMillis()) {
                beginDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                beginDate.set(Calendar.MINUTE, minute);
                edit_begin_time.setText(String.format("%02d", beginDate.get(Calendar.HOUR_OF_DAY)) + ":" +
                        String.format("%02d", beginDate.get(Calendar.MINUTE)));
            }
        }
    };

    private void insertEvent() {
        if (edit_event_title.getText().toString().equals("")) {
            Toast.makeText(this,"Error: title cannot be blank",Toast.LENGTH_LONG).show();
            return;
        }

        long startMillis = beginDate.getTimeInMillis();
        long endMillis = endDate.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.TITLE, edit_event_title.getText().toString());
        values.put(Events.DESCRIPTION, edit_event_description.getText().toString());
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "Europe/Madrid");
        values.put(Events.SYNC_DATA1, "event");

        Uri.Builder builder = Events.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(Events.ACCOUNT_NAME,"some.account@googlemail.com");
        builder.appendQueryParameter(Events.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true");

        Uri uri = getContentResolver().insert(builder.build(), values);
        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());
        Toast.makeText(this,"Event added: " + eventID, Toast.LENGTH_SHORT).show();
        finish();
    }
}
