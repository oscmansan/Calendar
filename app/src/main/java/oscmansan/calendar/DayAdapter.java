package oscmansan.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DayAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private Integer[] days_of_month;

    public DayAdapter(Context context, Integer[] days) {
        super(context, -1, days);

        this.context = context;
        this.days_of_month = days;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.day, parent, false);

        ((TextView)view.findViewById(R.id.day_of_month)).setText(String.valueOf(days_of_month[position]));

        return view;
    }
}
