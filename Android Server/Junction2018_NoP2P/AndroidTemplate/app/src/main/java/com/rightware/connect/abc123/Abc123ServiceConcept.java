// Auto-generated code. DO NOT EDIT!

package com.rightware.connect.abc123;

import com.rightware.connect.*;
import java.util.Vector;

public class Abc123ServiceConcept extends ExternalServiceBase {
    static final String TAG = "Abc123ServiceConcept";

    public Abc123ServiceConcept() {
        super("Connect.Service.Abc123");
    }

    // custom datatypes defined by the service.
    public static class CustomTypes {
    }
    //
    // Protocol messages (WRITEPROPERTY)
    //
    static final String message_method_setDefaultHeartrate =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultHeartrate");
    static final String message_method_setDefaultStress =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultStress");
    static final String message_method_setDefaultAccident =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultAccident");
    static final String message_method_setDefaultSleepstatus =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultSleepstatus");
    static final String message_method_setDefaultObjectproximity =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultObjectproximity");
    static final String message_method_setDefaultElement_1 =
            MessageUtil.writepropertyMethodNameToMessageName("Abc123", "setDefaultElement_1");

    //
    // Message Dispatcher
    //
    public void receive(ExternalServiceSession session, MessagePackage message) {
        setRunningSession(session);
        String type = message.getType();
        try {
            if (type.equals(message_method_setDefaultHeartrate)) {
                setDefaultHeartrate(MessageUtil.getIntArg(message,0));
            } else if (type.equals(message_method_setDefaultStress)) {
                setDefaultStress(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setDefaultAccident)) {
                setDefaultAccident(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setDefaultSleepstatus)) {
                setDefaultSleepstatus(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setDefaultObjectproximity)) {
                setDefaultObjectproximity(MessageUtil.getBooleanArg(message,0));
            } else if (type.equals(message_method_setDefaultElement_1)) {
                setDefaultElement_1(MessageUtil.getIntArg(message,0));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setRunningSession(null);
    }
    protected void setDefaultHeartrate(int argument) {
        RuntimeDataObject_int object = runtimeData().getIntObject("default.heartrate");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }
    protected void setDefaultStress(boolean argument) {
        RuntimeDataObject_bool object = runtimeData().getBoolObject("default.stress");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }
    protected void setDefaultAccident(boolean argument) {
        RuntimeDataObject_bool object = runtimeData().getBoolObject("default.accident");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }
    protected void setDefaultSleepstatus(boolean argument) {
        RuntimeDataObject_bool object = runtimeData().getBoolObject("default.sleepstatus");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }
    protected void setDefaultObjectproximity(boolean argument) {
        RuntimeDataObject_bool object = runtimeData().getBoolObject("default.objectproximity");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }
    protected void setDefaultElement_1(int argument) {
        RuntimeDataObject_int object = runtimeData().getIntObject("default.element_1");
        if (object != null) {
            object.setValue(argument);
            runtimeData().notifyModified();
        }
    }

    //
    // RUNTIME DATA SCHEMA
    //
    public String provideServiceRuntimeDataOverride() {
        return "<runtime-data>"+
            "		<default>"+
            "			<heartrate default=\"116\" type=\"int\" writable=\"1\"/>"+
            "			<stress default=\"false\" type=\"bool\" writable=\"1\"/>"+
            "			<accident default=\"false\" type=\"bool\" writable=\"1\"/>"+
            "			<sleepstatus default=\"false\" type=\"bool\" writable=\"1\"/>"+
            "			<objectproximity default=\"false\" type=\"bool\" writable=\"1\"/>"+
            "			<element_1 default=\"0\" type=\"int\" writable=\"1\"/>"+
            "		</default>"+
            "	</runtime-data>";
    }

    //
    // SERVICE DESCRIPTION
    //
    public String getSchema() {
        return "<service description=\"interface for Junction sensors\" name=\"Abc123\" namespace=\"kanzi\" package=\"com.rightware.connect.abc123\">"+
            "	"+
            "<method name=\"setDefaultHeartrate\"><argument datatype=\"int\" link=\"reliable\" name=\"value\" route=\"server\"/></method><method name=\"setDefaultStress\"><argument datatype=\"bool\" link=\"reliable\" name=\"value\" route=\"server\"/></method><method name=\"setDefaultAccident\"><argument datatype=\"bool\" link=\"reliable\" name=\"value\" route=\"server\"/></method><method name=\"setDefaultSleepstatus\"><argument datatype=\"bool\" link=\"reliable\" name=\"value\" route=\"server\"/></method><method name=\"setDefaultObjectproximity\"><argument datatype=\"bool\" link=\"reliable\" name=\"value\" route=\"server\"/></method><method name=\"setDefaultElement_1\"><argument datatype=\"int\" link=\"reliable\" name=\"value\" route=\"server\"/></method>" + provideServiceRuntimeDataOverride() + "</service>";
    }

    //
    // ROUTING INFORMATION
    //
    public String provideServiceRoutingInformationOverride() {
        return "<routing><route link=\"reliable\" type=\"server\"><runtimedata/><method name=\"setDefaultHeartrate\"/><method name=\"setDefaultStress\"/><method name=\"setDefaultAccident\"/><method name=\"setDefaultSleepstatus\"/><method name=\"setDefaultObjectproximity\"/><method name=\"setDefaultElement_1\"/></route></routing>";
    }
}
