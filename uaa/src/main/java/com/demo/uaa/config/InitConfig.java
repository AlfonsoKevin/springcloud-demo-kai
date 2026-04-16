package com.demo.uaa.config;

import com.demo.uaa.entity.User;
import com.demo.uaa.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author TangZhikai
 */
@Configuration
public class InitConfig {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("user_1", passwordEncoder.encode("user_1"), "user1@example.com", "USER"));
                userRepository.save(new User("editor_1", passwordEncoder.encode("editor_1"), "editor1@example.com", "EDITOR"));
                userRepository.save(new User("adm_1", passwordEncoder.encode("adm_1"), "adm1@example.com", "PRODUCT_ADMIN"));
                
                userRepository.save(new User("ldap_user_1", passwordEncoder.encode("ldap_user_1"), "ldap_user1@example.com", "USER"));
                userRepository.save(new User("ldap_editor_1", passwordEncoder.encode("ldap_editor_1"), "ldap_editor1@example.com", "EDITOR"));
                userRepository.save(new User("ldap_adm_1", passwordEncoder.encode("ldap_adm_1"), "ldap_adm1@example.com", "PRODUCT_ADMIN"));
            }
        };
    }
}
