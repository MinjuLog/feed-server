package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.UserProfile;
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
}
