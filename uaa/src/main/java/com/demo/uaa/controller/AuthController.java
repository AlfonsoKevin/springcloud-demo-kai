package com.demo.uaa.controller;

import com.demo.uaa.dto.TokenRequest;
import com.demo.uaa.entity.User;
import com.demo.uaa.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author TangZhikai
 */
@RestController
@RequestMapping("/uaa")
public class AuthController {

    @Resource
    private UserRepository userRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private LdapTemplate ldapTemplate;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody TokenRequest tokenRequest) {

        if (!"password".equals(tokenRequest.getGrantType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported grantType");
        }

        boolean isAuthenticated = false;
        User user = null;

        // 1. 根据 loginType 判断走 DB 还是 LDAP
        if ("ldap".equalsIgnoreCase(tokenRequest.getLoginType())) {
            if (ldapTemplate == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("LDAP is not configured");
            }
            try {
                // 尝试在 ou=users 下查询，如果抛出 NameNotFoundException 说明 OU 不存在
                boolean authResult = false;
                try {
                    authResult = ldapTemplate.authenticate(
                            "ou=users",
                            "(cn=" + tokenRequest.getUsername() + ")",
                            tokenRequest.getPassword()
                    );

                    if (!authResult) {
                        authResult = ldapTemplate.authenticate(
                                "ou=users",
                                "(uid=" + tokenRequest.getUsername() + ")",
                                tokenRequest.getPassword()
                        );
                    }
                } catch (org.springframework.ldap.NameNotFoundException e) {
                    // 如果 ou=users 不存在，说明初始化可能没成功或者被删除了
                    // 回退到在根目录下 (base="") 搜索，这样可以兼容不同的 LDAP 结构
                    System.out.println("OU 'users' not found, falling back to root base search.");
                    authResult = ldapTemplate.authenticate(
                            "",
                            "(cn=" + tokenRequest.getUsername() + ")",
                            tokenRequest.getPassword()
                    );
                    if (!authResult) {
                        authResult = ldapTemplate.authenticate(
                                "",
                                "(uid=" + tokenRequest.getUsername() + ")",
                                tokenRequest.getPassword()
                        );
                    }
                }

                if (authResult) {
                    // LDAP 验证成功后，从本地数据库加载该用户的角色信息 (或者如果 DB 中没有，赋予默认角色)
                    Optional<User> userOpt = userRepository.findByUsername(tokenRequest.getUsername());
                    if (userOpt.isPresent()) {
                        user = userOpt.get();
                    } else {
                        // 如果 LDAP 中有，但本地没同步过，自动注册一个并赋予基础角色
                        user = new User(tokenRequest.getUsername(), "", tokenRequest.getUsername() + "@example.com", "USER");
                        userRepository.save(user);
                    }
                    isAuthenticated = true;
                } else {
                    isAuthenticated = false;
                }
            } catch (Exception e) {
                // 打印出具体的 LDAP 异常以便调试，后面改成slf4j日志
                e.printStackTrace();
                isAuthenticated = false;
            }
        } else {
            // 2. 默认走 DB 验证
            Optional<User> userOpt = userRepository.findByUsername(tokenRequest.getUsername());
            if (userOpt.isPresent() && passwordEncoder.matches(tokenRequest.getPassword(), userOpt.get().getPassword())) {
                user = userOpt.get();
                isAuthenticated = true;
            }
        }

        // 3. 如果认证成功，签发 JWT
        if (isAuthenticated && user != null) {
            String token = Jwts.builder()
                    .setSubject(user.getUsername())
                    .claim("roles", user.getRoles())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                    .compact();

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", token);
            response.put("token_type", "bearer");
            response.put("expires_in", expiration / 1000);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    /**
     * 用户注册，暂时不需要使用到
     *
     * @param user
     * @return
     */
    @PostMapping("/register")
    @Deprecated
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("USER");
        }
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }


}
