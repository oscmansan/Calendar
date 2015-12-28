package oscmansan.calendar;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

        LinearLayout eventList = (LinearLayout)findViewById(R.id.events_detail);
        eventList.removeAllViews();
        Cursor cursor = getEventsOnDay(c);
        while (cursor.moveToNext()) {
            View event = LayoutInflater.from(this).inflate(R.layout.event, eventList, false);

            String title = cursor.getString(1);
            ((TextView) event.findViewById(R.id.event_title)).setText(title);

            Calendar beginTime = Calendar.getInstance();
            beginTime.setTimeInMillis(cursor.getLong(3));
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(cursor.getLong(4));
            String time =
                    String.format("%02d",beginTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                            String.format("%02d",beginTime.get(Calendar.MINUTE)) + " - " +
                            String.format("%02d",endTime.get(Calendar.HOUR_OF_DAY)) + ":" +
                            String.format("%02d", endTime.get(Calendar.MINUTE));
            ((TextView) event.findViewById(R.id.event_time)).setText(time);

            eventList.addView(event);
        }
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
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Cursor getEventsOnDay(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        Calendar dayStart = Calendar.getInstance();
        dayStart.set(year, month, day, 0, 0, 0);

        Calendar dayEnd = Calendar.getInstance();
        dayEnd.set(year, month, day, 23, 59, 59);

        String[] projection = {CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.STATUS};
        String selection = CalendarContract.Events.CALENDAR_ID + " = ? AND "
                + CalendarContract.Events.DTSTART + " >= ? AND "
                + CalendarContract.Events.DTSTART + " <= ?";
        String[] selectionArgs = {
                String.valueOf(calID),
                String.valueOf(Long.toString(dayStart.getTimeInMillis())),
                String.valueOf(Long.toString(dayEnd.getTimeInMillis()))
        };

        return getContentResolver().query(CalendarContract.Events.CONTENT_URI,projection,selection,selectionArgs, CalendarContract.Events.DTSTART + " ASC");
    }
}
