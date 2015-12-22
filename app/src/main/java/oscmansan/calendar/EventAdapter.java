package oscmansan.calendar;

import android.content.AsyncQueryHandler;
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

    private ContentResolver cr;
    private MyQueryHandler queryHandler;

    public EventAdapter(Context context) {
        super(context, null, 0);
        cr = context.getContentResolver();
        queryHandler = new MyQueryHandler(cr);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view.findViewById(R.id.event_id)).setText(String.valueOf(cursor.getLong(0)));
        ((TextView)view.findViewById(R.id.event_name)).setText(cursor.getString(1));
        int status = cursor.getInt(2);
        if (status == Events.STATUS_CONFIRMED) {
            view.setBackgroundColor(0xFFBDBDBD);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        long eventID = cursor.getLong(0);
        int status = cursor.getInt(2);
        ContentValues values = new ContentValues();

        // The new status for the event
        if (status == Events.STATUS_TENTATIVE) {
            values.put(Events.STATUS, Events.STATUS_CONFIRMED);
            view.setBackgroundColor(0xFFBDBDBD);
        } else {
            values.put(Events.STATUS, Events.STATUS_TENTATIVE);
            view.setBackgroundColor(0x00000000);
        }

        Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
        queryHandler.startUpdate(1, cursor,updateUri, values, null, null);
    }

    private class MyQueryHandler extends AsyncQueryHandler {

        public MyQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            Log.d(LOG_TAG, "Rows updated: " + result);
            Cursor cursor = (Cursor)cookie;
            cursor.requery();
        }
    }
}
