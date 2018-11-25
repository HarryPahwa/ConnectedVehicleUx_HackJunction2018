// Auto-generated code. DO NOT EDIT!

package com.rightware.connect.media;

import com.rightware.connect.*;
import java.util.Vector;

public class MediaServiceConcept extends ExternalServiceBase {
    static final String TAG = "MediaServiceConcept";

    public MediaServiceConcept() {
        super("Connect.Service.Media");
    }

    // custom datatypes defined by the service.
    public static class CustomTypes {
    }
    //
    // Protocol messages (METHOD)
    //
    static final String message_method_playPlaylist =
            MessageUtil.methodNameToMessageName("Media", "playPlaylist");
    static final String message_method_playTrack =
            MessageUtil.methodNameToMessageName("Media", "playTrack");
    static final String message_method_play =
            MessageUtil.methodNameToMessageName("Media", "play");
    static final String message_method_stop =
            MessageUtil.methodNameToMessageName("Media", "stop");
    static final String message_method_pause =
            MessageUtil.methodNameToMessageName("Media", "pause");
    static final String message_method_next =
            MessageUtil.methodNameToMessageName("Media", "next");
    static final String message_method_previous =
            MessageUtil.methodNameToMessageName("Media", "previous");

    //
    // Media API
    //
    protected void playPlaylist(int playlistId) { return; }
    protected boolean playTrack(int playlistId, int trackId) { return false; }
    protected void play() { return; }
    protected void stop() { return; }
    protected void pause() { return; }
    protected void next() { return; }
    protected void previous() { return; }
    //
    // EVENT: progress
    //
    protected MessagePackage createnotifyProgressEventMessage(int position, int duration) {
        MessagePackage _event = new MessagePackage();
        final String _type = MessageUtil.eventNameToMessageName("Media", "progress");
        _event.setType(_type);
        _event.addIntAttribute(MessagePackage.FixedAttributeKeys.ATTRIBUTE_KEY_ARGUMENT_1.swigValue()+0, position);
        _event.addIntAttribute(MessagePackage.FixedAttributeKeys.ATTRIBUTE_KEY_ARGUMENT_1.swigValue()+1, duration);
        return _event;
    }

    protected void notifyProgressEventTo(ExternalServiceSession _session, int position, int duration) {
        MessagePackage _event = createnotifyProgressEventMessage(position, duration);
        if (_event != null && _session != null) {
            _session.transmitToSession(_event);
        }
    }
    protected void notifyProgressEvent(int position, int duration) {
        notifyProgressEventTo(getRunningSession(), position, duration);
    }
    protected void notifyProgressEventToAll(int position, int duration) {
        transmitToAll(createnotifyProgressEventMessage(position, duration));
    }

