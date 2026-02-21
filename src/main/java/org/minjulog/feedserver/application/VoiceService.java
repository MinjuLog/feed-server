package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.minjulog.feedserver.domain.feed.repository.UserProfileRepository;
import org.minjulog.feedserver.domain.voice.model.VoiceRoom;
import org.minjulog.feedserver.domain.voice.repository.VoiceRoomRepository;
import org.minjulog.feedserver.infra.cache.VoiceRoomPresenceStore;
import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceService {

    private final VoiceRoomRepository voiceRoomRepository;
    private final VoiceRoomPresenceStore voiceRoomPresenceStore;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public List<VoiceRoomDto.RoomResponse> findRooms(Long channelId) {
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

        Map<Long, String> usernameByUserId = allUserIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findUserIdAndNameByUserIdIn(new ArrayList<>(allUserIds))
                .stream()
                .collect(Collectors.toMap(
                        UserProfileRepository.UserIdNameView::getUserId,
                        UserProfileRepository.UserIdNameView::getUsername
                ));

        return rooms.stream()
                .map(r -> {
                    List<VoiceRoomDto.UserResponse> onlineUsers = onlineUserIdsByRoom
                            .getOrDefault(r.getId(), Set.of())
                            .stream()
                            .map(userId -> new VoiceRoomDto.UserResponse(
                                    userId,
                                    usernameByUserId.get(userId)
                            ))
                            .toList();

                    return new VoiceRoomDto.RoomResponse(
                            r.getId(),
                            r.getTitle(),
                            r.isActive(),
                            r.getCreatedAt().toString(),
                            onlineUsers
                    );
                })
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
    public VoiceRoom getRoom(Long roomId) {
        return voiceRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("room not found"));
    }

    @Transactional(readOnly = true)
    public List<VoiceRoomDto.UserResponse> getOnlineUsers(Long roomId) {
        Set<Long> userIds = voiceRoomPresenceStore.getOnlineUsers(roomId);
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> usernameByUserId =
                userProfileRepository.findUserIdAndNameByUserIdIn(new ArrayList<>(userIds))
                        .stream()
                        .collect(Collectors.toMap(
                                UserProfileRepository.UserIdNameView::getUserId,
                                UserProfileRepository.UserIdNameView::getUsername
                        ));

        return userIds.stream()
                .map(userId -> new VoiceRoomDto.UserResponse(
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
}
