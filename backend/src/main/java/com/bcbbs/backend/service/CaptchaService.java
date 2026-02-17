package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.CaptchaResponse;
import com.bcbbs.backend.entity.CaptchaToken;
import com.bcbbs.backend.repository.CaptchaTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
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
    // Optional background image used inside the captcha SVG.
    // Put file at: backend/src/main/resources/captcha/captcha-bg.png
    private static final String CAPTCHA_BG_RESOURCE = "/captcha/captcha-bg.png";

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
     * Build captcha image encoded as a data URI.
     *
     * Note: the provided background `captcha-bg.png` already contains digits.
     * We treat it as a template image: erase the digits area and render the new code.
     */
    private String buildCaptchaImage(String code) {
        byte[] png = buildCaptchaPng(code);
        if (png == null) {
            // Very small fallback: keep previous behavior of returning something usable.
            String base64 = Base64.getEncoder().encodeToString(("captcha:" + code).getBytes(StandardCharsets.UTF_8));
            return "data:text/plain;base64," + base64;
        }
        String base64 = Base64.getEncoder().encodeToString(png);
        return "data:image/png;base64," + base64;
    }

    /**
     * Load captcha background PNG from classpath and return as a data URI.
     * Returns null when the resource is missing/unreadable.
     */
    private String loadCaptchaBackgroundDataUri() {
        try (InputStream in = CaptchaService.class.getResourceAsStream(CAPTCHA_BG_RESOURCE)) {
            if (in == null) {
                return null;
            }
            byte[] bytes = in.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/png;base64," + base64;
        } catch (IOException ex) {
            return null;
        }
    }

    private byte[] buildCaptchaPng(String code) {
        // Match frontend/design exact rendered size: 85x27.
        final int width = 85;
        final int height = 27;

        try {
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Draw template background if available.
                BufferedImage bg = loadBackgroundPng();
                if (bg != null) {
                    g.drawImage(bg, 0, 0, width, height, null);
                } else {
                    g.setColor(new Color(245, 245, 245));
                    g.fillRect(0, 0, width, height);
                }

                // Background image is a clean plate (no digits).
                // Add a small translucent pad only under the digits to improve contrast.
                int padX = 5;
                int padY = 3;
                int padW = 75;
                int padH = 21;
                g.setComposite(AlphaComposite.SrcOver.derive(0.28f));
                g.setColor(Color.WHITE);
                g.fillRoundRect(padX, padY, padW, padH, 4, 4);
                g.setComposite(AlphaComposite.SrcOver);

                // Add a bit of noise lines over the erased area so it still looks like captcha.
                g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
                for (int i = 0; i < 6; i++) {
                    g.setColor(new Color(secureRandom.nextInt(256), secureRandom.nextInt(256), secureRandom.nextInt(256)));
                int x1 = padX + secureRandom.nextInt(padW);
                int y1 = padY + secureRandom.nextInt(padH);
                int x2 = padX + secureRandom.nextInt(padW);
                int y2 = padY + secureRandom.nextInt(padH);
                    g.drawLine(x1, y1, x2, y2);
                }
                g.setComposite(AlphaComposite.SrcOver);

                // Draw code digits using fixed boxes to match design:
                // digit box: 16x16, gap: 6px.
                Font font = new Font("Arial", Font.BOLD, 16);
                g.setFont(font);
                final int digitBox = 16;
                final int digitGap = 6;
                final int digitCount = Math.min(code.length(), CAPTCHA_LENGTH);
                final int totalW = (digitCount * digitBox) + ((digitCount - 1) * digitGap);
                final int startX = Math.max(0, (width - totalW) / 2);
                final int startY = 6; // box top

                // Recompute pad to align with digit area (keep subtle).
                // (We don't move the background, only the overlay.)
                // Note: keep within image bounds.
                //noinspection SuspiciousNameCombination
                // (no-op; only for IDE hints)

                for (int i = 0; i < digitCount; i++) {
                    char ch = code.charAt(i);
                    int boxX = startX + i * (digitBox + digitGap);
                    int boxY = startY;

                    // Baseline roughly in the lower part of a 16px tall box.
                    int x = boxX + 2;
                    int y = boxY + 13;
                    double angle = (secureRandom.nextDouble() - 0.5) * 0.14; // keep small: improves readability

                    // Render with outline so digits stay readable on noisy background.
                    // Outline is white, fill is a darker random color.
                    Color fill = new Color(30 + secureRandom.nextInt(120), 30 + secureRandom.nextInt(120), 30 + secureRandom.nextInt(120));
                    FontRenderContext frc = g.getFontRenderContext();
                    GlyphVector gv = font.createGlyphVector(frc, new char[] { ch });
                    Shape glyph = gv.getOutline(x, y);

                    AffineTransform oldTx = g.getTransform();
                    g.rotate(angle, x, y);

                    g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(Color.WHITE);
                    g.draw(glyph);

                    g.setColor(fill);
                    g.fill(glyph);

                    g.setTransform(oldTx);
                }
            } finally {
                g.dispose();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(canvas, "png", out);
            return out.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage loadBackgroundPng() {
        try (InputStream in = CaptchaService.class.getResourceAsStream(CAPTCHA_BG_RESOURCE)) {
            if (in == null) {
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException ex) {
            return null;
        }
    }
}

