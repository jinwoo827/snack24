package com.snack24.common.outboxrelay.outbox.repository;

import com.snack24.common.outboxrelay.outbox.entity.Outbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    List<Outbox> findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            Long shardKey,
            LocalDateTime createdAt,
            Pageable pageable
    );
}
