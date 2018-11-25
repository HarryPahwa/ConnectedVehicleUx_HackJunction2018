package com.rightware.junction2018;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rightware.connect.AbstractService;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.rightware.connect.ClientConnector;
import com.rightware.connect.ConfigurationReader;
import com.rightware.connect.KanziConnectContext;
import com.rightware.connect.StringMap;
import com.rightware.connect.abc123.Abc123Service;
import com.rightware.connect.media.MediaService;
import com.rightware.connect.sensor.SensorService;
import com.rightware.connect.weather.WeatherService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PermissionRequestCodeExternalStorage = 1;

    private ServiceHandler m_mediaServiceHandler;
    private ServiceHandler m_sensorServiceHandler;
    private ServiceHandler m_weatherServiceHandler;
    private ServiceHandler m_abc123ServiceHandler;

    private static StringMap m_attributes;
    private static String m_ip;
    private static String m_port;

    private static com.rightware.connect.media.SongContentProvider m_songContentProvider = null;
    private static com.rightware.connect.media.PlaylistContentProvider m_playlistContentProvider = null;

    private class ServiceHandler extends ClientConnector.StateCallback {
        private static final String TAG = "ServiceHandler";
        private KanziConnectContext m_context;
        private String m_ip;
        private String m_port;
        private ClientConnector.State m_state;
        private MainActivity m_parent;
        private AbstractService m_service;
        private FirebaseDatabase database;
        private DatabaseReference databaseRef;

        /**
         * Callback is invoked when connector state changes
         * @param state new connector state
         */
        public void stateChanged(ClientConnector.State state) {
            Log.d(TAG, "stateChanged: " + state);

            /// Callbacks from Kanzi Connect must not throw exceptions
            try {
                m_state = state;
                m_parent.updateConnectionState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Retrieve connector state
         * @return state
         */
        public ClientConnector.State getState() {
            return m_state;
        }

        /**
         * Connection state
         * @return true if connected
         */
        public boolean isConnected() {
            return m_state == ClientConnector.State.Connected;
        }

        /**
         * Idle state
         * @return true if idling (not connecting nor connected).
         */
        public boolean isIdle() {
            return  m_state == ClientConnector.State.Disconnected ||
                    m_state == ClientConnector.State.NotPrepared;
        }

        /**
         * Currently connecting ?
         * @return true if connecting
         */
        public boolean isConnecting() {
            return isConnected() == false && isIdle() == false;
        }

        /**
         * Constructor
         */
        ServiceHandler(MainActivity parent, String ip, String port, StringMap attributes) {
            m_context = new KanziConnectContext(3);
            m_context.setLogging(true);
            m_state = ClientConnector.State.NotPrepared;
            m_parent = parent;

            m_attributes = attributes;
            m_ip = ip;
            m_port = port;

            Log.d(TAG, "ServiceHandler instance created. (ip = " + m_ip + " port = " + m_port + ")");
        }

        /**
         * retrieve client connector instance.
         * @return client connector
         */
        public ClientConnector getConnector() {
            return m_context.getClientConnector();
        }

        /**
         * Initialize handler with the service instance.
         * @param service the service to initialize handler with.
         * @return true on success
         */
        public boolean initialize(AbstractService service) {
            if (m_context.initialize(m_attributes, service) == true) {
                m_context.start();
                m_context.getClientConnector().addStateChangeCallback(this);
                m_service = service;
                return true;
            }

            return false;
        }

        /**
         * Retrieve configured IP Address
         * @return
         */
        public String getIP() { return m_ip; }

        /**
         * Retrieve configured port.
         * @return
         */
        public String getPort() { return m_port; }
    }

    /**
     * Checks whether application has permissions to read/write external storage
     * @return true if permissions were granted.
     */
    protected boolean verifyPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionRequestCodeExternalStorage);
            return false;
        } else {
            return true; // We have permissions.
        }
    }

    /**
     * Invoked after permissions provided/declined
     * @param requestCode the code passed when requesting
     * @param permissions requested permissions
     * @param grantResults results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PermissionRequestCodeExternalStorage) {
            Log.d(TAG, "onRequestPermissionsResult");
            start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ensures application has permissions to
        // read connection.xml from the external memory.
        if (verifyPermissions() == true) {
            start();
        }


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
    }

    /**
     * Updates the UI to reflect to current connection status.
     */
    void updateConnectionState() {
        TextView status = (TextView)findViewById(R.id.statustext);
        TextView ipinfo = (TextView)findViewById(R.id.ipaddress);
        Resources r = getResources();

        if (ipinfo != null && m_weatherServiceHandler != null) {
            String out = m_weatherServiceHandler.getIP() + ":" + m_weatherServiceHandler.getPort();
            ipinfo.setText(out);
        }

        // Connected if all services are connected.
        if (    m_weatherServiceHandler.isConnected() &&
                m_sensorServiceHandler.isConnected() &&
                m_mediaServiceHandler.isConnected()) {
            status.setText("CONNECTED");
            status.setTextColor(ResourcesCompat.getColor(r, R.color.colorGreen, null));
        } else if (   m_weatherServiceHandler.isConnecting() ||
                    m_sensorServiceHandler.isConnecting() ||
                    m_mediaServiceHandler.isConnecting()) {
            // Connecting if any of the services is connecting
            status.setText("CONNECTING");
            status.setTextColor(ResourcesCompat.getColor(r, R.color.colorYellow, null));
        } else {
            // Idling otherwise.
            Log.d(TAG, "States: " + m_weatherServiceHandler.getState() + "," + m_sensorServiceHandler.getState() + "," + m_mediaServiceHandler.getState()+ "," + m_abc123ServiceHandler.getState());
            status.setText("IDLE");
            status.setTextColor(ResourcesCompat.getColor(r, R.color.colorWhite, null));
        }
    }

    /**
     * Starts all services
     */
    void start() {
        Log.d(TAG, "start()");

        if (m_attributes == null) {
            m_attributes = ConfigurationReader.getConnectionParametersStatic("");
            m_ip = m_attributes.get("host_ip");
            m_port = m_attributes.get("host_port");
        }

        startMediaService();
        startSensorService();
        startWeatherService();
        startAbc123Service();
        /// TODO: Add custom service start routine here.
    }

    /**
     * Starts the weather service
     */
    void startWeatherService() {
        if (m_weatherServiceHandler == null) {
            m_weatherServiceHandler = new ServiceHandler(this, m_ip, m_port, m_attributes);
            m_weatherServiceHandler.initialize(new WeatherService(this));
        }
    }

    /**
     * Starts the abc123 service
     */
    void startAbc123Service() {
        if (m_abc123ServiceHandler == null) {
            m_abc123ServiceHandler = new ServiceHandler(this, m_ip, m_port, m_attributes);
            m_abc123ServiceHandler.initialize(new Abc123Service(this));
        }
    }

    /**
     * Starts the sensor service
     */
    void startSensorService() {
        if (m_sensorServiceHandler == null) {
            m_sensorServiceHandler = new ServiceHandler(this, m_ip, m_port, m_attributes);
            m_sensorServiceHandler.initialize(new SensorService(this));
        }
    }

    /**
     * Starts media service
     */
    void startMediaService() {
        if (m_mediaServiceHandler != null) {
            return;
        }
        m_mediaServiceHandler = new ServiceHandler(this, m_ip, m_port, m_attributes);

        // media service likes to know the ip of the server that potentially
        // provides also content to be played back.

        // -> Install content provider that will publish a single playlist that contains
        // all the music tracks found from the device.
        if (m_playlistContentProvider == null) {
            m_playlistContentProvider = new com.rightware.connect.media.PlaylistContentProvider(
                    MainActivity.this,
                    m_mediaServiceHandler.getConnector().getWorkQueueEx(),
                    m_mediaServiceHandler.getConnector().getContentClient(),
                    m_mediaServiceHandler.getConnector().getVirtualFileClient());
        }

        // -> Install a content provider that provides detailed information about
        // the songs within the device
        if (m_songContentProvider == null) {
            m_songContentProvider = new com.rightware.connect.media.SongContentProvider(
                    MainActivity.this,
                    m_mediaServiceHandler.getConnector().getWorkQueueEx(),
                    m_mediaServiceHandler.getConnector().getContentClient(),
                    m_mediaServiceHandler.getConnector().getVirtualFileClient());
        }

        MediaService ms = new MediaService(m_mediaServiceHandler.getConnector(), this);
        // -> media service needs to know the IP address of the server as it likes
        // to receive relative paths to resources inside the server.
        ms.setServerIPAddress(m_ip);
        m_mediaServiceHandler.initialize(ms);
    }

    /**
     * Stops service handler
     * @param handler the handler to stop
     */
    private void stopServiceHandler(ServiceHandler handler) {
        if (handler != null) {
            handler.getConnector().stop();
        }
    }
    /**
     * Stops the services.
     */
    private void stop() {
        Log.d(TAG, "stop()");

        stopServiceHandler(m_mediaServiceHandler);
        m_mediaServiceHandler = null;
        m_songContentProvider = null;
        m_playlistContentProvider = null;

        stopServiceHandler(m_weatherServiceHandler);
        m_weatherServiceHandler = null;

        stopServiceHandler(m_sensorServiceHandler);
        m_sensorServiceHandler = null;

        /// TODO: Add custom service stop routine here.

        stopServiceHandler(m_abc123ServiceHandler);
        m_abc123ServiceHandler = null;
    }
}
