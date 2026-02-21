package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.minjulog.feedserver.domain.feed.repository.UserProfileRepository;
import org.minjulog.feedserver.domain.voice.model.VoiceRoom;
import org.minjulog.feedserver.domain.voice.model.VoiceRoomMessage;
import org.minjulog.feedserver.domain.voice.repository.VoiceRoomMessageRepository;
import org.minjulog.feedserver.domain.voice.repository.VoiceRoomRepository;
import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomMessageDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoiceChatService {

    private final VoiceRoomRepository voiceRoomRepository;
    private final VoiceRoomMessageRepository voiceRoomMessageRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public VoiceRoomMessageDto.Response createMessage(Long roomId, Long userId, VoiceRoomMessageDto.Request request) {
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

        return new VoiceRoomMessageDto.Response(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getSenderId(),
                saved.getSenderName(),
                saved.getContent(),
                saved.getCreatedAt().toString()
        );
    }

    @Transactional(readOnly = true)
    public List<VoiceRoomMessageDto.Response> findMessages(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId is required");
        }
        return voiceRoomMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(m -> new VoiceRoomMessageDto.Response(
                        m.getId(),
                        m.getRoom().getId(),
                        m.getSenderId(),
                        m.getSenderName(),
                        m.getContent(),
                        m.getCreatedAt().toString()
                ))
                .toList();
    }

    private UserProfile getOrCreateUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> userProfileRepository.saveAndFlush(new UserProfile(userId)));
    }
}
