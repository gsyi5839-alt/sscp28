package com.bcbbs.backend.repository;

import com.bcbbs.backend.entity.CaptchaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaptchaTokenRepository extends JpaRepository<CaptchaToken, String> {

    Optional<CaptchaToken> findByTokenAndUsedFalse(String token);
}

