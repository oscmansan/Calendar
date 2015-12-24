package oscmansan.calendar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ListView week;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        week = (ListView)findViewById(R.id.week);
        title = (TextView)findViewById(R.id.title);

        Calendar c = Calendar.getInstance();
        showWeek(c);
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.pick_date:
                pickDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void pickDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this,dateSetListener,year,month,day);
        datePicker.show();
    }

    void showWeek(Calendar c) {
        int day_of_week = c.get(Calendar.DAY_OF_WEEK);
        c.roll(Calendar.DAY_OF_WEEK, -(day_of_week - Calendar.MONDAY));
        ArrayList<Calendar> days = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            days.add((Calendar)c.clone());
            c.roll(Calendar.DAY_OF_MONTH, 1);
        }
        week.setAdapter(new DayAdapter(MainActivity.this, days));

        String s = "Week " + c.get(Calendar.WEEK_OF_YEAR) + " of " + c.get(Calendar.YEAR);
        title.setText(s);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Log.d(LOG_TAG, "year: " + year + "  month: " + monthOfYear + "  day: " + dayOfMonth);
            Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            showWeek(c);
        }
    };

}
