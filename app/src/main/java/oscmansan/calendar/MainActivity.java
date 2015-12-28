package oscmansan.calendar;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Calendar c;
    private ListView week;
    private TextView title;
    private long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        c = Calendar.getInstance();

        week = (ListView)findViewById(R.id.week);
        title = (TextView)findViewById(R.id.title);

        if (getCalendarID() == -1)
            insertCalendar();
        calID = getCalendarID();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showWeek(c);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.pick_date:
                pickDate();
                return true;
            case R.id.add_event:
                Intent intent = new Intent(this,AddEventActivity.class);
                intent.putExtra("calID", calID);
                startActivity(intent);
                return true;
            case R.id.delete_all_events:
                deleteAllEvents();
                showWeek(c);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void pickDate() {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this,dateSetListener,year,month,day);
        datePicker.show();
    }

    void showWeek(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        int delta = day_of_week - Calendar.MONDAY;
        if (delta < 0) delta += 7;
        c.add(Calendar.DAY_OF_WEEK, -delta);
        Log.d(LOG_TAG, c.toString());
        ArrayList<Calendar> days = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            days.add((Calendar) c.clone());
            c.add(Calendar.DAY_OF_WEEK, 1);
        }
        c.add(Calendar.DAY_OF_WEEK, -7);
        week.setAdapter(new DayAdapter(MainActivity.this, calID, days));

        String s = c.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.US) + " of " + c.get(Calendar.YEAR);
        title.setText(s);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d(LOG_TAG, "year: " + year + "  month: " + monthOfYear + "  day: " + dayOfMonth);
            c.set(year, monthOfYear, dayOfMonth);
            showWeek(c);
        }
    };

    private void insertCalendar() {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME,"some.account@googlemail.com");
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME,"Local Calendar");
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,"Local Calendar");
        values.put(CalendarContract.Calendars.CALENDAR_COLOR,0xffff0000);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT,"some.account@googlemail.com");
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE,"Europe/Madrid");
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        Uri.Builder builder = CalendarContract.Calendars.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME,"some.account@googlemail.com");
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true");

        Uri uri = getContentResolver().insert(builder.build(), values);
        long calendarID = Long.parseLong(uri.getLastPathSegment());
        Log.d(LOG_TAG, "Calendar inserted: " + calendarID);
    }

    private void deleteCalendar() {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calID);
        int rows = getContentResolver().delete(deleteUri, null, null);
        Log.d(LOG_TAG, "Rows deleted: " + rows);
    }

    private long getCalendarID() {
        String[] projection = {CalendarContract.Calendars._ID};
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = {
                "some.account@googlemail.com",
                CalendarContract.ACCOUNT_TYPE_LOCAL
        };
        Cursor cur = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);

        long calID = -1;
        if (cur.moveToNext()) {
            calID = cur.getLong(0);
        }
        cur.close();
        return calID;
    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        int rows = getContentResolver().delete(deleteUri, null, null);
        Log.d(LOG_TAG, "Rows deleted: " + rows);
    }

    private void deleteAllEvents() {
        String[] projection = {CalendarContract.Events._ID};
        String selection = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = {String.valueOf(calID)};
        Cursor cur = getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            long eventID = cur.getLong(0);
            deleteEvent(eventID);
        }
        cur.close();
    }

}
