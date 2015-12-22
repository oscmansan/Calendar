package oscmansan.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EventAdapter extends CursorAdapter implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = EventAdapter.class.getSimpleName();
    private Context context;

    public EventAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view.findViewById(R.id.event_id)).setText(String.valueOf(cursor.getLong(0)));
        ((TextView)view.findViewById(R.id.event_name)).setText(cursor.getString(1));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        long eventID = cursor.getLong(0);
        int status = cursor.getInt(2);
        Log.d(LOG_TAG, "status: " + status);

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        // The new status for the event
        if (status == Events.STATUS_TENTATIVE) {
            values.put(Events.STATUS, Events.STATUS_CONFIRMED);
            view.setBackgroundColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        }
        else {
            values.put(Events.STATUS, Events.STATUS_TENTATIVE);
            view.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
        }

        Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        int rows = cr.update(updateUri, values, null, null);
        Log.d(LOG_TAG, "Rows updated: " + rows);
    }
}
