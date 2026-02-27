package org.minjulog.feedserver.infrastructure.cache;

import org.minjulog.feedserver.application.voice.VoiceTransportMode;

public interface VoiceRoomTransportModeStore {

    VoiceTransportMode getMode(Long roomId);

    void putMode(Long roomId, VoiceTransportMode mode);

    void clear(Long roomId);
}
