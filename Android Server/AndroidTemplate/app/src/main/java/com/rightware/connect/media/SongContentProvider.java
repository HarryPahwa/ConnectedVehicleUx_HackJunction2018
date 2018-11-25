package com.rightware.connect.media;

import com.rightware.connect.*;
import android.content.Context;
import android.util.Log;
import android.os.Handler;
import java.io.FileInputStream;
import java.io.IOException;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.database.Cursor;
import java.text.ParseException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.text.NumberFormat;

interface AlbumArtImageProviderInterface {
    public class ImageInfo {
        public String path = "";
        public String extension = "";

        public boolean valid() {
            return path != null && path.length() > 0 && extension != null && extension.length() > 0;
        }
    }

    ImageInfo getAlbumImage(int albumid);
}

interface MusicTrackProviderInterface {
    String getMusicTrack(int trackid);
}

/**
 * Basic music track content provider for Android.
 * NOTE: This class expects that the music player is run on same host and thus the audio files
 * are provided as local filesystem paths (E.g. /sdcard/Music/mytrack.mp3).
 */
public class SongContentProvider extends ContentProviderBaseExample implements AlbumArtImageProviderInterface, MusicTrackProviderInterface {

    private static final String TAG = "SongProvider";
    private final boolean m_debug = true;

    /*
    EXTERNAL SCHEMA FOR SONGS
    */
    private static final String COLUMN_ID           = "id";         // integer
    private static final String COLUMN_NAME         = "name";       // string
    private static final String COLUMN_ARTIST       = "artist";     // string
    private static final String COLUMN_ALBUMID      = "album_id";   // integer
    private static final String COLUMN_URI          = "uri";        // string
    private static final String COLUMN_ALBUMIMAGE   = "albumimage"; // string
    private static final String COLUMN_SOURCE       = "source";     // string
    private static final String COLUMN_ROWID        = "rowid";      // integer

    private VoidToken m_artRegistrationToken;
    private VoidToken m_musicRegistrationToken;

    private AlbumArtFileProvider m_artProvider;
    private MusicTrackFileProvider m_musicProvider;

    private class GenericFileProvider extends HTTPVirtualFileProvider {
        private String m_staticPrefix;
        private String m_fileExtension;
        private String m_prefix;
        private boolean m_provideAsContentMode = false;

        public GenericFileProvider(String prefix, String fileExtension) {
            m_staticPrefix = prefix;
            m_fileExtension = fileExtension;
        }

        public String getPrefix() {
            return m_staticPrefix;
        }

        public void setUriPrefix(String path) {
            m_prefix = path;
        }

        public String uriForObject(int index) {
            return uriForObject(index, "");
        }

        public void setProvideAsContentMode(boolean enable) {
            m_provideAsContentMode = enable;
        }

        public String uriForObject(int index, String extension) {
            String id = Integer.toString(index);

            if (extension.length() > 0) {
                id += extension;
            } else {
                id += m_fileExtension;
            }

            String result = m_prefix + id;

            return result;
        }

        public void provideFile(String path, HTTPVirtualFileResponse response) {
            String strid = path.substring(getPrefix().length());

            int id = -1;

            try {
                id = NumberFormat.getInstance().parse(strid).intValue();
            } catch (ParseException pe) {
                pe.printStackTrace();
                response.provideAsFile("");
                return;
            }

            String pathToObject = getPathToObject(id);

            Log.d(TAG, "path: '" + path + "' => '" + pathToObject + "'");

            if (m_provideAsContentMode == true) {
                try {
                    FileInputStream fis = new FileInputStream(pathToObject);
                    int size = fis.available();
                    byte[] data = new byte[size];
                    fis.read(data);
                    response.provideAsContent("", data, data.length);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                response.provideAsFile(pathToObject);
            }
        }

        public String getPathToObject(int id) {
            return "";
        }

        public String getDefaultFile() {
            return "";
        }
    }

    private class MusicTrackFileProvider extends GenericFileProvider {
        MusicTrackProviderInterface m_providerIf;

        public MusicTrackFileProvider(MusicTrackProviderInterface providerIf) {
            super("/androidmusic/", ".mp3");
            m_providerIf = providerIf;
            setProvideAsContentMode(false);
        }

        public String getPathToObject(int id) {
            return m_providerIf.getMusicTrack(id);
        }
    }

    private class AlbumArtFileProvider extends GenericFileProvider {
        AlbumArtImageProviderInterface m_providerIf;
        
        public AlbumArtFileProvider(AlbumArtImageProviderInterface providerIf) {
            super("/androidalbumart/", ".jpg");
            m_providerIf = providerIf;
            setProvideAsContentMode(true);
        }

        public String getPathToObject(int id) {
            return m_providerIf.getAlbumImage(id).path;
        }

