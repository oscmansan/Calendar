package oscmansan.calendar;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DayAdapter extends ArrayAdapter<Calendar> {

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
            getEventsOnDay(day);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.day, parent, false);

        int day_of_month = days.get(position).get(Calendar.DAY_OF_MONTH);
        String day_of_week = days.get(position).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        ((TextView)view.findViewById(R.id.day_of_month)).setText(String.valueOf(day_of_month));
        ((TextView)view.findViewById(R.id.day_of_week)).setText(day_of_week);

        eventList = (LinearLayout)view.findViewById(R.id.events);
        eventList.removeAllViews();
        Cursor cursor = cursors.get(position);
        while (cursor.moveToNext()) {
            View event = LayoutInflater.from(context).inflate(R.layout.event, parent, false);
            String s = String.valueOf(cursor.getLong(0)) + " " + cursor.getString(1);
            ((TextView) event.findViewById(R.id.event)).setText(s);
            eventList.addView(event);
            View space = LayoutInflater.from(context).inflate(R.layout.space, parent, false);
            eventList.addView(space);
        }
        cursor.moveToPosition(-1);

        return view;
    }

    void getEventsOnDay(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        Calendar dayStart = Calendar.getInstance();
        dayStart.set(year, month, day, 0, 0, 0);

        Calendar dayEnd = Calendar.getInstance();
        dayEnd.set(year, month, day, 23, 59, 59);

        String[] projection = {Events._ID, Events.TITLE, Events.STATUS, Events.DTSTART};
        String selection = Events.CALENDAR_ID + " = ? AND "
                                + Events.DTSTART + " >= ? AND "
                                + Events.DTSTART + " <= ?";
        String[] selectionArgs = {
                String.valueOf(calID),
                String.valueOf(Long.toString(dayStart.getTimeInMillis())),
                String.valueOf(Long.toString(dayEnd.getTimeInMillis()))
        };

        Cursor cur = context.getContentResolver().query(Events.CONTENT_URI,projection,selection,selectionArgs,null);
        cursors.add(cur);
    }
}
