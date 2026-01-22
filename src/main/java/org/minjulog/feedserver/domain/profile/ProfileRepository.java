package org.minjulog.feedserver.domain.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findProfileByUserId(long userId);

    @Query("""
                select p.username
                from Profile p
                where p.profileId in :profileIds
            """)
    Set<String> findUsernamesByProfileIdIn(@Param("profileIds") List<Long> profileIds);
}
