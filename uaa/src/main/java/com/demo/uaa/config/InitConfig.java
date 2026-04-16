package com.demo.uaa.config;

import com.demo.uaa.entity.User;
import com.demo.uaa.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;

/**
 * @author TangZhikai
 */
@Configuration
public class InitConfig {

    @Autowired(required = false)
    private LdapTemplate ldapTemplate;

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
                // 尝试初始化 LDAP 数据
                if (ldapTemplate != null) {
                    try {
                        // 检查组织单元 users 是否存在，不存在则创建
                        Name ouName = LdapNameBuilder.newInstance().add("ou", "users").build();
                        try {
                            ldapTemplate.lookupContext(ouName);
                        } catch (org.springframework.ldap.NameNotFoundException e) {
                            DirContextAdapter ouContext = new DirContextAdapter(ouName);
                            ouContext.setAttributeValues("objectClass", new String[]{"top", "organizationalUnit"});
                            ouContext.setAttributeValue("ou", "users");
                            ldapTemplate.bind(ouContext);
                            System.out.println("LDAP OU 'users' created.");
                        }

                        // 初始化 ldap_user_1
                        initLdapUser("ldap_user_1", "user_1");
                        // 初始化 ldap_editor_1
                        initLdapUser("ldap_editor_1", "editor_1");
                        // 初始化 ldap_adm_1
                        initLdapUser("ldap_adm_1", "adm_1");

                    } catch (Exception e) {
                        System.err.println("LDAP 数据初始化失败，可能 LDAP 服务器未启动或已存在相关数据: " + e.getMessage());
                    }
                }
            }
        };
    }

    private void initLdapUser(String uid, String sn) {
        Name userDn = LdapNameBuilder.newInstance().add("ou", "users").add("uid", uid).build();
        try {
            ldapTemplate.lookupContext(userDn);
        } catch (org.springframework.ldap.NameNotFoundException e) {
            DirContextAdapter userContext = new DirContextAdapter(userDn);
            userContext.setAttributeValues("objectClass", new String[]{"top", "inetOrgPerson"});
            userContext.setAttributeValue("uid", uid);
            userContext.setAttributeValue("cn", uid);
            userContext.setAttributeValue("sn", sn);
            //暂时先明文
            userContext.setAttributeValue("userPassword", uid);
            ldapTemplate.bind(userContext);
            System.out.println("LDAP User '" + uid + "' created.");
        }
    }
}
