package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionTypeRepository extends JpaRepository<ReactionType, Long> {

    Optional<ReactionType> findByKey(String key);
}
