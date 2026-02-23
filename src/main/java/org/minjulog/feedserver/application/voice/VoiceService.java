package org.minjulog.feedserver.application.voice;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.UserProfile;
import org.minjulog.feedserver.domain.model.VoiceRoom;
import org.minjulog.feedserver.domain.model.VoiceRoomMessage;
import org.minjulog.feedserver.infrastructure.cache.VoiceRoomPresenceStore;
import org.minjulog.feedserver.infrastructure.configuration.LiveKitProperties;
import org.minjulog.feedserver.domain.repository.UserProfileRepository;
import org.minjulog.feedserver.domain.repository.VoiceRoomMessageRepository;
import org.minjulog.feedserver.domain.repository.VoiceRoomRepository;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceService {

    private final VoiceRoomRepository voiceRoomRepository;
    private final VoiceRoomMessageRepository voiceRoomMessageRepository;
    private final VoiceRoomPresenceStore voiceRoomPresenceStore;
    private final UserProfileRepository userProfileRepository;
    private final LiveKitProperties properties;

    public VoiceResponse.IssueToken issueToken(Long userId, VoiceRequest.IssueToken request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request == null || !StringUtils.hasText(request.roomName())) {
            throw new IllegalArgumentException("roomName is required");
        }
        if (!StringUtils.hasText(properties.getApiKey()) || !StringUtils.hasText(properties.getApiSecret())) {
            throw new IllegalStateException("LiveKit API key/secret is not configured");
        }

        String identity = userId.toString();
        String participantName = StringUtils.hasText(request.participantName())
                ? request.participantName()
                : "user-" + userId;

        AccessToken token = new AccessToken(properties.getApiKey(), properties.getApiSecret());
        token.setIdentity(identity);
        token.setName(participantName);
        token.addGrants(new RoomJoin(true), new RoomName(request.roomName()));

        return new VoiceResponse.IssueToken(
                token.toJwt(),
                request.roomName(),
                identity,
                participantName
        );
    }

    @Transactional(readOnly = true)
    public List<VoiceResponse.ReadRoom> findRooms(Long channelId) {
        List<VoiceRoom> rooms =
                voiceRoomRepository.findByChannelIdOrderByCreatedAtAsc(channelId);

        if (rooms.isEmpty()) {
            return List.of();
        }

        Map<Long, Set<Long>> onlineUserIdsByRoom = rooms.stream()
                .collect(Collectors.toMap(
                        VoiceRoom::getId,
                        r -> voiceRoomPresenceStore.getOnlineUsers(r.getId())
                ));

        Set<Long> allUserIds = onlineUserIdsByRoom.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<Long, String> usernameByUserId = userProfileRepository.findUsernameMapByUserIds(allUserIds);

        return rooms.stream()
                .map(r -> {
                    List<VoiceResponse.ReadUser> onlineUsers = onlineUserIdsByRoom
                            .getOrDefault(r.getId(), Set.of())
                            .stream()
                            .map(userId -> new VoiceResponse.ReadUser(
                                    userId,
                                    usernameByUserId.get(userId)
                            ))
                            .toList();

                    return new VoiceResponse.ReadRoom(
                            r.getId(),
                            r.getTitle(),
                            r.isActive(),
                            r.getCreatedAt().toString(),
                            onlineUsers
                    );
                })
                .toList();
    }

    @Transactional
    public VoiceResponse.ReadMessage createMessage(Long roomId, Long userId, VoiceRequest.CreateMessage request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("roomId is required");
        }
        if (request == null || !StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("content is required");
        }

        VoiceRoom room = voiceRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        UserProfile sender = getOrCreateUserProfile(userId);

        VoiceRoomMessage message = VoiceRoomMessage.builder()
                .room(room)
                .senderUserProfile(sender)
                .content(request.content())
                .build();

        VoiceRoomMessage saved = voiceRoomMessageRepository.save(message);

        return new VoiceResponse.ReadMessage(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getSenderId(),
                saved.getSenderName(),
                saved.getContent(),
                saved.getCreatedAt().toString()
        );
    }

    @Transactional(readOnly = true)
    public List<VoiceResponse.ReadMessage> findMessages(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId is required");
        }
        return voiceRoomMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(m -> new VoiceResponse.ReadMessage(
                        m.getId(),
                        m.getRoom().getId(),
                        m.getSenderId(),
                        m.getSenderName(),
                        m.getContent(),
                        m.getCreatedAt().toString()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public void joinRoom(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            throw new IllegalArgumentException("roomId and userId are required");
        }
        voiceRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
        voiceRoomPresenceStore.addUser(roomId, userId);
    }

    @Transactional(readOnly = true)
    public void leaveRoom(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            throw new IllegalArgumentException("roomId and userId are required");
        }
        voiceRoomPresenceStore.removeUser(roomId, userId);
    }

    @Transactional(readOnly = true)
    public List<DisconnectRoomPresence> leaveAllRoomsOnDisconnect(Long userId) {
        if (userId == null) {
            return List.of();
        }

        Set<Long> roomIds = voiceRoomPresenceStore.removeUserFromAllRooms(userId);
        if (roomIds.isEmpty()) {
            return List.of();
        }

        return roomIds.stream()
                .map(roomId -> {
                    VoiceRoom room = voiceRoomRepository.findById(roomId)
                            .orElse(null);
                    if (room == null) {
                        return null;
                    }
                    return new DisconnectRoomPresence(
                            room.getChannel().getId(),
                            roomId,
                            getOnlineUsers(roomId)
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisconnectRoomPresence> getDisconnectRoomPresences(Set<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }

        return roomIds.stream()
                .map(roomId -> {
                    VoiceRoom room = voiceRoomRepository.findById(roomId)
                            .orElse(null);
                    if (room == null) {
                        return null;
                    }
                    return new DisconnectRoomPresence(
                            room.getChannel().getId(),
                            roomId,
                            getOnlineUsers(roomId)
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoiceRoom getRoom(Long roomId) {
        return voiceRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
    }

    @Transactional(readOnly = true)
    public List<VoiceResponse.ReadUser> getOnlineUsers(Long roomId) {
        Set<Long> userIds = voiceRoomPresenceStore.getOnlineUsers(roomId);
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> usernameByUserId = userProfileRepository.findUsernameMapByUserIds(userIds);

        return userIds.stream()
                .map(userId -> new VoiceResponse.ReadUser(
                        userId,
                        usernameByUserId.get(userId)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public String getUsername(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getUsername)
                .orElse(null);
    }

    private UserProfile getOrCreateUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> userProfileRepository.saveAndFlush(new UserProfile(userId)));
    }

    public record DisconnectRoomPresence(
            Long channelId,
            Long roomId,
            List<VoiceResponse.ReadUser> onlineUsers
    ) {}
}
