package org.minjulog.feedserver.domain.feed.repository;

import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(Long userId);

    @Query("""
                select p.username
                from UserProfile p
                where p.id in :userProfileIds
            """)
    Set<String> findUsernamesByUserProfileIdIn(@Param("userProfileIds") List<UUID> userProfileIds);

    @Query("""
                select p.userId as userId, p.username as username
                from UserProfile p
                where p.userId in :userIds
            """)
    List<UserIdNameView> findUserIdAndNameByUserIdIn(@Param("userIds") List<Long> userIds);

    interface UserIdNameView {
        Long getUserId();

        String getUsername();
    }
}
