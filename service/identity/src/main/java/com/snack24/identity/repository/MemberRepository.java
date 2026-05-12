package com.snack24.identity.repository;

import com.snack24.identity.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>,
                                            MemberSearchRepository {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
