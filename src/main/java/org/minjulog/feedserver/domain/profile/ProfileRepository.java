package org.minjulog.feedserver.domain.profile;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findProfileByUserId(long userId);
}
