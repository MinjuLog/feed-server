package org.minjulog.feedserver.infrastructure.bootstrap;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.UserProfile;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.infrastructure.repository.JpaUserProfileRepository;
import org.minjulog.feedserver.infrastructure.repository.JpaWorkspaceRepository;
import org.minjulog.feedserver.domain.model.VoiceChannel;
import org.minjulog.feedserver.domain.model.VoiceRoom;
import org.minjulog.feedserver.infrastructure.repository.JpaVoiceChannelRepository;
import org.minjulog.feedserver.infrastructure.repository.JpaVoiceRoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VoiceRoomBootstrap implements ApplicationRunner {

    private static final Long SYSTEM_USER_ID = 0L;
    private static final String DEFAULT_CHANNEL_TITLE = "기본 음성채널";
    private static final int DEFAULT_ROOM_COUNT = 4;

    private final JpaWorkspaceRepository workspaceRepository;
    private final JpaUserProfileRepository userProfileRepository;
    private final JpaVoiceChannelRepository voiceChannelRepository;
    private final JpaVoiceRoomRepository voiceRoomRepository;

    @Value("${env.VOICE.WORKSPACE_ID:1}")
    private Long defaultWorkspaceId;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Workspace workspace = workspaceRepository.findById(defaultWorkspaceId)
                .orElseGet(() -> workspaceRepository.saveAndFlush(
                        Workspace.builder()
                                .likeCount(0L)
                                .build()
                ));

        UserProfile systemUser = userProfileRepository.findByUserId(SYSTEM_USER_ID)
                .orElseGet(() -> userProfileRepository.saveAndFlush(
                        UserProfile.builder()
                                .userId(SYSTEM_USER_ID)
                                .username("system")
                                .build()
                ));

        VoiceChannel channel = voiceChannelRepository
                .findByWorkspaceIdAndTitle(workspace.getId(), DEFAULT_CHANNEL_TITLE)
                .orElseGet(() -> voiceChannelRepository.saveAndFlush(
                        VoiceChannel.builder()
                                .workspace(workspace)
                                .createdByUserProfile(systemUser)
                                .title(DEFAULT_CHANNEL_TITLE)
                                .active(true)
                                .build()
                ));

        for (int i = 1; i <= DEFAULT_ROOM_COUNT; i++) {
            String title = "음성채팅방" + i;
            if (voiceRoomRepository.existsByChannelIdAndTitle(channel.getId(), title)) {
                continue;
            }
            voiceRoomRepository.save(
                    VoiceRoom.builder()
                            .channel(channel)
                            .createdByUserProfile(systemUser)
                            .title(title)
                            .active(true)
                            .build()
            );
        }
    }
}
