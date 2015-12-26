package oscmansan.calendar;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
        for (int i = 0; i < days.size(); ++i) {
            getEventsOnDay(i);
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
        }
        cursor.moveToPosition(-1);

        return view;
    }

    void getEventsOnDay(int position) {
        Calendar c = days.get(position);
        String[] projection = {CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.STATUS};
        String selection = "((" + CalendarContract.Events.CALENDAR_ID + " = ?) AND ("
                                + CalendarContract.Events.DTSTART + " >= ?))";
        String[] selectionArgs = {String.valueOf(calID),String.valueOf(c.getTimeInMillis())};

        Cursor cur = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,projection,selection,selectionArgs,null);
        cursors.add(position,cur);
    }
}
