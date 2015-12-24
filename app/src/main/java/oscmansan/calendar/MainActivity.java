package oscmansan.calendar;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ListView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ContentResolver cr;
    private MyQueryHandler queryHandler;

    //private ListView listView;
    //private EventAdapter adapter;
    private Button insert_button;
    private Button delete_button;
    private long calID;
    private DatePicker datePicker;
    private ListView week;
    private DayAdapter dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cr = getContentResolver();
        queryHandler = new MyQueryHandler(cr);

        /*listView = (ListView)findViewById(R.id.list);
        adapter = new EventAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);*/

        week = (ListView)findViewById(R.id.week);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker = (DatePicker)findViewById(R.id.date_picker);
        datePicker.init(year, month, day, dateSetListener);

        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int d = date.get(Calendar.DAY_OF_MONTH);
        Integer[] days_of_month = {d,d+1,d+2,d+3,d+4,d+5,d+6};
        dayAdapter = new DayAdapter(this,days_of_month);
        week.setAdapter(dayAdapter);

        insert_button = (Button)findViewById(R.id.insert_button);
        insert_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //insertEvent();
            }
        });

        delete_button = (Button)findViewById(R.id.delete_button);
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteAllEvents();
            }
        });

        if (getCalendarID() == -1)
            insertCalendar();
        calID = getCalendarID();
    }

    private DatePicker.OnDateChangedListener dateSetListener = new DatePicker.OnDateChangedListener() {

        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d(LOG_TAG, "year: " + year + "  month: " + monthOfYear + "  day: " + dayOfMonth);
            Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);

            int day_of_week = c.get(Calendar.DAY_OF_WEEK);
            c.roll(Calendar.DAY_OF_WEEK,-(day_of_week-Calendar.MONDAY));
            int d = c.get(Calendar.DAY_OF_MONTH);
            Integer[] days_of_month = {d,d+1,d+2,d+3,d+4,d+5,d+6};
            DayAdapter dayAdapter = new DayAdapter(MainActivity.this,days_of_month);
            dayAdapter.notifyDataSetChanged();
            week.setAdapter(dayAdapter);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        //showCalendars();
        //showEvents();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private void showCalendars() {
        Uri uri = Calendars.CONTENT_URI;
        // Submit the query and get a Cursor object back.
        Cursor cur = cr.query(uri, EVENT_PROJECTION, null, null, null);
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            // Get the field values
            long calID = cur.getLong(PROJECTION_ID_INDEX);
            String displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            String accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            String ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            // Do something with the values...
            Log.d(LOG_TAG, calID + " \t" + displayName + " \t" + accountName + " \t" + ownerName + "\n\n");
        }
        cur.close();
    }

    private void insertCalendar() {
        ContentValues values = new ContentValues();
        values.put(Calendars.ACCOUNT_NAME,"some.account@googlemail.com");
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(Calendars.NAME,"Local Calendar");
        values.put(Calendars.CALENDAR_DISPLAY_NAME,"Local Calendar");
        values.put(Calendars.CALENDAR_COLOR,0xffff0000);
        values.put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER);
        values.put(Calendars.OWNER_ACCOUNT,"some.account@googlemail.com");
        values.put(Calendars.CALENDAR_TIME_ZONE,"Europe/Madrid");
        values.put(Calendars.SYNC_EVENTS, 1);

        Uri.Builder builder = Calendars.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(Calendars.ACCOUNT_NAME,"some.account@googlemail.com");
        builder.appendQueryParameter(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true");

        Uri uri = cr.insert(builder.build(),values);
        long calendarID = Long.parseLong(uri.getLastPathSegment());
        Log.d(LOG_TAG, "Calendar inserted: " + calendarID);
    }

    private void deleteCalendar() {
        Uri deleteUri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calID);
        int rows = getContentResolver().delete(deleteUri, null, null);
        Log.d(LOG_TAG, "Rows deleted: " + rows);
    }

    private long getCalendarID() {
        String[] projection = {Calendars._ID};
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                                + Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = {
                "some.account@googlemail.com",
                CalendarContract.ACCOUNT_TYPE_LOCAL
        };
        Cursor cur = cr.query(Calendars.CONTENT_URI, projection, selection, selectionArgs, null);

        long calID = -1;
        if (cur.moveToNext()) {
            calID = cur.getLong(0);
        }
        cur.close();
        return calID;
    }

    private void showEvents() {
        String[] projection = {Events._ID, Events.TITLE, Events.STATUS};
        String selection = "(" + Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = {String.valueOf(calID)};

        queryHandler.startQuery(1,null,Events.CONTENT_URI,projection,selection,selectionArgs,null);
    }

    private void insertEvent() {
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2015, 12, 25, 12, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2015, 12, 25, 13, 0);
        long endMillis = endTime.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.TITLE, "Test Event");
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "Europe/Madrid");
        values.put(Events.STATUS,Events.STATUS_TENTATIVE);

        queryHandler.startInsert(1, null, Events.CONTENT_URI, values);
    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        queryHandler.startDelete(1, null, deleteUri, null, null);
    }

    private void deleteAllEvents() {
        String[] projection = {Events._ID};
        String selection = "(" + Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = {String.valueOf(calID)};
        Cursor cur = cr.query(Events.CONTENT_URI, projection,selection,selectionArgs,null);

        while (cur.moveToNext()) {
            long eventID = cur.getLong(0);
            deleteEvent(eventID);
        }
        cur.close();
    }

    private class MyQueryHandler extends AsyncQueryHandler {

        public MyQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cur) {
            //adapter.swapCursor(cur);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
            Log.d(LOG_TAG, "Event inserted: " + eventID);
            showEvents();
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            Log.d(LOG_TAG, "Rows deleted: " + result);
            showEvents();
        }
    }
}