        public String getDefaultFile() {
            String URI = new String("http://<SERVER>:8080/albumart/empty_music.png");
            return URI;
        }
    }

    /**
     * Encapsulate all the information related to a Song
     */
    private class SongItem {
        public int id;
        public String name;
        public String artist;
        public int albumid;
        public String fileuri;
        public String localfileuri;
        public String albumimage;
        public String source;
    }

    private class HTTPPathRegistrationCompletor extends HTTPVirtualFileClient.RegistrationStatusChangeCallback {
        private HTTPVirtualFileClient m_fileServerClient;
        private AlbumArtFileProvider m_fileProviderArt = null;
        //private MusicTrackFileProvider m_fileProviderMusic = null;
        private VoidToken m_fileServerRegistrationToken = null;

        public HTTPPathRegistrationCompletor(HTTPVirtualFileClient fileServerClient, MusicTrackFileProvider fileProvider, VoidToken registrationToken) {
            m_fileServerClient = fileServerClient;
          //  m_fileProviderMusic = fileProvider;
            m_fileServerRegistrationToken = registrationToken;
        }
        public HTTPPathRegistrationCompletor(HTTPVirtualFileClient fileServerClient, AlbumArtFileProvider fileProvider, VoidToken registrationToken) {
            m_fileServerClient = fileServerClient;
            m_fileProviderArt = fileProvider;
            m_fileServerRegistrationToken = registrationToken;
        }

        public void registrationStateChanged() {
            String prefix = m_fileServerClient.getPath(m_fileServerRegistrationToken);
            Log.d(TAG, "Received fileserver prefix: '" + prefix + "'");
            if (prefix.length() > 0) {
                if (m_fileProviderArt != null) {
                    m_fileProviderArt.setUriPrefix(prefix);
                }
            }
        }
    }
    HTTPPathRegistrationCompletor m_HTTPRegistrationCompletorArt;
    HTTPPathRegistrationCompletor m_HTTPRegistrationCompletorMusic;

    public SongContentProvider(Context context, WorkQueueInterface workQueue, ContentClient client, HTTPVirtualFileClient fileServerClient) {
        super("songs", context, workQueue, client, fileServerClient);

        // Register a virtual directory to HTTP file server
        m_artProvider = new AlbumArtFileProvider(this);
        m_artRegistrationToken = m_fileServerClient.registerPath("http", m_artProvider.getPrefix(), m_artProvider);
        m_HTTPRegistrationCompletorArt =
                new HTTPPathRegistrationCompletor(m_fileServerClient, m_artProvider, m_artRegistrationToken);
        m_fileServerClient.registerRegistrationStatusChangeCallback(m_artRegistrationToken, m_HTTPRegistrationCompletorArt);

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
        if (m_artRegistrationToken != null) {
            m_fileServerClient.unregisterPath(m_artRegistrationToken);
            m_artRegistrationToken = null;
        }

        super.finalize();
    }
    
    public String getMusicTrack(int trackid) {
        if (m_debug) {
            Log.d(TAG, "getMusicTrack: " + trackid);
        }
        String criteria = MediaStore.Audio.Media._ID + " = " + Integer.toString(trackid);
        Cursor cursor = m_context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, criteria, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            SongItem item = read(cursor);
            if (item != null) {
                cursor.close();
                return item.localfileuri;
            }
        }

        if (m_debug) {
            Log.d(TAG, "music not found with id: " + trackid);
        }

        return "";
    }
    
    public AlbumArtImageProviderInterface.ImageInfo getAlbumImage(int albumid) {
        try {
            final Uri sArtworkUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            Uri queryUri = ContentUris.withAppendedId(sArtworkUri, albumid);
            
            Cursor cursor = m_context.getContentResolver().query(queryUri, null, null, null, null);
            if (cursor == null || cursor.moveToFirst() == false) {
                if (cursor != null) {
                    cursor.close();
                }
                return new AlbumArtImageProviderInterface.ImageInfo();
            }

            int artIdx = cursor.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            if (artIdx < 0) {
                cursor.close();
                return new AlbumArtImageProviderInterface.ImageInfo();
            }
            String uri = cursor.getString(artIdx);

            if (uri != null && uri.length() > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(uri, options);
                if (options.outWidth != -1 && options.outHeight != -1) {
                    AlbumArtImageProviderInterface.ImageInfo ii = new AlbumArtImageProviderInterface.ImageInfo();
                    ii.path = uri;
                    if (options.outMimeType != null && options.outMimeType.equals("image/png")) {
                        ii.extension = ".png";
                    } else if (options.outMimeType != null && options.outMimeType.equals("image/jpeg")) {
                        ii.extension = ".jpg";
                    }
                    cursor.close();
                    return ii;
                } else {
                    cursor.close();
                    return new AlbumArtImageProviderInterface.ImageInfo();
                }
            } else {
                cursor.close();
                return new AlbumArtImageProviderInterface.ImageInfo();
            }
        } catch (Error ee) {
            Log.w(TAG, ee.toString());
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }

        return new AlbumArtImageProviderInterface.ImageInfo();
    }

