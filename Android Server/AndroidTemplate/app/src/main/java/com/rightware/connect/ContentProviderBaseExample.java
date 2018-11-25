package com.rightware.connect;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.rightware.connect.ContentClient;
import com.rightware.connect.ContentColumn;
import com.rightware.connect.ContentData;
import com.rightware.connect.ContentDataType;
import com.rightware.connect.ContentProvider;
import com.rightware.connect.ContentQuery;
import com.rightware.connect.ContentQueryArguments;
import com.rightware.connect.HTTPVirtualFileClient;
import com.rightware.connect.StringList;
import com.rightware.connect.VoidToken;
import com.rightware.connect.WorkQueueInterface;

/**
 * Created by jkuronen on 15.9.2017.
 */

public class ContentProviderBaseExample extends ContentProvider {

    private static final String TAG = "ContentProviderBase";

    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";

    protected String m_URI;
    protected Context m_context;
    protected ContentClient m_contentClient;
    protected HTTPVirtualFileClient m_fileServerClient;
    protected VoidToken m_contentToken;

    /**
     *
     */
    protected class LimitResult {
        private int m_index;
        private int m_count;

        public LimitResult(int index, int count) {
            m_index = index;
            m_count = count;
        }

        /**
         * Convers object content to string for debugging purposes.
         * @return object contents as string
         */
        public String toString() {
            String result;

            if (m_count > 0) {
                result = "START FROM " + m_index + " HAVING " + m_count + " ITEMS.";
            } else {
                result = "START FROM " + m_index + ".";
            }

            return result;
        }

        /**
         *
         * @return the index where to start fetching of results.
         */
        public int getIndex() {
            return m_index;
        }

        /**
         * Return the count where to limit the results.
         * @return count
         */
        public int getCount() {
            return m_count;
        }
    }

    public ContentProviderBaseExample(String URI, Context context, WorkQueueInterface workQueue, ContentClient contentClient, HTTPVirtualFileClient fileServerClient) {
        super(workQueue);

        m_URI = URI;
        m_context = context;
        m_contentClient = contentClient;
        m_fileServerClient = fileServerClient;

        m_contentToken = m_contentClient.acquireContentToken();
        m_contentClient.addContentProvider(m_contentToken, getURI(), this);
    }

    protected void finalize() {
        m_contentClient.releaseContentToken(m_contentToken);

        super.finalize();
    }

    protected String getURIPostfix() {
        return m_URI;
    }

    public void executeQuery(String uri, StringList columns, String filters, LimitResult lr, String sort, ContentQuery contentQuery) {
        Log.e(TAG, "executeQuery not overridden");
    }

    public void query(ContentQueryArguments args, ContentQuery contentQuery) {
        if (args.getUri().equals(m_URI) == false) {
            contentQuery.notifyCompleted();
            return;
        }
        Log.d(TAG, "query: "+ args.getUri());
        LimitResult lr = new LimitResult(args.getLimitIndex(), args.getLimitCount());
        executeQuery(args.getUri(), args.getColumns(), args.getFilter(), lr, args.getSort(), contentQuery);
    }

    private String getURI() {
        return "content://com.rightware.content/" + m_URI;
    }

    protected boolean hasColumn(String[] columns, String column) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(column)) {
                return true;
            }
        }

        Log.w(TAG, "Column: '" + column + "' not found from '" + TextUtils.join("|", columns) + "'");

        return false;
    }

    protected String fixFilter(String filter) {
        if (filter == null) {
            return "";
        }

        // Kanzi uses "id" where android has "_id"
        return filter.replace("id=", "_id=");
    }

    protected boolean prepareColumns(StringList columns, ContentData contentData, String[] validColumns, ContentDataType[] validColumnTypes) {
        if (columns.size() < 1) {
            for (int i = 0; i < validColumns.length; ++i) {
                columns.add(validColumns[i]);
            }
        } else { // ensure all provided columns are valid.
            for (int i = 0; i < columns.size(); ++i) {
                String column = columns.get(i);

                if (hasColumn(validColumns, column) == false) {
                    Log.e(TAG, "Column '" + column + " not supported. returning empty set.");
                    return false;
                }
            }
        }

        for (int i = 0; i < columns.size(); ++i) {
            String column = columns.get(i);

            for (int j = 0; j < validColumns.length; ++j) {
                if (column.equals(validColumns[j])) {
                    ContentColumn col = contentData.addColumn();
                    col.setName(column);
                    col.setDatatype(validColumnTypes[j]);
                }
            }
        }

        return true;
    }

    protected void setCursorToPosition(Cursor c, LimitResult lr) {
        if (lr.getIndex() < 1) {
            c.moveToPosition(0);
            return;
        }

        c.moveToPosition(lr.getIndex());
    }
}