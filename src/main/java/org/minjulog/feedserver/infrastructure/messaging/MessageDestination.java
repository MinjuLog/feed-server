package org.minjulog.feedserver.infrastructure.messaging;

public enum MessageDestination {

    WORKSPACE_FEED("/topic/workspace.%s"),
    WORKSPACE_FEED_DELETE("/topic/workspace.%s.delete"),
    WORKSPACE_FEED_PRESENCE("/topic/workspace.%s.connect"),
    WORKSPACE_REACTION("/topic/workspace.%s.reaction"),
    WORKSPACE_LIKE("/topic/workspace.%s.like"),
    VOICE_CHANNEL_PRESENCE("/topic/voice.channel.%s"),
    VOICE_ROOM_SIGNAL("/topic/voice.room.%s.signal"),
    VOICE_ROOM_CHAT("/topic/voice.room.%s.chat"),
    APP_VOICE_ROOM_SIGNAL("/voice/rooms/{roomId}/signal");

    private final String pattern;

    MessageDestination(String pattern) {
        this.pattern = pattern;
    }

    public String destination(Object... args) {
        return pattern.formatted(args);
    }
}
