package io.jieum.gyulbackendrepository.domain.user.repository;

import io.jieum.gyulbackendrepository.domain.user.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(Long id);

    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(Long id);
    boolean existsByEmail(String email);
}
