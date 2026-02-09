package com.boardly.data.repository;

import com.boardly.commmon.enums.TokenType;
import com.boardly.data.model.sql.authentication.SecureToken;
import com.boardly.data.model.sql.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SecureTokenRepository extends JpaRepository<SecureToken, UUID> {
    Optional<SecureToken> findByToken(String token);

    void deleteAllByUserAndTokenType(User user, TokenType tokenType);
}
