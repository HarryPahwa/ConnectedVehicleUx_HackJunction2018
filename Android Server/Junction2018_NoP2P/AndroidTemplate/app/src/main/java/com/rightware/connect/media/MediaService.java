package com.rightware.connect.media;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rightware.connect.*;


class TrackInfo {
    public int m_id;
    public String m_name;
    public String m_artist;
    public int m_albumid;
    public String m_uri;
    public String m_image;
    public String m_source;
};

class PlaylistInfo {
    public int m_id;
    public String m_name;
    public int [] m_tracks;
    public String m_tracksOriginal;
    public String m_source;

    public PlaylistInfo() {
        m_id = 0;
        m_source = "?";
        m_name = "?";
    }

    public boolean hasTrack(int trackid) {
        if (m_tracks != null)
        {
            for (int i=0; i<m_tracks.length; ++i)
            {
                if (m_tracks[i] == trackid)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public int getNextTrack(int trackId) {
        if (m_tracks == null) {
            return -1;
        }

        for (int i=0; i<(m_tracks.length-1); ++i) {
            if (m_tracks[i] == trackId) {
                return m_tracks[i+1];
            }
        }

        return -1;
    }

    public int getPreviousTrack(int trackId) {
        if (m_tracks == null) {
            return -1;
        }

        for (int i=1; i<m_tracks.length; ++i) {
            if (m_tracks[i] == trackId) {
                return m_tracks[i-1];
            }
        }

        return -1;
    }

    public int getFirstTrack() {
        if (m_tracks == null) {
            return -1;
        }
        if (m_tracks.length < 1) {
            return -1;
        }
        return m_tracks[0];
    }

    public String tracksToString() {
        String s = new String();

        if (m_tracks != null) {
            for (int i=0; i<m_tracks.length; ++i) {
                if (s.length() > 0) {
                    s += ";";
                }
                s += Integer.toString(m_tracks[i]);
            }
        }

        return s;
    }
};

interface ITrackQueryResult {
    void trackQueryComplete(int cookie, TrackInfo trackInfo);
};

interface IPlaylistQueryResult {
    void playlistQueryComplete(PlaylistInfo playlistInfo);
};

/**
 * Example Media service implementation with very limited functionality.
 */
public class MediaService extends MediaServiceConcept implements ITrackQueryResult, IPlaylistQueryResult, MusicPlayer.Listener {
    private static final String TAG = "MediaService";
    private ClientConnector m_connector;
    private Context m_context;
    private String m_serverIP;
    private Handler m_handler = new Handler();
    private Runnable m_playbackStateReporter = new Runnable() {
        @Override
        public void run() {
            reportPlaybackState();
            m_handler.postDelayed(m_playbackStateReporter, 500);
        }
    };
    private MusicPlayer m_musicPlayer = null;
    private MusicPlayer getMusicPlayer() {
        if (m_musicPlayer == null) {
            m_musicPlayer = new MusicPlayer(m_context, this);
        }
        return m_musicPlayer;
    }

    private static final int Request_None           = 0;
    private static final int Request_PlayPlaylist   = 1;
    private static final int Request_PlayTrack      = 2;
    private int m_state = Request_None;

    private static final int TrackQueryCookie_Current   = 0;
    private static final int TrackQueryCookie_Previous  = 1;
    private static final int TrackQueryCookie_Next      = 2;

    private static final int Playback_Stopped       = 0;
    private static final int Playback_Playing       = 1;
    private static final int Playback_Paused        = 2;

    private TrackInfo m_currentTrack;
    private TrackInfo m_previousTrack;
    private TrackInfo m_nextTrack;
    private PlaylistInfo m_currentPlaylist;
    private UriTranslator m_uriTranslator;

    private PlaylistQuery m_playlistQuery = null;
    private TrackQuery m_trackQuery = null;
    private TrackQuery m_nextTrackQuery = null;
    private TrackQuery m_previousTrackQuery = null;

    private class PlaybackState {
        public int m_state;         // Oneof above Playback_*
        public int m_position;
        public int m_duration;
        public boolean m_stoppedReported;
        public boolean m_pausedReported;

        PlaybackState() {
            reset();
        }

        public void resetTo(int state) {
            m_state = state;
            m_stoppedReported = false;
            m_pausedReported = false;
            m_duration = 0;
            m_position = 0;
        }

        public void reset() {
            resetTo(Playback_Stopped);
        }
    };
    private PlaybackState m_playbackState = new PlaybackState();

    private class TrackQuery extends ContentClient.ContentClientObserver {
        private final String TAG = "TrackQuery";

        private boolean m_busy = false;
        private ITrackQueryResult m_callback;
        private VoidToken m_token;
        private ContentClient m_client;
        public TrackInfo m_trackInfo;
        private int m_cookie;

        public TrackQuery(ContentClient client, ITrackQueryResult callback, int cookie) {
            m_cookie = cookie;
            m_callback = callback;
            m_client = client;
            m_token = m_client.acquireContentToken();
        }

        public boolean isBusy() {
            return m_busy;
        }

        public void execute(int trackId) {
            StringList columns = new StringList();
            columns.add("id");
            columns.add("name");
            columns.add("artist");
            columns.add("album_id");
            columns.add("uri");
            columns.add("albumimage");
            columns.add("source");

            ContentQueryArguments args = ContentQueryArguments.create("content://com.rightware.content/songs", columns);
            args.setFilter("id=" + Integer.toString(trackId));

            m_client.subscribeEx(m_token, args, this);
            m_busy = true;
        }

        public void contentSubscriptionResult(String uri, ContentData data) {
            m_busy = false;
            m_client.unsubscribe(m_token, uri);

            Log.d(TAG, "contentSubscriptionResult");

            if (data.getRowCount() > 0 && data.getColumnCount() > 0) {
                ContentRow row = data.getRow(0);

                TrackInfo ti = new TrackInfo();
                int colidx = 0;

                ti.m_id = row.getValueInt(colidx++);
                ti.m_name = row.getValueString(colidx++);
                ti.m_artist = row.getValueString(colidx++);
                ti.m_albumid = row.getValueInt(colidx++);
                ti.m_uri = row.getValueString(colidx++);
                ti.m_image =  row.getValueString(colidx++);
                ti.m_source = row.getValueString(colidx++);

                m_trackInfo = ti;

                m_callback.trackQueryComplete(m_cookie, m_trackInfo);
            }
        }
    };

    public void trackQueryComplete(int cookie, TrackInfo trackInfo) {
        try {
            if (cookie == TrackQueryCookie_Current) {
                Log.d(TAG, "trackQueryComplete(CURRENT)");

                m_currentTrack = trackInfo;

                if (m_state == Request_PlayTrack) {
                    m_state = Request_None;
                    if (m_currentTrack != null && m_currentPlaylist != null) {
                        startPlayCurrentTrack();
                    }
                }
            } else if (cookie == TrackQueryCookie_Previous) {
                Log.d(TAG, "trackQueryComplete(PREVIOUS)");

                m_previousTrack = trackInfo;
                reportTrackInformation();
            } else if (cookie == TrackQueryCookie_Next) {
                Log.d(TAG, "trackQueryComplete(NEXT)");

                m_nextTrack = trackInfo;
                reportTrackInformation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlaylistQuery extends ContentClient.ContentClientObserver {
        private final String TAG = "PlaylistQuery";

        private boolean m_busy = false;
        private VoidToken m_token;
        private RemoteContentClient m_client;
        private IPlaylistQueryResult m_callback;
        public PlaylistInfo m_playlist;

        public PlaylistQuery(RemoteContentClient client, IPlaylistQueryResult callback) {
            m_callback = callback;
            m_playlist = new PlaylistInfo();

            m_client = client;
            m_token = m_client.acquireContentToken();
        }

        public boolean isBusy() {
            return m_busy;
        }
        public void execute(int playlistId) {
            StringList columns = new StringList();

            columns.add("id");
            columns.add("name");
            columns.add("tracks");
            columns.add("source");

            ContentQueryArguments args = ContentQueryArguments.create("content://com.rightware.content/playlists", columns);
            args.setFilter("id=" + Integer.toString(playlistId));

            m_client.subscribeEx(m_token, args, this);
            m_busy = true;
        }

        public void contentSubscriptionResult(String uri, ContentData data) {
            m_client.unsubscribe(m_token, uri);
            m_busy = false;

            Log.d(TAG, "contentSubscriptionResult");

            if (data.getRowCount() > 0 && data.getColumnCount() > 0) {
                int colidx = 0;
                ContentRow row = data.getRow(0);

                PlaylistInfo info = new PlaylistInfo();

                // ID
                info.m_id = row.getValueInt(colidx++);

                // NAME
                info.m_name = row.getValueString(colidx++);

                // TRACKS
                info.m_tracks = null;
                info.m_tracksOriginal = row.getValueString(colidx);
                String [] tokens = info.m_tracksOriginal.split(";");
                if (tokens.length > 0) {
                    info.m_tracks = new int [tokens.length];
                    int index = 0;
                    for (String token : tokens) {
                        info.m_tracks[index++] = Integer.parseInt(token);
                    }
                }

                // SOURCE
                info.m_source = row.getValueString(colidx);

                Log.d(TAG, "contentSubscriptionResult: " + info.m_id + " - " + info.m_name + " - " + info.m_source + " - " + info.m_tracks);

                m_playlist = info;

                m_callback.playlistQueryComplete(m_playlist);
            }
        }
    };

    public void playlistQueryComplete(PlaylistInfo playlistInfo) {
        m_currentPlaylist = playlistInfo;
        reportPlaylistInformation();
        if (m_state == Request_PlayPlaylist) {
            m_state = Request_None;
        }
    }

    public MediaService(ClientConnector connector, Context context) {
        m_context = context;
        m_connector = connector;

        m_trackQuery = new TrackQuery(m_connector.getContentClient(), this, TrackQueryCookie_Current);
        m_previousTrackQuery = new TrackQuery(m_connector.getContentClient(), this, TrackQueryCookie_Previous);
        m_nextTrackQuery = new TrackQuery(m_connector.getContentClient(), this, TrackQueryCookie_Next);
        m_playlistQuery = new PlaylistQuery(m_connector.getContentClient(), this);

        /// Start report playback state.
        m_handler.postDelayed(m_playbackStateReporter, 500);

        // Will be started and stopped on demand.
        setPersistence(AbstractService.ServiceDescription.Persistence.StartOnDemand);
    }

    /**
     * Configures Server IP Address
     *
     * @param ipa the IP Address, used to map <SERVER> tags to a IP address
     *            when music is being played.
     */
    public void setServerIPAddress(String ipa) {
        m_serverIP = ipa;
    }

    protected AbstractService.ServiceControlResult onStartServiceRequest(ServiceArguments arguments) {
        Log.d(TAG, "onStartServiceRequest");
        getMusicPlayer();
        return ServiceControlResult.ServiceControlSuccess;
    }

    protected AbstractService.ServiceControlResult onStopServiceRequest() {
        Log.d(TAG, "onStopServiceRequest");
        stop();
        m_playbackState.reset();
        reportPlaybackState();
        return ServiceControlResult.ServiceControlSuccess;
    }

    public boolean playTrack(int playlistId, int trackId) {
        Log.d(TAG, "playTrack("+playlistId+","+trackId+")");

        if (m_playlistQuery.isBusy() || m_trackQuery.isBusy()) {
            Log.e(TAG, "Tried to query while track or playlist queries are busy");
            return false;
        } else {
            m_state = Request_PlayTrack;
            m_playlistQuery.execute(playlistId);
            m_trackQuery.execute(trackId);
        }
        return true;
    }

    public void playPlaylist(int playlistId) {
        Log.d(TAG, "playPlaylist(" + playlistId+")");
        if (m_playlistQuery.isBusy()) {
            Log.e(TAG, "tried to query while playlist query is busy");
        } else {
            m_state = Request_PlayPlaylist;
            m_playlistQuery.execute(playlistId);
        }
    }

    public void play() {
        Log.d(TAG, "play");
        if (getMusicPlayer().play()) {
            m_playbackState.m_state = Playback_Playing;
            reportPlaybackState();
        }
    }

    public void stop() {
        Log.d(TAG, "stop");
        if (getMusicPlayer().stop()) {
            m_playbackState.reset();
            reportPlaybackState();
        }
    }

    public void next() {
        Log.d(TAG, "next");
        if (m_currentPlaylist != null && m_currentTrack != null) {
            int currentTrackId = m_currentTrack.m_id;
            int nextTrackId = m_currentPlaylist.getNextTrack(currentTrackId);
            if (nextTrackId != -1) {
                stop();
                playTrack(m_currentPlaylist.m_id, nextTrackId);
            }
        }
    }

    public void previous() {
        Log.d(TAG, "previous");
        if (m_currentPlaylist != null && m_currentTrack != null) {
            int currentTrackId = m_currentTrack.m_id;

            int previousTrackId = m_currentPlaylist.getPreviousTrack(currentTrackId);
            if (previousTrackId != -1) {
                stop();
                playTrack(m_currentPlaylist.m_id, previousTrackId);
            }
        }
    }
    public void pause() {
        Log.d(TAG, "pause");
        if (getMusicPlayer().pause()) {
            m_playbackState.m_state = Playback_Paused;
            reportPlaybackState();
        }
    }

    private void reportTrackDomainInformation(String domain, TrackInfo trackInfo) {
        runtimeData().setValue(domain + ".id", trackInfo != null ? trackInfo.m_id : -1);
        runtimeData().setValue(domain + ".name", trackInfo != null ? trackInfo.m_name : "");
        runtimeData().setValue(domain + ".artist", trackInfo != null ? trackInfo.m_artist : "");
        runtimeData().setValue(domain + ".image", trackInfo != null ? trackInfo.m_image : "");
        runtimeData().setValue(domain + ".album_id", trackInfo != null ? trackInfo.m_albumid : -1);
        runtimeData().setValue(domain + ".uri", trackInfo != null ? trackInfo.m_uri : "");
        runtimeData().setValue(domain + ".uri", trackInfo != null ? trackInfo.m_source : "");
    }

    private void reportPlaylistInformation() {
        runtimeData().setValue("current_playlist.id", m_currentPlaylist != null ? m_currentPlaylist.m_id : -1);
        runtimeData().setValue("current_playlist.name", m_currentPlaylist != null ? m_currentPlaylist.m_name : "");
        runtimeData().setValue("current_playlist.tracks", m_currentPlaylist != null ? m_currentPlaylist.m_tracksOriginal : "");
        runtimeData().setValue("current_playlist.source", m_currentPlaylist != null ? m_currentPlaylist.m_source : "");

        runtimeData().notifyModified("current_playlist");
    }

    private void reportTrackInformation() {
        reportTrackDomainInformation("current_track", m_currentTrack);
        reportTrackDomainInformation("previous_track", m_previousTrack);
        reportTrackDomainInformation("next_track", m_nextTrack);

        runtimeData().notifyModified("");
    }

    public void reportPlaybackState() {
        if (m_playbackState.m_state == Playback_Stopped) {
            if (m_playbackState.m_stoppedReported == true) {
                m_playbackState.m_pausedReported = false;
                return;
            }
            m_playbackState.m_stoppedReported = true;
        } else if (m_playbackState.m_state == Playback_Paused) {
            m_playbackState.m_stoppedReported = false;
            if (m_playbackState.m_pausedReported == true) {
                return;
            }
            m_playbackState.m_pausedReported = true;
        } else {
            m_playbackState.m_stoppedReported = false;
            m_playbackState.m_pausedReported = false;
        }
        m_playbackState.m_duration = getMusicPlayer().getDuration() / 1000;
        m_playbackState.m_position = getMusicPlayer().getPosition() / 1000;

        runtimeData().setValue("playback.state", m_playbackState.m_state);
        runtimeData().setValue("playback.position", secondsToString(m_playbackState.m_position));
        runtimeData().setValue("playback.duration", secondsToString(m_playbackState.m_duration));

        float offset = 0;
        if (m_playbackState.m_duration > 0) {
            offset = (float)m_playbackState.m_position / (float)m_playbackState.m_duration;
        }
        runtimeData().setValue("playback.offset", offset);

        notifyProgressEventToAll(m_playbackState.m_position, m_playbackState.m_duration);

        Log.d(TAG, "reportPlaybackState");
        runtimeData().notifyModified("playback");
    }
    private String secondsToString(int secs) {
        int hours = secs / 3600;
        int minutes = (secs - (hours * 3600)) / 60;
        int seconds = secs - (hours * 3600) - (minutes * 60);

        if (hours > 0) {
            return String.format("%2d:", hours) + String.format("%02d:", minutes) + String.format("%02d", seconds);
        }

        return String.format("%02d:", minutes) + String.format("%02d", seconds);
    }

    private void startPlayCurrentTrack() {
        if (m_currentTrack == null) {
            Log.w(TAG, "CurrentTrack null when about to play.");
            return;
        }
        String mediaUri = m_currentTrack.m_uri;
        if (m_uriTranslator != null) {
            mediaUri = m_uriTranslator.translate(m_currentTrack.m_uri, false);
        }

        if (m_serverIP.length() > 0) {
            mediaUri = mediaUri.replace("<server>", m_serverIP).replace("<SERVER>", m_serverIP);
        }
        Log.d(TAG, "startPlay: " + mediaUri);
        if (getMusicPlayer().playUri(mediaUri)) {
            notifyTrackChanged();
            reportTrackInformation();
            m_playbackState.resetTo(Playback_Playing);
            reportPlaybackState();
        }
    }

    private void notifyTrackChanged() {
        m_previousTrack = null;
        m_nextTrack = null;

        if (m_currentPlaylist != null && m_currentTrack != null) {
            int pid = m_currentPlaylist.getPreviousTrack(m_currentTrack.m_id);
            if (pid != -1) {
                if (m_previousTrackQuery.isBusy()) {
                    Log.e(TAG, "tried to query while previous track query is busy.");
                } else {
                    m_previousTrackQuery.execute(pid);
                }
            }

            m_nextTrack = null;
            int nid = m_currentPlaylist.getNextTrack(m_currentTrack.m_id);
            if (nid != -1) {
                if (m_nextTrackQuery.isBusy()) {
                    Log.e(TAG, "tried to query while next track query is busy.");
                } else {
                    m_nextTrackQuery.execute(nid);
                }
            }
        }
    }

    //
    // Handling callbacks from actual music player.
    //

    public void musicPlayComplete() {
        Log.d(TAG, "musicPlayComplete");

        m_playbackState.reset();
        reportPlaybackState();

        if (m_currentPlaylist != null && m_currentTrack != null) {
            int nextTrack = m_currentPlaylist.getNextTrack(m_currentTrack.m_id);
            if (nextTrack != -1) {
                // Move to next track.
                playTrack(m_currentPlaylist.m_id, nextTrack);
            } else {
                // No more tracks. Start from beginning.
                playTrack(m_currentPlaylist.m_id, m_currentPlaylist.getFirstTrack());
            }
        }
    }

    public void musicPlayError() {
        Log.d(TAG, "musicPlayError");

        m_playbackState.reset();
        reportPlaybackState();
    }
}
