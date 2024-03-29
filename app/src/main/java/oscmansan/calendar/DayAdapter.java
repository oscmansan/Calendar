package oscmansan.calendar;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DayAdapter extends ArrayAdapter<Calendar> implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = DayAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Calendar> days;
    private long calID;
    private LinearLayout eventList;
    private ArrayList<Cursor> cursors;

    public DayAdapter(Context context, long calID, ArrayList<Calendar> days) {
        super(context, -1, days);

        this.context = context;
        this.days = days;
        this.calID = calID;

        cursors = new ArrayList<>();
        for (Calendar day : days) {
            Cursor cur = getEventsOnDay(day);
            cursors.add(cur);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        for (Cursor cur : cursors) {
            cur.close();
        }
        super.finalize();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.day, parent, false);

        int day_of_month = days.get(position).get(Calendar.DAY_OF_MONTH);
        String day_of_week = days.get(position).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        ((TextView)view.findViewById(R.id.list_day_of_month)).setText(String.valueOf(day_of_month));
        ((TextView)view.findViewById(R.id.list_day_of_week)).setText(day_of_week);

        eventList = (LinearLayout)view.findViewById(R.id.events);
        eventList.removeAllViews();
        Cursor cursor = cursors.get(position);
        while (cursor.moveToNext()) {
            String type = cursor.getString(5);
            if (type.equals("event")) {
                View event = LayoutInflater.from(context).inflate(R.layout.event, eventList, false);

                String title = cursor.getString(1);
                ((TextView) event.findViewById(R.id.event_title)).setText(title);

                Calendar beginTime = Calendar.getInstance();
                beginTime.setTimeInMillis(cursor.getLong(3));
                Calendar endTime = Calendar.getInstance();
                endTime.setTimeInMillis(cursor.getLong(4));
                String time =
                        String.format("%02d", beginTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                                String.format("%02d", beginTime.get(Calendar.MINUTE)) + " - " +
                                String.format("%02d", endTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                                String.format("%02d", endTime.get(Calendar.MINUTE));
                ((TextView) event.findViewById(R.id.event_time)).setText(time);

                eventList.addView(event);
            }
            else if (type.equals("task")) {
                View task = LayoutInflater.from(context).inflate(R.layout.task, eventList, false);

                String title = cursor.getString(1);
                ((TextView) task.findViewById(R.id.task_title)).setText(title);

                int status = cursor.getInt(6);
                if (status == Events.STATUS_CONFIRMED) {
                    task.findViewById(R.id.checkbox).setVisibility(View.VISIBLE);
                }

                eventList.addView(task);
            }
        }
        cursor.moveToPosition(-1);

        return view;
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

        String[] projection = {Events._ID, Events.TITLE, Events.DESCRIPTION, Events.DTSTART, Events.DTEND, Events.SYNC_DATA1, Events.STATUS};
        String selection = "("  + Events.CALENDAR_ID + " = ? AND ("
                + Events.DTSTART + " >= ? AND "
                + Events.DTSTART + " <= ? ) OR "
                + Events.SYNC_DATA2 + " = ? OR ("
                + Events.SYNC_DATA2 + " = ? AND "
                + Events.DTSTART + " >= ? AND "
                + Events.DTSTART + " <= ?))";
        String[] selectionArgs = {
                String.valueOf(calID),
                String.valueOf(dayStart.getTimeInMillis()),
                String.valueOf(dayEnd.getTimeInMillis()),
                "daily",
                "weekly",
                String.valueOf(weekStart.getTimeInMillis()),
                String.valueOf(weekEnd.getTimeInMillis())
        };

        return context.getContentResolver().query(Events.CONTENT_URI,projection,selection,selectionArgs, Events.DTSTART + " ASC");
    }

    private void rollToMonday(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        int delta = day_of_week - Calendar.MONDAY;
        if (delta < 0) delta += 7;
        c.add(Calendar.DAY_OF_WEEK, -delta);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(context,DayActivity.class);
        intent.putExtra("calID", calID);
        intent.putExtra("day", days.get(position).getTimeInMillis());
        context.startActivity(intent);
    }
}
