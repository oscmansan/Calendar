package oscmansan.calendar;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DayActivity extends AppCompatActivity {

    private static final String LOG_TAG = DayActivity.class.getSimpleName();

    private Calendar c;
    private long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        this.setTitle("");

        c = Calendar.getInstance();
        c.setTimeInMillis(getIntent().getLongExtra("day",-1));
        calID = getIntent().getLongExtra("calID", -1);

        ((TextView)findViewById(R.id.day_of_week)).setText(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US));
        ((TextView)findViewById(R.id.day_of_month)).setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
        String month_year = c.getDisplayName(Calendar.MONTH,Calendar.LONG,Locale.US) + " " + c.get(Calendar.YEAR);
        ((TextView)findViewById(R.id.month_year)).setText(month_year);

        setEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day, menu);
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

    private void setEvents() {
        LinearLayout eventList = (LinearLayout)findViewById(R.id.events_detail);
        eventList.removeAllViews();
        final Cursor cursor = getEventsOnDay(c);
        while (cursor.moveToNext()) {
            String type = cursor.getString(5);
            if (type.equals("event")) {
                setEvent(cursor,eventList);
            }
            else if (type.equals("task")) {
                setTask(cursor,eventList);
            }
        }
        cursor.close();
    }

    private void setEvent(Cursor cursor, LinearLayout eventList) {
        View event = LayoutInflater.from(this).inflate(R.layout.event, eventList, false);

        final long eventID = cursor.getLong(0);
        final String title = cursor.getString(1);
        final String description = cursor.getString(2);
        ((TextView) event.findViewById(R.id.event_title)).setText(title);

        final Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(cursor.getLong(3));
        final Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(cursor.getLong(4));
        final String time =
                String.format("%02d", beginTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                        String.format("%02d", beginTime.get(Calendar.MINUTE)) + " - " +
                        String.format("%02d", endTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                        String.format("%02d", endTime.get(Calendar.MINUTE));
        ((TextView) event.findViewById(R.id.event_time)).setText(time);

        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DayActivity.this);

                View dialog_layout = getLayoutInflater().inflate(R.layout.event_detail, null);
                ((TextView) dialog_layout.findViewById(R.id.event_detail_title)).setText(title);
                SimpleDateFormat df = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);

                TextView event_detail_date = (TextView) dialog_layout.findViewById(R.id.event_detail_date);
                TextView event_detail_time = (TextView) dialog_layout.findViewById(R.id.event_detail_time);
                if (sameDay(beginTime,endTime)) {
                    event_detail_date.setText(df.format(beginTime.getTime()));
                    event_detail_time.setText(time);
                }
                else {
                    event_detail_date.setText(df.format(beginTime.getTime()) + " -");
                    event_detail_time.setText(df.format(endTime.getTime()));
                }

                TextView event_detail_description = (TextView) dialog_layout.findViewById(R.id.event_detail_description);
                if (!description.equals(""))
                    event_detail_description.setText(description);
                else
                    event_detail_description.setText("No description");

                builder.setView(dialog_layout);
                builder.create().show();
            }
        });

        event.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DayActivity.this);
                builder.setItems(new String[]{"Delete event"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEvent(eventID);
                        setEvents();
                    }
                });
                builder.create().show();

                return true;
            }
        });

        eventList.addView(event);
    }

    private void setTask(Cursor cursor, LinearLayout eventList) {
        View task = LayoutInflater.from(this).inflate(R.layout.task, eventList, false);

        final long eventID = cursor.getLong(0);
        final String title = cursor.getString(1);
        final String description = cursor.getString(2);
        ((TextView) task.findViewById(R.id.task_title)).setText(title);

        final int status = cursor.getInt(6);
        if (status == Events.STATUS_CONFIRMED) {
            task.findViewById(R.id.checkbox).setVisibility(View.VISIBLE);
        }

        final String frequency;
        if (cursor.getString(7).equals("daily"))
            frequency = "Daily";
        else
            frequency = "For a week";

        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DayActivity.this);

                View dialog_layout = getLayoutInflater().inflate(R.layout.task_detail, null);
                ((TextView)dialog_layout.findViewById(R.id.task_detail_title)).setText(title);
                ((TextView)dialog_layout.findViewById(R.id.task_detail_frequency)).setText(frequency);
                TextView task_detail_description = (TextView)dialog_layout.findViewById(R.id.task_detail_description);
                if (!description.equals(""))
                    task_detail_description.setText(description);
                else
                    task_detail_description.setText("No description");

                builder.setView(dialog_layout);
                builder.create().show();
            }
        });

        task.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DayActivity.this);
                String[] items;
                if (status == Events.STATUS_TENTATIVE)
                    items = new String[]{"Delete task", "Mark as done"};
                else
                    items = new String[]{"Delete task"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                deleteEvent(eventID);
                                break;
                            case 1:
                                markAsDone(eventID);
                                break;
                        }
                        setEvents();
                    }
                });
                builder.create().show();

                return true;
            }
        });

        eventList.addView(task);
    }

    private Cursor getEventsOnDay(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        Calendar dayStart = Calendar.getInstance();
        dayStart.set(year, month, day, 0, 0, 0);

        Calendar dayEnd = Calendar.getInstance();
        dayEnd.set(year, month, day, 23, 59, 59);

        Calendar weekStart = Calendar.getInstance();
        weekStart.set(year, month, day, 0, 0, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        rollToMonday(weekStart);

        Calendar weekEnd = (Calendar)weekStart.clone();
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);
        weekEnd.add(Calendar.DAY_OF_WEEK, 6);

        String[] projection = {Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.SYNC_DATA1, Events.STATUS, Events.SYNC_DATA2};
        String selection = "(" + Events.CALENDAR_ID + " = ? AND (("
                + Events.DTSTART + " >= ? AND "
                + Events.DTSTART + " <= ? ) OR ("
                + Events.DTEND + " >= ? AND "
                + Events.DTEND + " <= ? )) OR "
                + Events.SYNC_DATA2 + " = ? OR ("
                + Events.SYNC_DATA2 + " = ? AND "
                + Events.DTSTART + " >= ? AND "
                + Events.DTSTART + " <= ?))";
        String[] selectionArgs = {
                String.valueOf(calID),
                String.valueOf(dayStart.getTimeInMillis()),
                String.valueOf(dayEnd.getTimeInMillis()),
                String.valueOf(dayStart.getTimeInMillis()),
                String.valueOf(dayEnd.getTimeInMillis()),
                "daily",
                "weekly",
                String.valueOf(weekStart.getTimeInMillis()),
                String.valueOf(weekEnd.getTimeInMillis())
        };

        return getContentResolver().query(Events.CONTENT_URI,projection,selection,selectionArgs, Events.DTSTART + " ASC");
    }

    private boolean sameDay(Calendar d1, Calendar d2) {
        return d1.get(Calendar.DAY_OF_MONTH) == d2.get(Calendar.DAY_OF_MONTH) &&
                d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH) &&
                d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR);
    }

    private void rollToMonday(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        int delta = day_of_week - Calendar.MONDAY;
        if (delta < 0) delta += 7;
        c.add(Calendar.DAY_OF_WEEK, -delta);
    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        int rows = getContentResolver().delete(deleteUri, null, null);
        Log.d(LOG_TAG, "Rows deleted: " + rows);
    }

    private void markAsDone(long eventID) {
        Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        ContentValues values = new ContentValues();
        values.put(Events.STATUS, Events.STATUS_CONFIRMED);
        int rows = getContentResolver().update(updateUri, values, null, null);
        Log.i(LOG_TAG, "Rows updated: " + rows);
    }
}
