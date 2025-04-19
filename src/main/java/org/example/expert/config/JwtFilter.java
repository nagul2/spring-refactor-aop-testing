package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.dto.ErrorDto;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.example.expert.config.JwtConst.*;
import static org.example.expert.domain.common.exception.ErrorMessage.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // HttpServletRequest, HttpServletResponse -> 스트림이라 한 번 읽으면 끝남
        // spring에서 읽어버리면 AOP에서 사용할 수 없기 때문에 ContentCachingWrapper로 감싸서 바디를 캐싱해둠
        ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(httpResponse);

        String url = httpRequest.getRequestURI();

        if (url.startsWith("/auth")) {
            chain.doFilter(wrapperRequest, wrapperResponse);
            wrapperResponse.copyBodyToResponse();
            return;
        }

        String bearerJwt = wrapperRequest.getHeader(AUTHORIZATION);

        if (bearerJwt == null) {
            // 토큰이 없는 경우 400을 반환합니다.
            filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_BAD_REQUEST, HttpStatus.BAD_REQUEST, JWT_REQUIRED.getMessage());
            return;
        }
        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            // JWT 유효성 검사와 claims 추출
            Claims claims = jwtUtil.extractClaims(jwt);
            if (claims == null) {
                filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_BAD_REQUEST, HttpStatus.BAD_REQUEST, JWT_BAD_TOKEN.getMessage());
                return;
            }

            UserRole userRole = UserRole.valueOf(claims.get(USER_ROLE, String.class));

            wrapperRequest.setAttribute(USER_ID, Long.parseLong(claims.getSubject()));
            wrapperRequest.setAttribute(EMAIL, claims.get(EMAIL));
            wrapperRequest.setAttribute(USER_ROLE, claims.get(USER_ROLE));

            if (url.startsWith("/admin")) {
                // 관리자 권한이 없는 경우 403을 반환합니다.
                if (!UserRole.ADMIN.equals(userRole)) {
                    filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_FORBIDDEN, HttpStatus.FORBIDDEN, AUTH_FORBIDDEN.getMessage());
                    return;
                }
                chain.doFilter(wrapperRequest, wrapperResponse);
                return;
            }


            chain.doFilter(wrapperRequest, wrapperResponse);


        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_UNAUTHORIZED, HttpStatus.UNAUTHORIZED, JWT_INVALID_SIGNATURE.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_UNAUTHORIZED, HttpStatus.UNAUTHORIZED, JWT_EXPIRED.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_BAD_REQUEST, HttpStatus.BAD_REQUEST, JWT_UNSUPPORTED.getMessage());
        } catch (Exception e) {
            log.error("Invalid JWT token, 유효하지 않는 JWT 토큰 입니다.", e);
            filterErrorResponse(wrapperRequest, wrapperResponse, HttpServletResponse.SC_BAD_REQUEST, HttpStatus.BAD_REQUEST, JWT_INVALID.getMessage());
        } finally {
            wrapperResponse.copyBodyToResponse();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private void filterErrorResponse(ContentCachingRequestWrapper requestWrapper,
                                     ContentCachingResponseWrapper responseWrapper,
                                     int code,
                                     HttpStatus status,
                                     String message) throws IOException {

        responseWrapper.setStatus(code);
        responseWrapper.setContentType("application/json");
        responseWrapper.setCharacterEncoding("UTF-8");

        String errorJson = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValueAsString(new ErrorDto(
                        code,
                        status,
                        message,
                        LocalDateTime.now(),
                        requestWrapper.getRequestURL().toString()));

        responseWrapper.getWriter().write(errorJson);
        responseWrapper.copyBodyToResponse();
    }
}
