package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.UserProfile;
import org.minjulog.feedserver.infrastructure.repository.JpaUserProfileRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserProfileRepository {
    private final JpaUserProfileRepository jpa;

    public Optional<UserProfile> findByUserId(Long userId) { return jpa.findByUserId(userId); }
    public UserProfile saveAndFlush(UserProfile profile) { return jpa.saveAndFlush(profile); }
    public Set<String> findUsernamesByUserProfileIdIn(List<UUID> userProfileIds) { return jpa.findUsernamesByUserProfileIdIn(userProfileIds); }
    public List<JpaUserProfileRepository.UserIdNameView> findUserIdAndNameByUserIdIn(List<Long> userIds) { return jpa.findUserIdAndNameByUserIdIn(userIds); }
    public Map<Long, String> findUsernameMapByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return jpa.findUserIdAndNameByUserIdIn(new ArrayList<>(userIds))
                .stream()
                .collect(Collectors.toMap(
                        JpaUserProfileRepository.UserIdNameView::getUserId,
                        JpaUserProfileRepository.UserIdNameView::getUsername
                ));
    }
}
