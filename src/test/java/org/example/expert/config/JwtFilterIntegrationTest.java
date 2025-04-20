package org.example.expert.config;

import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.expert.domain.common.exception.ErrorMessage.AUTH_FORBIDDEN;
import static org.example.expert.domain.common.exception.ErrorMessage.JWT_REQUIRED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.profiles.active=local"})
@AutoConfigureMockMvc
class JwtFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void 토큰이_없으면_400에러와_JWT_REQUIRED메시지가_반환된다() throws Exception {
        // given - 토큰이 없음

        // when & then: andExpect()가 assertThat 역할을 한다고 함
        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isBadRequest())     // 400 에러 코드
                .andExpect(jsonPath("$.message").value(JWT_REQUIRED.getMessage()));     // 지정 예외 메시지
    }

    // $.message : JsonPath, Json 전용 쿼리 언어, Json 응답의 message 필드 탐색
    @Test
    void 일반유저가_adminAPI요청시_403예외와_AUTH_FORBIDDEN메시지가_반환된다() throws Exception {
        // given
        String token = jwtUtil.createToken(1L, "user@a.com", UserRole.USER); // 일반유저 및 토큰 생성

        // when & then
        mockMvc.perform(get("/admin/users/1")           // 관리자 api 요청
                        .header("Authorization", token))     // 헤더 토큰 입력
                .andExpect(status().isForbidden())  // 403 에러 코드
                .andExpect(jsonPath("$.message").value(AUTH_FORBIDDEN.getMessage()));   // 지정 예외 메시지
    }
}