    private SongItem read(Cursor cursor) {
        SongItem item = new SongItem();

        try {
            item.id = (int) cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            item.name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            item.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            // NOTE: provide direct filesystem path to media file
            item.fileuri = "file:///" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            item.localfileuri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            item.albumid = (int) cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            AlbumArtImageProviderInterface.ImageInfo ii = getAlbumImage(item.albumid);
            if (ii.valid()) {
                item.albumimage = m_artProvider.uriForObject(item.albumid, ii.extension);
            } else {
                item.albumimage = m_artProvider.getDefaultFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public int getSubProviderCount() {
        return 1;
    }

    public ContentProviderDescription getSubProviderDescription(int index) {
        ContentProviderDescription desc = new ContentProviderDescription();
        ContentColumnContainer columns = new ContentColumnContainer();

        columns.add(new ContentColumn(ContentDataType.Integer, COLUMN_ID));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_NAME));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_ARTIST));
        columns.add(new ContentColumn(ContentDataType.Integer, COLUMN_ALBUMID));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_URI));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_ALBUMIMAGE));
        columns.add(new ContentColumn(ContentDataType.String, COLUMN_SOURCE));
        columns.add(new ContentColumn(ContentDataType.Integer, COLUMN_ROWID));

        desc.setColumns(columns);
        return desc;
    }

    public void executeQuery(String uri, StringList columns, String filters, LimitResult limits, String sort, ContentQuery contentQuery) {
        ContentData contentData = new ContentData();

        String[] validColumns = new String[] {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_ARTIST,
                COLUMN_ALBUMID,
                COLUMN_URI,
                COLUMN_ALBUMIMAGE,
                COLUMN_SOURCE,
                COLUMN_ROWID};
        ContentDataType[] validColumnTypes = new ContentDataType[] {
                ContentDataType.Integer,
                ContentDataType.String,
                ContentDataType.String,
                ContentDataType.Integer,
                ContentDataType.String,
                ContentDataType.String,
                ContentDataType.String,
                ContentDataType.Integer};

        if (prepareColumns(columns, contentData, validColumns, validColumnTypes)) {
            if (filters.isEmpty()) {
                filters = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            }

            if (m_debug) {
                Log.d(TAG, "filters = " + fixFilter(filters) + " sort = " + sort + " limits=" + limits.toString());
            }

            Cursor cur = m_context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, fixFilter(filters), null, sort);
            if (cur != null && cur.getCount() > 0) {
                if (m_debug) {
                    Log.d(TAG, "Cursor found " + cur.getCount() + " elements with limits: " + limits.toString());
                }

                setCursorToPosition(cur, limits);

                int rowid = limits.getIndex() + 1;
                while (cur.isAfterLast() == false && ((contentData.getRowCount() < limits.getCount()) || limits.getCount() == 0)) {
                    SongItem info = read(cur);
                    cur.moveToNext();

                    if (info != null) {
                        ContentRow row = contentData.addRow();
                        row.resize(columns.size());

                        int index = 0;
                        for (int i=0; i<columns.size(); ++i) {
                            String column = columns.get(i);
                            if (column.equals(COLUMN_ID)) {
                                row.setValue(index++, info.id);
                            } else if (column.equals(COLUMN_NAME)) {
                                row.setValue(index++, info.name);
                            } else if (column.equals(COLUMN_ARTIST)) {
                                row.setValue(index++, info.artist);
                            } else if (column.equals(COLUMN_ALBUMID)) {
                                row.setValue(index++, info.albumid);
                            } else if (column.equals(COLUMN_URI)) {
                                row.setValue(index++, info.fileuri);
                            } else if (column.equals(COLUMN_ALBUMIMAGE)) {
                                row.setValue(index++, info.albumimage);
                            } else if (column.equals(COLUMN_SOURCE)) {
                                row.setValue(index++, info.source == null ? "" : info.source);
                            } else if (column.equals(COLUMN_ROWID)) {
                                row.setValue(index++, rowid);
                            } else {
                                Log.w(TAG, "Don't know how to handle column: " + column);
                            }
                        }
                        rowid++;
                    }
                }
            }
            cur.close();
        }

        // This will indicate the framework that results are available.
        contentData.setStartIndex(limits.getIndex());
        contentQuery.setData(contentData);
        contentQuery.notifyCompleted();
    }
};
