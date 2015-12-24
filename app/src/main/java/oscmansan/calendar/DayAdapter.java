package oscmansan.calendar;

import android.content.Context;
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

    public DayAdapter(Context context, ArrayList<Calendar> days) {
        super(context, -1, days);

        this.context = context;
        this.days = days;
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

        LinearLayout eventList = (LinearLayout)view.findViewById(R.id.events);
        eventList.removeAllViews();
        String[] events = {"Event 1", "Event 2", "Event 3"};
        for (String s : events) {
            View event = LayoutInflater.from(context).inflate(R.layout.event, parent, false);
            ((TextView)event.findViewById(R.id.event)).setText(s);
            eventList.addView(event);
        }

        return view;
    }
}
