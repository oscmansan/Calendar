package oscmansan.calendar;

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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {
    
    private static final String LOG_TAG = AddTaskActivity.class.getSimpleName();
    
    private EditText edit_task_title;
    private EditText edit_task_description;
    private Button save_button;
    private long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        calID = getIntent().getLongExtra("calID",-1);
        Log.d(LOG_TAG, "calID: " + calID);
        
        edit_task_title = (EditText)findViewById(R.id.edit_task_title);
        edit_task_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_title && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        
        edit_task_description = (EditText)findViewById(R.id.edit_task_description);
        edit_task_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.getId() == R.id.edit_event_title && !hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        
        save_button = (Button)findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertTask();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
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

    private void insertTask() {
        Boolean daily = ((Switch)findViewById(R.id.daily_sw)).isChecked();

        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, 0);
        values.put(Events.DTEND, 0);
        values.put(Events.TITLE, edit_task_title.getText().toString());
        values.put(Events.DESCRIPTION, edit_task_description.getText().toString());
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "Europe/Madrid");
        values.put(Events.STATUS, Events.STATUS_TENTATIVE);
        values.put(Events.SYNC_DATA1, "task");
        //if (daily)
            values.put(Events.SYNC_DATA2, "daily");

        Uri.Builder builder = Events.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(Events.ACCOUNT_NAME,"some.account@googlemail.com");
        builder.appendQueryParameter(Events.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true");

        Uri uri = getContentResolver().insert(builder.build(), values);
        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());
        Toast.makeText(this, "Task added: " + eventID, Toast.LENGTH_SHORT).show();
        finish();
    }
}
