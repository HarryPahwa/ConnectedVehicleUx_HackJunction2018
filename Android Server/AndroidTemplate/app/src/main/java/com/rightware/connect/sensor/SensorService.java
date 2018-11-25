// Auto-generated stub code.
// This file can be edited and will not get overwritten by code generation tools.

package com.rightware.connect.sensor;

import java.util.HashMap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import com.rightware.connect.*;

public class SensorService extends SensorServiceConcept implements SensorEventListener {
    private static final String TAG = "SensorService";

    /// Manager for sensors.
    private SensorManager m_manager;

    public static final String TYPE_ACCELEROMETER   = "ACCELEROMETER";
    public static final String TYPE_GRAVITY         = "GRAVITY";
    public static final String TYPE_GYROSCOPE       = "GYRO";
    public static final String TYPE_COMPASS         = "COMPASS";
    public static final String TYPE_PRESSURE        = "PRESSURE";

    private Context m_context;

    // Sensors currently being monitored.
    private HashMap<String, Sensor> m_sensors = new HashMap<String, Sensor>();

    public SensorService(Context context) {
        m_context = context;
        m_manager = (SensorManager) m_context.getSystemService(Context.SENSOR_SERVICE);
    }

    protected AbstractService.ServiceControlResult onStartServiceRequest(ServiceArguments arguments) {
        Log.d(TAG, "onStartServiceRequest");

        setAccelerometerState(true);
        setGravitySensorState(true);
        setGyroscopeState(true);
        setCompassState(true);
        setPressureState(true);

        return ServiceControlResult.ServiceControlSuccess;
    }

    protected void setAccelerometerState(boolean enabled) {
        Log.i(TAG, "setAccelerometerState: " + enabled);
        setSensorState(TYPE_ACCELEROMETER, enabled);
    }
    protected void setGravitySensorState(boolean enabled) {
        Log.i(TAG, "setGravitySensorState: " + enabled);
        setSensorState(TYPE_GRAVITY, enabled);
    }
    protected void setGyroscopeState(boolean enabled) {
        Log.i(TAG, "setGyroscopeState: " + enabled);
        setSensorState(TYPE_GYROSCOPE, enabled);
    }
    protected void setCompassState(boolean enabled) {
        Log.i(TAG, "setCompassState: " + enabled);
        setSensorState(TYPE_COMPASS, enabled);
    }
    protected void setPressureState(boolean enabled) {
        Log.i(TAG, "setPressureState: " + enabled);
        setSensorState(TYPE_PRESSURE, enabled);
    }

    public void setSensorState(String sensorType, boolean enabled) {
        Log.d(TAG, "setSensorState: " + sensorType + " = " + enabled);

        if (enabled) {
            // See if we're currently listening for this type of sensor.
            if (!m_sensors.containsKey(sensorType)) {
                Sensor sensor = null;
                if (sensorType == TYPE_ACCELEROMETER) {
                    sensor = m_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                }
                else if (sensorType == TYPE_GRAVITY) {
                    sensor = m_manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                }
                else if (sensorType == TYPE_GYROSCOPE) {
                    sensor = m_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                }
                else if (sensorType == TYPE_COMPASS) {
                    sensor = m_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                }
                else if (sensorType == TYPE_PRESSURE) {
                    sensor = m_manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                }

                if (sensor != null) {
                    m_manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                    m_sensors.put(sensorType, sensor);

                    notifySensorStateChanged(sensorType, enabled);
                } else {
                    Log.d(TAG, "Unable to get sensor of type: " + sensorType);
                }
            }
        }
        else {
            // See if we're currently listening for this type of sensor.
            if (m_sensors.containsKey(sensorType)) {
                m_manager.unregisterListener(this, m_sensors.get(sensorType));
                m_sensors.remove(sensorType);

                notifySensorStateChanged(sensorType, enabled);
                notifySensorData(sensorType, 0, 0, 0);
            }
        }
    }

    private void notifySensorStateChanged(String sensorType, boolean enabled) {
        Log.d(TAG, "Sensor " + sensorType + " state changed to " + enabled);
        if (sensorType == TYPE_ACCELEROMETER) {
            runtimeData().setValue("accelerometer.enabled", enabled);
            runtimeData().notifyModified("accelerometer");
        }
        else if (sensorType == TYPE_GRAVITY) {
            runtimeData().setValue("gravity.enabled", enabled);
            runtimeData().notifyModified("gravity");
        }
        else if (sensorType == TYPE_GYROSCOPE) {
            runtimeData().setValue("gyroscope.enabled", enabled);
            runtimeData().notifyModified("gyroscope");
        }
        else if (sensorType == TYPE_COMPASS) {
            runtimeData().setValue("compass.enabled", enabled);
            runtimeData().notifyModified("compass");
        }
        else if (sensorType == TYPE_PRESSURE) {
            runtimeData().setValue("pressure.enabled", enabled);
            runtimeData().notifyModified("pressure");
        }
    }

    private void notifySensorData(String sensorType, float x, float y, float z) {
        if (sensorType == TYPE_ACCELEROMETER) {
            runtimeData().setValue("accelerometer.x", x);
            runtimeData().setValue("accelerometer.y", y);
            runtimeData().setValue("accelerometer.z", z);
            runtimeData().notifyModified("accelerometer");
        }
        else if (sensorType == TYPE_GRAVITY) {
            runtimeData().setValue("gravity.x", x);
            runtimeData().setValue("gravity.y", y);
            runtimeData().setValue("gravity.z", z);
            runtimeData().notifyModified("gravity");
        }
        else if (sensorType == TYPE_GYROSCOPE) {
            runtimeData().setValue("gyroscope.x", x);
            runtimeData().setValue("gyroscope.y", y);
            runtimeData().setValue("gyroscope.z", z);
            runtimeData().notifyModified("gyroscope");
        }
        else if (sensorType == TYPE_COMPASS) {
            runtimeData().setValue("compass.x", x);
            runtimeData().setValue("compass.y", x);
            runtimeData().setValue("compass.z", x);
            runtimeData().notifyModified("compass");
        }
        else if (sensorType == TYPE_PRESSURE) {
            runtimeData().setValue("pressure.value", x);
            runtimeData().notifyModified("pressure");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = 0;
        float y = 0;
        float z = 0;
        if (event.values.length > 0) {
            x = event.values[0];
        }
        if (event.values.length > 1) {
            y = event.values[1];
        }
        if (event.values.length > 2) {
            z = event.values[2];
        }

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                notifySensorData(TYPE_ACCELEROMETER, x, y, z);
                break;

            case Sensor.TYPE_GRAVITY:
                notifySensorData(TYPE_GRAVITY, x, y, z);
                break;

            case Sensor.TYPE_GYROSCOPE:
                notifySensorData(TYPE_GYROSCOPE, x, y, z);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                notifySensorData(TYPE_COMPASS, x, y, z);
                break;

            case Sensor.TYPE_PRESSURE:
                notifySensorData(TYPE_PRESSURE, z, y, z);
                break;
        }
    }
}



