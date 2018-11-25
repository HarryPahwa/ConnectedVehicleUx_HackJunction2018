// Auto-generated code. DO NOT EDIT!

package com.rightware.connect.weather;

import android.util.Log;

import com.rightware.connect.*;
import java.util.Vector;

public class WeatherServiceConcept extends ExternalServiceBase {
    static final String TAG = "WeatherServiceConcept";

    public WeatherServiceConcept() {
        super("Connect.Service.Weather");
    }

    // custom datatypes defined by the service.
    public static class CustomTypes {
    }
    //
    // Protocol messages (METHOD)
    //
    static final String message_method_setCity =
            MessageUtil.methodNameToMessageName("Weather", "setCity");

    //
    // Weather API
    //
    protected void setCity(String cityname) { return; }
    //
    // EVENT: searchStateChanged
    //
    protected MessagePackage createnotifySearchStateChangedEventMessage(int state) {
        MessagePackage _event = new MessagePackage();
        final String _type = MessageUtil.eventNameToMessageName("Weather", "searchStateChanged");
        _event.setType(_type);
        _event.addIntAttribute(MessagePackage.FixedAttributeKeys.ATTRIBUTE_KEY_ARGUMENT_1.swigValue()+0, state);
        return _event;
    }

    protected void notifySearchStateChangedEventTo(ExternalServiceSession _session, int state) {
        MessagePackage _event = createnotifySearchStateChangedEventMessage(state);
        if (_event != null && _session != null) {
            _session.transmitToSession(_event);
        }
    }
    protected void notifySearchStateChangedEvent(int state) {
        notifySearchStateChangedEventTo(getRunningSession(), state);
    }
    protected void notifySearchStateChangedEventToAll(int state) {
        transmitToAll(createnotifySearchStateChangedEventMessage(state));
    }

    //
    // Message Dispatcher
    //
    public void receive(ExternalServiceSession session, MessagePackage message) {
        Log.d(TAG, "Bingo Bango Bongo");
        setRunningSession(session);
        String type = message.getType();
        try {
            if (type.equals(message_method_setCity)) {
                setCity(MessageUtil.getStringArg(message,0));
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
            "    <search>"+
            "      "+
            "      <criteria type=\"string\"/>"+
            "      "+
            "      <data type=\"string\"/>"+
            "      "+
            "      <state type=\"int\"/>"+
            "      "+
            "      <errordescription type=\"string\"/>"+
            "    </search>"+
            "    "+
            "    <result>"+
            "    "+
            "      "+
            "      <temperature type=\"int\"/>"+
            "      "+
            "      "+
            "      <windspeed type=\"String\"/>"+
            "      "+
            "      "+
            "      <winddirection type=\"int\"/>"+
            "      "+
            "      "+
            "      <humidity type=\"bool\"/>"+
            "      "+
            "      "+
            "      <cloudiness type=\"String\"/>"+
            "    "+
            "      "+
            "      "+
            "      <icon type=\"int\"/>    "+
            "      "+
            "    </result>"+
            "  </runtime-data>";
    }

    //
    // SERVICE DESCRIPTION
    //
    public String getSchema() {
        return "<service description=\"interface for weather data integration\" name=\"Weather\" namespace=\"kanzi\" package=\"com.rightware.connect.weather\">"+
            "  <method description=\"Sets the name of the city whom weather is to be shown\" name=\"setCity\">"+
            "    <argument datatype=\"string\" name=\"cityname\"/>"+
            "  </method>"+
            "  "+
            "  <event description=\"emit when ever search state changes\" name=\"searchStateChanged\">"+
            "    <argument datatype=\"int\" name=\"state\"/>"+
            "  </event>"+
            "  "+
            "  "+
            "  "+
            "" + provideServiceRuntimeDataOverride() + "</service>";
    }

    //
    // ROUTING INFORMATION
    //
    public String provideServiceRoutingInformationOverride() {
        return "<routing><route link=\"reliable\" type=\"server\"><method name=\"setCity\"/><event name=\"searchStateChanged\"/><runtimedata/></route></routing>";
    }
}
