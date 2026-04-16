package com.demo.uaa.controller;

import com.demo.uaa.dto.TokenRequest;
import com.demo.uaa.entity.User;
import com.demo.uaa.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody TokenRequest tokenRequest) {

        if (!"password".equals(tokenRequest.getGrantType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported grantType");
        }

        //1.查询
        Optional<User> userOpt = userRepository.findByUsername(tokenRequest.getUsername());
        //2.校验
        if (userOpt.isPresent() && passwordEncoder.matches(tokenRequest.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            //3.签发
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
