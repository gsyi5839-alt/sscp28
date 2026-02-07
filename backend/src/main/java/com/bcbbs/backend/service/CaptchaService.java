package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.CaptchaResponse;
import com.bcbbs.backend.entity.CaptchaToken;
import com.bcbbs.backend.repository.CaptchaTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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
    @SuppressWarnings("null")
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
                .image(buildCaptchaImage(code))
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

    /**
     * Build a simple SVG captcha image encoded as a data URI.
     */
    private String buildCaptchaImage(String code) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='90' height='28'>");
        svg.append("<rect width='100%' height='100%' fill='#f5f5f5'/>");
        svg.append("<line x1='5' y1='6' x2='85' y2='6' stroke='#d0d0d0'/>");
        svg.append("<line x1='5' y1='22' x2='85' y2='22' stroke='#d0d0d0'/>");
        svg.append("<text x='10' y='20' font-size='18' font-family='Arial' fill='#333'>");
        svg.append(code);
        svg.append("</text>");
        svg.append("</svg>");

        String base64 = Base64.getEncoder().encodeToString(svg.toString().getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }
}

