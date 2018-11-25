// Auto-generated code. DO NOT EDIT!

package com.rightware.connect.sensor;

import com.rightware.connect.*;
import java.util.Vector;

public class SensorServiceConcept extends ExternalServiceBase {
    static final String TAG = "SensorServiceConcept";

    public SensorServiceConcept() {
        super("Connect.Service.Sensor");
    }

    // custom datatypes defined by the service.
    public static class CustomTypes {
    }
    //
    // Protocol messages (METHOD)
    //
    static final String message_method_setAccelerometerState =
            MessageUtil.methodNameToMessageName("Sensor", "setAccelerometerState");
    static final String message_method_setGravitySensorState =
            MessageUtil.methodNameToMessageName("Sensor", "setGravitySensorState");
    static final String message_method_setGyroscopeState =
            MessageUtil.methodNameToMessageName("Sensor", "setGyroscopeState");
    static final String message_method_setCompassState =
            MessageUtil.methodNameToMessageName("Sensor", "setCompassState");

    //
    // Sensor API
    //
    protected void setAccelerometerState(boolean enabled) { return; }
    protected void setGravitySensorState(boolean enabled) { return; }
    protected void setGyroscopeState(boolean enabled) { return; }
    protected void setCompassState(boolean enabled) { return; }
    //
    // Message Dispatcher
    //
    public void receive(ExternalServiceSession session, MessagePackage message) {
        setRunningSession(session);
        String type = message.getType();
        try {
            if (type.equals(message_method_setAccelerometerState)) {
                setAccelerometerState(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setGravitySensorState)) {
                setGravitySensorState(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setGyroscopeState)) {
                setGyroscopeState(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setCompassState)) {
                setCompassState(MessageUtil.getBooleanArg(message,0));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setRunningSession(null);
    }

    //
    // RUNTIME DATA SCHEMA
    //
    public String provideServiceRuntimeDataOverride() {
        return "<runtime-data>"+
            "    <accelerometer>                                  "+
            "        <enabled type=\"bool\"/>                    "+
            "        <x type=\"float\"/>                         "+
            "        <y type=\"float\"/>                         "+
            "        <z type=\"float\"/>                         "+
            "    </accelerometer>                                 "+
            "    <gravity>                                        "+
            "        <enabled type=\"bool\"/>                    "+
            "        <x type=\"float\"/>                         "+
            "        <y type=\"float\"/>                         "+
            "        <z type=\"float\"/>                         "+
            "    </gravity>                                       "+
            "    <gyroscope>                                      "+
            "        <enabled type=\"bool\"/>                    "+
            "        <x type=\"float\"/>                         "+
            "        <y type=\"float\"/>                         "+
            "        <z type=\"float\"/>                         "+
            "    </gyroscope>  "+
            "    <compass>"+
            "        <enabled type=\"bool\"/>                    "+
            "        <x type=\"float\"/>                         "+
            "        <y type=\"float\"/>                         "+
            "        <z type=\"float\"/>                         "+
            "    </compass>"+
            "    <pressure>"+
            "        <enabled type=\"bool\"/>"+
            "        <value type=\"float\"/>"+
            "    </pressure>    "+
            "  </runtime-data>";
    }

    //
    // SERVICE DESCRIPTION
    //
    public String getSchema() {
        return "<service description=\"interface for motion tracking sensors\" name=\"Sensor\" namespace=\"kanzi\" package=\"com.rightware.connect.sensor\">"+
            "  <method description=\"Enable / Disable accelerometer.\" name=\"setAccelerometerState\">"+
            "    <argument datatype=\"bool\" name=\"enabled\"/>"+
            "  </method>"+
            "  <method description=\"Enable / Disable gravity sensor.\" name=\"setGravitySensorState\">"+
            "    <argument datatype=\"bool\" name=\"enabled\"/>"+
            "  </method>"+
            "  <method description=\"Enable / Disable gyroscope.\" name=\"setGyroscopeState\">"+
            "    <argument datatype=\"bool\" name=\"enabled\"/>"+
            "  </method>"+
            "  <method description=\"Enable / Disable compass.\" name=\"setCompassState\">"+
            "    <argument datatype=\"bool\" name=\"enabled\"/>"+
            "  </method>"+
            "  "+
            "  "+
            "" + provideServiceRuntimeDataOverride() + "</service>";
    }

    //
    // ROUTING INFORMATION
    //
    public String provideServiceRoutingInformationOverride() {
        return "<routing><route link=\"reliable\" type=\"server\"><method name=\"setAccelerometerState\"/><method name=\"setGravitySensorState\"/><method name=\"setGyroscopeState\"/><method name=\"setCompassState\"/><runtimedata/></route></routing>";
    }
}
