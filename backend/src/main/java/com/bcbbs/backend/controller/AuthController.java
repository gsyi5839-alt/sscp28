package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.*;
import com.bcbbs.backend.entity.User;
import com.bcbbs.backend.security.JwtService;
import com.bcbbs.backend.service.CaptchaService;
import com.bcbbs.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        logger.info("========== Standard login request started ==========");
        logger.info("Request parameters - Username: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        logger.info("Authentication successful - User: {}, Role: {}", user.getUsername(), user.getRole());

        String token = jwtService.generateToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();

        logger.info("========== Standard login request completed ==========\n");
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Login with a required role and captcha validation.
     * Contains forced password change logic:
     * - First login: Prompt for password change
     * - Second login without change: Prompt again
     * - Third login and beyond: Continue prompting and require password change (no longer permanently disabled to avoid "cannot login and cannot change password" deadlock)
     */
    @PostMapping("/role-login")
    public ResponseEntity<ApiResponse<AuthResponse>> roleLogin(@Valid @RequestBody RoleLoginRequest request) {
        logger.info("========== Role login request started ==========");
        logger.info("Request parameters - Username: {}, Role: {}, Captcha Token: {}",
            request.getUsername(), request.getRole(), request.getCaptchaToken());

        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        logger.info("Captcha validation result: {}", captchaValid);

        if (!captchaValid) {
            logger.warn("Captcha validation failed - User: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        // First probe user status: if account is disabled by "initial password not changed" mechanism, guide to forced password change endpoint
        // Don't use AuthenticationManager here (because disabled will be directly blocked by Spring Security)
        try {
            User probe = userService.findByUsername(request.getUsername());
            logger.info("User probe - Username: {}, Enabled: {}, Role: {}, Password changed: {}, Login count: {}",
                probe.getUsername(), probe.getEnabled(), probe.getRole(),
                probe.getPasswordChanged(), probe.getLoginCountWithoutChange());

            if (Boolean.FALSE.equals(probe.getEnabled())
                    && (probe.getRole() == User.Role.MEMBER || probe.getRole() == User.Role.AGENT)
                    && Boolean.FALSE.equals(probe.getPasswordChanged())) {
                logger.warn("Account disabled - User: {}, Reason: Consecutive logins without changing initial password", probe.getUsername());
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "Account disabled: Logged in 3 times consecutively without changing initial password, please go to [Force Change Password] to unlock"));
            }
        } catch (Exception e) {
            logger.warn("User probe failed - Username: {}, Error: {}", request.getUsername(), e.getMessage());
            // Cases like user not found will be handled by subsequent authenticationManager (return unified error)
        }

        logger.info("Starting Spring Security authentication - Username: {}", request.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            logger.info("Spring Security authentication successful - Username: {}", request.getUsername());
        } catch (Exception e) {
            logger.error("Spring Security authentication failed - Username: {}, Error: {}", request.getUsername(), e.getMessage());
            throw e;
        }

        User user = (User) authentication.getPrincipal();
        logger.info("Authenticated user info - ID: {}, Username: {}, Role: {}, Email: {}",
            user.getId(), user.getUsername(), user.getRole(), user.getEmail());

        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
            logger.info("Requested role: {}, User actual role: {}", requiredRole, user.getRole());
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid role parameter: {}", request.getRole());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }

        if (user.getRole() != requiredRole) {
            logger.warn("Role mismatch - Requested role: {}, Actual role: {}", requiredRole, user.getRole());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }

        logger.info("Role verification passed - Role: {}", requiredRole);

        // Check password change status (for MEMBER and AGENT roles)
        logger.info("========== Starting password change status check ==========");
        Boolean needPasswordChange = false;

        if (user.getRole() == User.Role.MEMBER || user.getRole() == User.Role.AGENT) {
            Boolean passwordChanged = user.getPasswordChanged();
            Integer loginCount = user.getLoginCountWithoutChange();

            logger.info("[IMPORTANT] User {} login - Database original values:", user.getUsername());
            logger.info("  - passwordChanged = {}", passwordChanged);
            logger.info("  - loginCount = {}", loginCount);
            logger.info("  - User role = {}", user.getRole());

            // Handle null values
            if (passwordChanged == null) {
                logger.info("  - passwordChanged is NULL, setting to false");
                passwordChanged = false;
            }
            if (loginCount == null) {
                logger.info("  - loginCount is NULL, setting to 0");
                loginCount = 0;
            }

            logger.info("[DECISION] passwordChanged = {}, Does password need to be changed?", passwordChanged);

            // If password not changed
            if (!passwordChanged) {
                // Set need password change flag
                needPasswordChange = true;
                logger.info("✅ [KEY] User has not changed password, setting needPasswordChange = true");

                // Increment login count without password change (increment first then check)
                loginCount = loginCount + 1;
                user.setLoginCountWithoutChange(loginCount);

                logger.info("[UPDATE] Login count increased from {} to {}", (loginCount - 1), loginCount);

                // No longer disable account to avoid deadlock. Only record count and let frontend force redirect to password change page.
                userService.save(user);
                logger.info("[SAVE] User data has been updated to database");
            } else {
                needPasswordChange = false;
                logger.info("✅ User {} has already changed password (passwordChanged=true), no forced change needed", user.getUsername());
            }
        } else {
            logger.info("User role {} is not MEMBER or AGENT, skipping password check", user.getRole());
        }

        logger.info("========== Password check completed, needPasswordChange = {} ==========", needPasswordChange);

        logger.info("========== Generating response data ==========");
        String token = jwtService.generateToken(user);
        logger.info("JWT Token generated");

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(needPasswordChange)
                .loginCountWithoutChange(user.getLoginCountWithoutChange())
                .build();

        logger.info("[FINAL RESPONSE] AuthResponse built:");
        logger.info("  - username: {}", authResponse.getUsername());
        logger.info("  - role: {}", authResponse.getRole());
        logger.info("  - email: {}", authResponse.getEmail());
        logger.info("  - nickname: {}", authResponse.getNickname());
        logger.info("  - 🔴 needPasswordChange: {}", authResponse.getNeedPasswordChange());
        logger.info("  - loginCountWithoutChange: {}", authResponse.getLoginCountWithoutChange());
        logger.info("  - token: {}...", token.substring(0, Math.min(20, token.length())));

        logger.info("========== Role login request completed, returning 200 OK ==========\n");
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Force change password and unlock account (no login token required)
     * Used to solve the deadlock: "account disabled due to not changing initial password, cannot login, cannot access password change page".
     */
    @PostMapping("/force-change-password")
    public ResponseEntity<ApiResponse<AuthResponse>> forceChangePassword(
            @Valid @RequestBody ForceChangePasswordRequest request
    ) {
        logger.info("========== Force change password request started ==========");
        logger.info("Request parameters - Username: {}, Role: {}", request.getUsername(), request.getRole());

        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        logger.info("Captcha validation result: {}", captchaValid);

        if (!captchaValid) {
            logger.warn("Captcha validation failed");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        User user = userService.findByUsername(request.getUsername());
        logger.info("User lookup - Username: {}, Found: {}", request.getUsername(), (user != null));
        if (user == null) {
            // Defensive null guard for static analysis tools; findByUsername should normally throw if not found.
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "User not found"));
        }

        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
            logger.info("Role validation - Requested role: {}", requiredRole);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid role parameter: {}", request.getRole());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }

        if (user.getRole() != requiredRole) {
            logger.warn("Role mismatch - Requested: {}, Actual: {}", requiredRole, user.getRole());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }

        // Validate old password (using database hash)
        logger.info("Validating old password...");
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            logger.warn("Old password validation failed - User: {}", user.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Old password is incorrect"));
        }

        // Update password + unlock + mark as password changed
        logger.info("Old password validation passed, updating new password...");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        user.setLoginCountWithoutChange(0);
        user.setEnabled(true);
        userService.save(user);
        logger.info("Password update successful - User: {}, passwordChanged=true, loginCount=0, enabled=true",
            user.getUsername());

        String token = jwtService.generateToken(user);
        logger.info("Generated new JWT Token");

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(false)
                .loginCountWithoutChange(0)
                .build();

        logger.info("========== Force change password request completed ==========\n");
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully, account unlocked", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Username already exists"));
        }
        
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Email already exists"));
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(User.Role.USER)
                .enabled(true)
                .build();
        
        user = userService.save(Objects.requireNonNull(user, "user must not be null"));
        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Registration successful", authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        AuthResponse authResponse = AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            logger.info("Change password request - User: {}", user.getUsername());
            userService.changePassword(user, request.getOldPassword(), request.getNewPassword());
            logger.info("Password changed successfully - User: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            logger.warn("Password change failed - Invalid parameter: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Password change failed - System error: ", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Password change failed: " + e.getMessage()));
        }
    }
}

