package com.coursebuddy;

import com.coursebuddy.common.security.JwtUtil;
import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        "jwt.secret=test-secret-key-for-unit-tests-minimum-32-chars-long",
        "jwt.expiration=3600000"
})
class CourseBuddyApplicationTest {

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        assertThat(jwtUtil).isNotNull();
    }

    @Test
    void jwtTokenGenerationAndValidation() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(Role.STUDENT)
                .build();

        String token = jwtUtil.generateToken(user);
        assertThat(token).isNotBlank();

        String extractedUsername = jwtUtil.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");

        assertThat(jwtUtil.validateToken(token, user)).isTrue();
    }
}
