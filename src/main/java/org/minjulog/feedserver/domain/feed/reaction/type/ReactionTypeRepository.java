package org.minjulog.feedserver.domain.feed.reaction.type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionTypeRepository extends JpaRepository<ReactionType, Long> {

    Optional<ReactionType> findByKey(String key);
}
