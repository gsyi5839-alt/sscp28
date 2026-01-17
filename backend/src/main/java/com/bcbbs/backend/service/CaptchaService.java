package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.CaptchaResponse;
import com.bcbbs.backend.entity.CaptchaToken;
import com.bcbbs.backend.repository.CaptchaTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final int CAPTCHA_LENGTH = 4;
    private static final int CAPTCHA_EXPIRATION_MINUTES = 5;
    private static final String CAPTCHA_DIGITS = "0123456789";

    private final CaptchaTokenRepository captchaTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create and persist a new captcha token.
     */
    public CaptchaResponse createCaptcha() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CAPTCHA_EXPIRATION_MINUTES);

        CaptchaToken captchaToken = CaptchaToken.builder()
                .token(token)
                .code(code)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        captchaTokenRepository.save(captchaToken);

        return CaptchaResponse.builder()
                .token(token)
                .code(code)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Validate and consume a captcha token.
     */
    public boolean validateCaptcha(String token, String code) {
        return captchaTokenRepository.findByTokenAndUsedFalse(token)
                .filter(captcha -> captcha.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(captcha -> captcha.getCode().equals(code))
                .map(captcha -> {
                    captcha.setUsed(true);
                    captchaTokenRepository.save(captcha);
                    return true;
                })
                .orElse(false);
    }

    private String generateCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = secureRandom.nextInt(CAPTCHA_DIGITS.length());
            builder.append(CAPTCHA_DIGITS.charAt(index));
        }
        return builder.toString();
    }
}

