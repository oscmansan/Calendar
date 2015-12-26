package oscmansan.calendar;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private static final String LOG_TAG = AddEventActivity.class.getSimpleName();

    private Button add_event_button;
    private MyQueryHandler queryHandler;
    private long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        calID = getIntent().getLongExtra("calID",-1);
        Log.d(LOG_TAG, "calID: " + calID);
        queryHandler = new MyQueryHandler(getContentResolver());

        add_event_button = (Button)findViewById(R.id.add_event_button);
        add_event_button.setOnClickListener(new View.OnClickListener() {
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
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private class MyQueryHandler extends AsyncQueryHandler {

        public MyQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
            Log.d(LOG_TAG, "Event inserted: " + eventID);
            finish();
        }
    }
}
