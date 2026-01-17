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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * Login with a required role and captcha validation.
     * 包含强制修改密码逻辑：
     * - 第一次登录：提示修改密码
     * - 第二次登录未修改：再次提示
     * - 第三次及以后：仍提示并强制修改密码（不再永久停用，避免“无法登录也无法改密”的死锁）
     */
    @PostMapping("/role-login")
    public ResponseEntity<ApiResponse<AuthResponse>> roleLogin(@Valid @RequestBody RoleLoginRequest request) {
        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        if (!captchaValid) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        // 先探测用户状态：如果账号已被“未改初始密码”机制停用，则引导走强制改密解锁接口
        // 这里不走 AuthenticationManager（因为 disabled 会被 Spring Security 直接拦截）
        try {
            User probe = userService.findByUsername(request.getUsername());
            if (Boolean.FALSE.equals(probe.getEnabled())
                    && (probe.getRole() == User.Role.MEMBER || probe.getRole() == User.Role.AGENT)
                    && Boolean.FALSE.equals(probe.getPasswordChanged())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "账户已停用：连续3次登录未修改初始密码，请前往【强制修改密码】解锁"));
            }
        } catch (Exception ignored) {
            // 找不到用户等情况由后续 authenticationManager 处理（返回统一错误）
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }

        if (user.getRole() != requiredRole) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }

        // 检查密码修改状态（对MEMBER和AGENT角色）
        Boolean needPasswordChange = false;
        if (user.getRole() == User.Role.MEMBER || user.getRole() == User.Role.AGENT) {
            Boolean passwordChanged = user.getPasswordChanged();
            Integer loginCount = user.getLoginCountWithoutChange();
            
            // 日志记录原始值
            logger.info("用户 {} 登录 - 原始值: passwordChanged={}, loginCount={}", 
                user.getUsername(), passwordChanged, loginCount);
            
            if (passwordChanged == null) passwordChanged = false;
            if (loginCount == null) loginCount = 0;
            
            // 如果未修改密码
            if (!passwordChanged) {
                // 设置需要修改密码标志
                needPasswordChange = true;
                
                // 增加未修改密码的登录次数（先增加再判断）
                loginCount = loginCount + 1;
                user.setLoginCountWithoutChange(loginCount);
                
                logger.info("用户 {} 需要修改密码 - needPasswordChange={}, 新loginCount={}", 
                    user.getUsername(), needPasswordChange, loginCount);
                
                // 不再停用账户，避免死锁。仅记录次数并由前端强制跳转改密页。
                userService.save(user);
            } else {
                logger.info("用户 {} 已修改过密码，无需强制修改", user.getUsername());
            }
        }

        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(needPasswordChange)
                .loginCountWithoutChange(user.getLoginCountWithoutChange())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * 强制修改密码并解锁账号（无需登录 token）
     * 用于解决“账号因未修改初始密码被停用后无法登录、无法进入改密页”的死锁。
     */
    @PostMapping("/force-change-password")
    public ResponseEntity<ApiResponse<AuthResponse>> forceChangePassword(
            @Valid @RequestBody ForceChangePasswordRequest request
    ) {
        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        if (!captchaValid) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        User user = userService.findByUsername(request.getUsername());

        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }
        if (user.getRole() != requiredRole) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }

        // 校验旧密码（用数据库中的 hash）
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "旧密码不正确"));
        }

        // 更新密码 + 解锁 + 标记已改密
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        user.setLoginCountWithoutChange(0);
        user.setEnabled(true);
        userService.save(user);

        String token = jwtService.generateToken(user);
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(false)
                .loginCountWithoutChange(0)
                .build();
        return ResponseEntity.ok(ApiResponse.success("密码修改成功，账号已解锁", authResponse));
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
        
        user = userService.save(user);
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
            userService.changePassword(user, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "密码修改失败"));
        }
    }
}

