package oscmansan.calendar;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class PendingTasksActivity extends AppCompatActivity {

    private static final String LOG_TAG = PendingTasksActivity.class.getSimpleName();
    
    private long calID;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_tasks);

        calID = getIntent().getLongExtra("calID",-1);
        Log.d(LOG_TAG, "calID: " + calID);

        Cursor cursor = getPendingTasks();
        PendingTasksAdapter adapter = new PendingTasksAdapter(this,cursor);
        ListView pending_tasks_list = (ListView)findViewById(R.id.pending_tasks_list);
        pending_tasks_list.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pending_tasks, menu);
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
    
    private Cursor getPendingTasks() {
        String[] projection = {Events._ID, Events.TITLE, Events.SYNC_DATA3};
        String selection = Events.CALENDAR_ID + " = ? AND " + Events.SYNC_DATA1 + " = ? AND " + Events.STATUS + " = ?";
        String[] selectionArgs = {String.valueOf(calID), "task", String.valueOf(Events.STATUS_TENTATIVE)};

        return getContentResolver().query(Events.CONTENT_URI,projection,selection,selectionArgs,null);
    }
}
