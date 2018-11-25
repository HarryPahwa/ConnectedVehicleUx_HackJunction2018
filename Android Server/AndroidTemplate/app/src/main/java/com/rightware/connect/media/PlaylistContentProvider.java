package com.rightware.connect.media;
import com.rightware.connect.*;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import android.provider.MediaStore;
import android.database.Cursor;

import com.rightware.connect.ContentClient;
import com.rightware.connect.ContentColumn;
import com.rightware.connect.ContentColumnContainer;
import com.rightware.connect.ContentData;
import com.rightware.connect.ContentDataType;
import com.rightware.connect.ContentProviderDescription;
import com.rightware.connect.ContentQuery;
import com.rightware.connect.ContentRow;
import com.rightware.connect.HTTPVirtualFileClient;
import com.rightware.connect.HTTPVirtualFileLocalClient;
import com.rightware.connect.LocalContentClient;
import com.rightware.connect.StringList;
import com.rightware.connect.VoidToken;
import com.rightware.connect.WorkQueueInterface;

public class PlaylistContentProvider extends ContentProviderBaseExample {
    private static final String TAG = "PlaylistProvider";

    private final boolean m_debug = true;

    /*
      EXTERNAL SCHEMA FOR PLAYLISTS
    */
    private static final String COLUMN_ID           = "id";     // integer
    private static final String COLUMN_NAME         = "name";   // string
    private static final String COLUMN_TRACKS       = "tracks"; // string
    private static final String COLUMN_SOURCE       = "source"; // string
    private static final String COLUMN_ROWID        = "rowid";

    public PlaylistContentProvider(Context context, WorkQueueInterface workQueue, ContentClient client, HTTPVirtualFileClient fileServerClient) {
        super("playlists", context, workQueue, client, fileServerClient);

        class ChangeNotifier extends ContentObserver {
            private Context m_context;
            public ChangeNotifier(Handler h, Context context) {
                super(h);
                m_context = context;
            }
            @Override
            public void onChange(boolean selfchange) {
                // -> Invoke KanziConnects ContentProvider change notifier.
                notifyChangedEx(getURIPostfix());
            }
        }

        m_context.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, new ChangeNotifier(new Handler(), m_context));
    }
    
    protected void finalize() {
        super.finalize();
    }

    protected String getAllTracks(String sort, LimitResult lr) {
        StringBuilder allTracks = new StringBuilder(2048);

        Cursor cur = m_context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, sort);
        
        if (cur != null && cur.getCount() > 0) {
            if (m_debug) {
                Log.d(TAG, "Cursor found " + cur.getCount() + " tracks with limits = " + lr.toString());
            }

            while (cur.moveToNext()) {
                int id = (int)cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                if (allTracks.length() > 0) {
                    allTracks.append(';');
                }
                allTracks.append(Integer.toString(id));
            }
        }
        if (cur != null) {
            cur.close();
        }
        return allTracks.toString();
    }

    public int getSubProviderCount() {
        return 1;
    }

    public ContentProviderDescription getSubProviderDescription(int index) {
        ContentProviderDescription desc = new ContentProviderDescription();
        ContentColumnContainer columns = new ContentColumnContainer();

        columns.add(new ContentColumn(ContentDataType.Integer, COLUMN_ID));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_NAME));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_TRACKS));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_SOURCE));
        columns.add(new ContentColumn(ContentDataType.Integer, COLUMN_ROWID));

        desc.setColumns(columns);
        return desc;
    }

    public void executeQuery(String uri, StringList columns, String filters, LimitResult limits, String sort, ContentQuery contentQuery) {
        
        ContentData contentData = new ContentData();

        //
        // We will effectively ignore here the filters etc. -> just always return same artificial 
        // playlist.
        //

        if (m_debug) {
            Log.d(TAG, "query for uri: " + uri);
        }

        String[] validColumns = new String[] {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_TRACKS,
                COLUMN_SOURCE,
                COLUMN_ROWID};
        ContentDataType[] validColumnTypes = new ContentDataType[] {
                ContentDataType.Integer,
                ContentDataType.String,
                ContentDataType.String,
                ContentDataType.String,
                ContentDataType.Integer};

        if (prepareColumns(columns, contentData, validColumns, validColumnTypes)) {
            if (limits.getIndex() == 0) {
                try {
                    ContentRow row = contentData.addRow();
                    row.resize(columns.size());

                    int index = 0;
                    for (int i = 0; i < columns.size(); ++i) {
                        String column = columns.get(i);

                        if (column.equals(COLUMN_ID)) {
                            row.setValue(index++, 1);
                        } else if (column.equals(COLUMN_NAME)) {
                            row.setValue(index++, "All tracks");
                        } else if (column.equals(COLUMN_TRACKS)) {
                            row.setValue(index++, getAllTracks(sort, limits));
                        } else if (column.equals(COLUMN_SOURCE)) {
                            row.setValue(index++, "");
                        } else if (column.equals(COLUMN_ROWID)) {
                            row.setValue(index++, 1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // This will indicate the framework that results are available.
        contentQuery.setData(contentData);
        contentQuery.notifyCompleted();
    }
};
