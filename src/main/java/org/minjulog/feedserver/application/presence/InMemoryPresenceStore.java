package org.minjulog.feedserver.application.presence;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryPresenceStore implements PresenceStore {

    // sessionId -> userId
    private final ConcurrentMap<String, String> sessionToUser = new ConcurrentHashMap<>();

    // userId -> sessionId set
    private final ConcurrentMap<String, Set<String>> userToSessions = new ConcurrentHashMap<>();

    // destination -> sessionId set
    private final ConcurrentMap<String, Set<String>> destToSessions = new ConcurrentHashMap<>();

    // sessionId -> destination set (disconnect 정리용)
    private final ConcurrentMap<String, Set<String>> sessionToDests = new ConcurrentHashMap<>();

    @Override
    public void onConnected(String sessionId, String userId) {
        sessionToUser.put(sessionId, userId);
        userToSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    @Override
    public void onDisconnected(String sessionId) {
        String userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userToSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) userToSessions.remove(userId);
            }
        }

        Set<String> dests = sessionToDests.remove(sessionId);
        if (dests != null) {
            for (String dest : dests) {
                Set<String> sessions = destToSessions.get(dest);
                if (sessions != null) {
                    sessions.remove(sessionId);
                    if (sessions.isEmpty()) destToSessions.remove(dest);
                }
            }
        }
    }

    @Override
    public void onSubscribe(String sessionId, String destination) {
        if (destination == null) return;
        destToSessions.computeIfAbsent(destination, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToDests.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(destination);
    }

    @Override
    public void onUnsubscribe(String sessionId, String destination) {
        if (destination == null) return;
        Set<String> sessions = destToSessions.get(destination);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) destToSessions.remove(destination);
        }
        Set<String> dests = sessionToDests.get(sessionId);
        if (dests != null) {
            dests.remove(destination);
            if (dests.isEmpty()) sessionToDests.remove(sessionId);
        }
    }

    @Override
    public Set<String> getOnlineUsers() {
        return Set.copyOf(userToSessions.keySet());
    }

    @Override
    public boolean isUserOnline(String userId) {
        Set<String> s = userToSessions.get(userId);
        return s != null && !s.isEmpty();
    }

    @Override
    public Set<String> getUserSessions(String userId) {
        Set<String> s = userToSessions.get(userId);
        return s == null ? Set.of() : Set.copyOf(s);
    }

    @Override
    public int getDestinationSubscriberCount(String destination) {
        Set<String> s = destToSessions.get(destination);
        return s == null ? 0 : s.size();
    }

    @Override
    public Optional<String> findUserIdBySessionId(String sessionId) {
        return Optional.ofNullable(sessionToUser.get(sessionId));
    }
}