    //
    // Message Dispatcher
    //
    public void receive(ExternalServiceSession session, MessagePackage message) {
        setRunningSession(session);
        String type = message.getType();
        try {
            if (type.equals(message_method_playPlaylist)) {
                playPlaylist(MessageUtil.getIntArg(message,0));
            } else if (type.equals(message_method_playTrack)) {
                reportBooleanResult(message, playTrack(MessageUtil.getIntArg(message,0), MessageUtil.getIntArg(message,1)));
            } else if (type.equals(message_method_play)) {
                play();
            } else if (type.equals(message_method_stop)) {
                stop();
            } else if (type.equals(message_method_pause)) {
                pause();
            } else if (type.equals(message_method_next)) {
                next();
            } else if (type.equals(message_method_previous)) {
                previous();
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
            "    <playback>                                                        "+
            "        <state type=\"int\"/>                                        "+
            "        <position default=\"00:00\" type=\"string\"/>                "+
            "        <duration default=\"00:00\" type=\"string\"/>                "+
            "        <offset type=\"float\"/>                                     "+
            "    </playback>                                                       "+
            "    <current_playlist>                                                "+
            "        <id type=\"int\"/>                                           "+
            "        <name type=\"string\"/>                                      "+
            "        <tracks type=\"string\"/>                                    "+
            "        <source type=\"string\"/>                                    "+
            "    </current_playlist>                                               "+
            "    <current_track>                                                   "+
            "        <id type=\"int\"/>                                           "+
            "        <name type=\"string\"/>                                      "+
            "        <artist type=\"string\"/>                                    "+
            "        <album_id type=\"int\"/>                                     "+
            "        <uri type=\"string\"/>                                       "+
            "        <image type=\"string\"/>                                     "+
            "        <source type=\"string\"/>                                    "+
            "    </current_track>                                                  "+
            "    <previous_track>                                                  "+
            "        <id type=\"int\"/>                                           "+
            "        <name type=\"string\"/>                                      "+
            "        <artist type=\"string\"/>                                    "+
            "        <album_id type=\"int\"/>                                     "+
            "        <uri type=\"string\"/>                                       "+
            "        <image type=\"string\"/>                                     "+
            "        <source type=\"string\"/>                                    "+
            "    </previous_track>                                                 "+
            "    <next_track>                                                      "+
            "        <id type=\"int\"/>                                           "+
            "        <name type=\"string\"/>                                      "+
            "        <artist type=\"string\"/>                                    "+
            "        <album_id type=\"int\"/>                                     "+
            "        <uri type=\"string\"/>                                       "+
            "        <image type=\"string\"/>                                     "+
            "        <source type=\"string\"/>                                    "+
            "    </next_track>"+
            "  </runtime-data>";
    }

    //
    // SERVICE DESCRIPTION
    //
    public String getSchema() {
        return "<service description=\"Media playback interface\" name=\"Media\" namespace=\"kanzi\" package=\"com.rightware.connect.media\">"+
            "  <method description=\"Starts playing given playlist from the first track\" name=\"playPlaylist\">"+
            "    <argument datatype=\"int\" name=\"playlistId\"/>"+
            "  </method>"+
            "  "+
            "  <method description=\"Starts playing given track in given playlist\" name=\"playTrack\" return=\"bool\">"+
            "    <argument datatype=\"int\" name=\"playlistId\"/>"+
            "    <argument datatype=\"int\" name=\"trackId\"/>"+
            "  </method>"+
            "  "+
            "  <method description=\"Continues playback of current track\" name=\"play\"/>"+
            "  <method description=\"Stops playback of current track\" name=\"stop\"/>"+
            "  "+
            "  <method description=\"Pauses playback of current track\" name=\"pause\"/>"+
            "  <event description=\"Notification of playback state change\" name=\"progress\">"+
            "    <argument datatype=\"int\" name=\"position\">"+
            "      <attribute key=\"DisplayName\" value=\"Playback position (s)\"/>"+
            "      <attribute key=\"Tooltip\" value=\"Playback position in seconds.\"/>"+
            "    </argument>"+
            "    <argument datatype=\"int\" name=\"duration\">"+
            "      <attribute key=\"DisplayName\" value=\"Track duration (s)\"/>"+
            "      <attribute key=\"Tooltip\" value=\"Track duration in seconds.\"/>"+
            "    </argument>"+
            "  </event>"+
            "  <method description=\"Advances to next track\" name=\"next\"/>"+
            "  <method description=\"Goes back to previous track\" name=\"previous\"/>"+
            "  "+
            "" + provideServiceRuntimeDataOverride() + "</service>";
    }

    //
    // ROUTING INFORMATION
    //
    public String provideServiceRoutingInformationOverride() {
        return "<routing><route link=\"reliable\" type=\"server\"><method name=\"playPlaylist\"/><method name=\"playTrack\"/><method name=\"play\"/><method name=\"stop\"/><method name=\"pause\"/><method name=\"next\"/><method name=\"previous\"/><event name=\"progress\"/><runtimedata/></route></routing>";
    }
}
