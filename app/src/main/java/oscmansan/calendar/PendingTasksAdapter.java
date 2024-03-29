package oscmansan.calendar;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class PendingTasksAdapter extends CursorAdapter {

    public PendingTasksAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.pending_task, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view.findViewById(R.id.pending_task_title)).setText(cursor.getString(1));
        TextView linked_event = (TextView)view.findViewById(R.id.linked_event);
        if (cursor.getString(2) != null)
            linked_event.setText("Linked to " + cursor.getString(2));
        else
            linked_event.setText("No event linked");
    }
}
