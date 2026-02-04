package com.example.meezy.bc.user.user.domain.repository;

import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UserId> {
    Optional<User> findByEmailAndOauthProvider(String email, OauthProvider oauthProvider);
    Optional<User> findByAccountId(String accountId);
    Optional<User> findByUserId_Value(UUID userId);
    Optional<User> findByUserId(UserId userId);
    List<User> findByUserId_ValueIn(List<UUID> userIds);

    boolean existsByAccountId(String accountId);
    boolean existsByEmailAndOauthProvider(String email, OauthProvider oauthProvider);
}
