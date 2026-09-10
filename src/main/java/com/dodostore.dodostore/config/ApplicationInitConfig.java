package com.dodostore.dodostore.config;

import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.RoleEnum;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.BrandRepository;
import com.dodostore.dodostore.repository.CategoryRepository;
import com.dodostore.dodostore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationInitConfig {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, 
                                        BrandRepository brandRepository,
                                        CategoryRepository categoryRepository) {
        return args -> {
            if (userRepository.findByUsername("admin") == null) {

                UserEntity user = UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(RoleEnum.ADMIN)
                        .status(StatusEnum.Active)
                        .build();
                userRepository.save(user);
            }
        };
    }
}

