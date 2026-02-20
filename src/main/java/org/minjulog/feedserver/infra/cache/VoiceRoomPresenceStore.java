package org.minjulog.feedserver.infra.cache;

import java.util.Set;

public interface VoiceRoomPresenceStore {

    void addUser(Long roomId, Long userId);

    void removeUser(Long roomId, Long userId);

    Set<Long> getOnlineUsers(Long roomId);
}
