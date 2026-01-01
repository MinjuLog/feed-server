package org.minjulog.feedserver.application;
import java.util.Optional;
import java.util.Set;

public interface PresenceStore {

    void onConnected(String sessionId, String userId);

    void onDisconnected(String sessionId);

    void onSubscribe(String sessionId, String destination);

    void onUnsubscribe(String sessionId, String destination); // destination 모를 수 있으면 호출 안 해도 됨

    Set<String> getOnlineUsers(); // 운영에선 비싸면 비활성화하거나 별도 메트릭으로
    boolean isUserOnline(String userId);

    Set<String> getUserSessions(String userId);

    int getDestinationSubscriberCount(String destination);

    Optional<String> findUserIdBySessionId(String sessionId);
}
