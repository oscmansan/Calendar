package oscmansan.calendar;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Calendar;

public class CalendarProviderHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Context context;
    private ContentResolver cr;
    private MyQueryHandler queryHandler;
    private long calID;

    CalendarProviderHandler(Context context) {
        this.context = context;
        cr = context.getContentResolver();
        queryHandler = new MyQueryHandler(cr);

        if (getCalendarID() == -1)
            insertCalendar();
        calID = getCalendarID();
    }

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private void showCalendars() {
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
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

        Uri uri = cr.insert(builder.build(),values);
        long calendarID = Long.parseLong(uri.getLastPathSegment());
        Log.d(LOG_TAG, "Calendar inserted: " + calendarID);
    }

    private void deleteCalendar() {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calID);
        int rows = context.getContentResolver().delete(deleteUri, null, null);
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
        Cursor cur = cr.query(CalendarContract.Calendars.CONTENT_URI, projection, selection, selectionArgs, null);

        long calID = -1;
        if (cur.moveToNext()) {
            calID = cur.getLong(0);
        }
        cur.close();
        return calID;
    }

    private void showEvents() {
        String[] projection = {CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.STATUS};
        String selection = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = {String.valueOf(calID)};

        queryHandler.startQuery(1,null, CalendarContract.Events.CONTENT_URI,projection,selection,selectionArgs,null);
    }

    private void insertEvent() {
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2015, 12, 25, 12, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2015, 12, 25, 13, 0);
        long endMillis = endTime.getTimeInMillis();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, "Test Event");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Madrid");
        values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE);

        queryHandler.startInsert(1, null, CalendarContract.Events.CONTENT_URI, values);
    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
        queryHandler.startDelete(1, null, deleteUri, null, null);
    }

    private void deleteAllEvents() {
        String[] projection = {CalendarContract.Events._ID};
        String selection = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = {String.valueOf(calID)};
        Cursor cur = cr.query(CalendarContract.Events.CONTENT_URI, projection,selection,selectionArgs,null);

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
