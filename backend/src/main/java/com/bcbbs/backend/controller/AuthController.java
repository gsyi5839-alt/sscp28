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
        logger.info("========== 普通登录请求开始 ==========");
        logger.info("请求参数 - 用户名: {}", request.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        logger.info("认证成功 - 用户: {}, 角色: {}", user.getUsername(), user.getRole());
        
        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        logger.info("========== 普通登录请求完成 ==========\n");
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
        logger.info("========== 角色登录请求开始 ==========");
        logger.info("请求参数 - 用户名: {}, 角色: {}, 验证码Token: {}", 
            request.getUsername(), request.getRole(), request.getCaptchaToken());
        
        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        logger.info("验证码验证结果: {}", captchaValid);
        
        if (!captchaValid) {
            logger.warn("验证码验证失败 - 用户: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        // 先探测用户状态：如果账号已被"未改初始密码"机制停用，则引导走强制改密解锁接口
        // 这里不走 AuthenticationManager（因为 disabled 会被 Spring Security 直接拦截）
        try {
            User probe = userService.findByUsername(request.getUsername());
            logger.info("用户探测 - 用户名: {}, 是否启用: {}, 角色: {}, 密码已修改: {}, 登录次数: {}", 
                probe.getUsername(), probe.getEnabled(), probe.getRole(), 
                probe.getPasswordChanged(), probe.getLoginCountWithoutChange());
            
            if (Boolean.FALSE.equals(probe.getEnabled())
                    && (probe.getRole() == User.Role.MEMBER || probe.getRole() == User.Role.AGENT)
                    && Boolean.FALSE.equals(probe.getPasswordChanged())) {
                logger.warn("账户已停用 - 用户: {}, 原因: 连续登录未修改初始密码", probe.getUsername());
                return ResponseEntity.status(403)
                        .body(ApiResponse.error(403, "账户已停用：连续3次登录未修改初始密码，请前往【强制修改密码】解锁"));
            }
        } catch (Exception e) {
            logger.warn("用户探测失败 - 用户名: {}, 错误: {}", request.getUsername(), e.getMessage());
            // 找不到用户等情况由后续 authenticationManager 处理（返回统一错误）
        }

        logger.info("开始Spring Security认证 - 用户名: {}", request.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            logger.info("Spring Security认证成功 - 用户名: {}", request.getUsername());
        } catch (Exception e) {
            logger.error("Spring Security认证失败 - 用户名: {}, 错误: {}", request.getUsername(), e.getMessage());
            throw e;
        }

        User user = (User) authentication.getPrincipal();
        logger.info("认证用户信息 - ID: {}, 用户名: {}, 角色: {}, 邮箱: {}", 
            user.getId(), user.getUsername(), user.getRole(), user.getEmail());
        
        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
            logger.info("请求的角色: {}, 用户实际角色: {}", requiredRole, user.getRole());
        } catch (IllegalArgumentException ex) {
            logger.error("无效的角色参数: {}", request.getRole());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }

        if (user.getRole() != requiredRole) {
            logger.warn("角色不匹配 - 请求角色: {}, 实际角色: {}", requiredRole, user.getRole());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }
        
        logger.info("角色验证通过 - 角色: {}", requiredRole);

        // 检查密码修改状态（对MEMBER和AGENT角色）
        logger.info("========== 开始检查密码修改状态 ==========");
        Boolean needPasswordChange = false;
        
        if (user.getRole() == User.Role.MEMBER || user.getRole() == User.Role.AGENT) {
            Boolean passwordChanged = user.getPasswordChanged();
            Integer loginCount = user.getLoginCountWithoutChange();
            
            logger.info("【重要】用户 {} 登录 - 数据库原始值:", user.getUsername());
            logger.info("  - passwordChanged = {}", passwordChanged);
            logger.info("  - loginCount = {}", loginCount);
            logger.info("  - 用户角色 = {}", user.getRole());
            
            // 处理null值
            if (passwordChanged == null) {
                logger.info("  - passwordChanged 为 NULL，设置为 false");
                passwordChanged = false;
            }
            if (loginCount == null) {
                logger.info("  - loginCount 为 NULL，设置为 0");
                loginCount = 0;
            }
            
            logger.info("【判断】passwordChanged = {}, 是否需要修改密码？", passwordChanged);
            
            // 如果未修改密码
            if (!passwordChanged) {
                // 设置需要修改密码标志
                needPasswordChange = true;
                logger.info("✅ 【关键】用户未修改过密码，设置 needPasswordChange = true");
                
                // 增加未修改密码的登录次数（先增加再判断）
                loginCount = loginCount + 1;
                user.setLoginCountWithoutChange(loginCount);
                
                logger.info("【更新】登录次数从 {} 增加到 {}", (loginCount - 1), loginCount);
                
                // 不再停用账户，避免死锁。仅记录次数并由前端强制跳转改密页。
                userService.save(user);
                logger.info("【保存】用户数据已更新到数据库");
            } else {
                needPasswordChange = false;
                logger.info("✅ 用户 {} 已修改过密码 (passwordChanged=true)，无需强制修改", user.getUsername());
            }
        } else {
            logger.info("用户角色 {} 不是 MEMBER 或 AGENT，跳过密码检查", user.getRole());
        }
        
        logger.info("========== 密码检查完成，needPasswordChange = {} ==========", needPasswordChange);

        logger.info("========== 生成响应数据 ==========");
        String token = jwtService.generateToken(user);
        logger.info("JWT Token 已生成");
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(needPasswordChange)
                .loginCountWithoutChange(user.getLoginCountWithoutChange())
                .build();
        
        logger.info("【最终响应】AuthResponse 构建完成:");
        logger.info("  - username: {}", authResponse.getUsername());
        logger.info("  - role: {}", authResponse.getRole());
        logger.info("  - email: {}", authResponse.getEmail());
        logger.info("  - nickname: {}", authResponse.getNickname());
        logger.info("  - 🔴 needPasswordChange: {}", authResponse.getNeedPasswordChange());
        logger.info("  - loginCountWithoutChange: {}", authResponse.getLoginCountWithoutChange());
        logger.info("  - token: {}...", token.substring(0, Math.min(20, token.length())));
        
        logger.info("========== 角色登录请求完成，返回 200 OK ==========\n");
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * 强制修改密码并解锁账号（无需登录 token）
     * 用于解决"账号因未修改初始密码被停用后无法登录、无法进入改密页"的死锁。
     */
    @PostMapping("/force-change-password")
    public ResponseEntity<ApiResponse<AuthResponse>> forceChangePassword(
            @Valid @RequestBody ForceChangePasswordRequest request
    ) {
        logger.info("========== 强制修改密码请求开始 ==========");
        logger.info("请求参数 - 用户名: {}, 角色: {}", request.getUsername(), request.getRole());
        
        boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaToken(), request.getCaptchaCode());
        logger.info("验证码验证结果: {}", captchaValid);
        
        if (!captchaValid) {
            logger.warn("验证码验证失败");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid captcha"));
        }

        User user = userService.findByUsername(request.getUsername());
        logger.info("查找用户 - 用户名: {}, 找到: {}", request.getUsername(), (user != null));

        User.Role requiredRole;
        try {
            requiredRole = User.Role.valueOf(request.getRole().toUpperCase());
            logger.info("角色验证 - 请求角色: {}", requiredRole);
        } catch (IllegalArgumentException ex) {
            logger.error("无效的角色参数: {}", request.getRole());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid role"));
        }
        
        if (user.getRole() != requiredRole) {
            logger.warn("角色不匹配 - 请求: {}, 实际: {}", requiredRole, user.getRole());
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(403, "Role mismatch"));
        }

        // 校验旧密码（用数据库中的 hash）
        logger.info("验证旧密码...");
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            logger.warn("旧密码验证失败 - 用户: {}", user.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "旧密码不正确"));
        }

        // 更新密码 + 解锁 + 标记已改密
        logger.info("旧密码验证通过，更新新密码...");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChanged(true);
        user.setLoginCountWithoutChange(0);
        user.setEnabled(true);
        userService.save(user);
        logger.info("密码更新成功 - 用户: {}, passwordChanged=true, loginCount=0, enabled=true", 
            user.getUsername());

        String token = jwtService.generateToken(user);
        logger.info("生成新的JWT Token");
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .needPasswordChange(false)
                .loginCountWithoutChange(0)
                .build();
        
        logger.info("========== 强制修改密码请求完成 ==========\n");
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

