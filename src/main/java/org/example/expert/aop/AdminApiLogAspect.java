package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AdminApiLogAspect {

    @Pointcut("execution(* org.example.expert.domain.*.controller.*AdminController.*(..))")
    private void adminControllerPointcut() {}

    @Around("adminControllerPointcut()")
    public Object adminControllerAccessLog(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        // 요청한 사용자의 ID 로그
        Long requestUserId = (Long) request.getAttribute("userId");

        // API 요청 시각 로그
        String requestTime = LocalDateTime.now().toString();

        // API 요청 URL 로그
        String requestUrl = request.getRequestURL().toString();
        log.info("요청한 사용자 ID: {}", requestUserId);
        log.info("요청 시각: {}", requestTime);
        log.info("요청 URL: {}", requestUrl);

        // 요청 본문(ReqeustBody) 로그
        if (request instanceof ContentCachingRequestWrapper wrapperRequest) {
            String requestBody = new String(wrapperRequest.getContentAsByteArray(), wrapperRequest.getCharacterEncoding());
            log.info("요청 본문: {}", requestBody);
        }

        // 응답 본문(ResponseBody) 로그
        Object result = joinPoint.proceed();
        if (response instanceof ContentCachingResponseWrapper wrapperResponse) {
            String responseBody = new String(wrapperResponse.getContentAsByteArray(), wrapperResponse.getCharacterEncoding());
            log.info("응답 본문: {}", responseBody);
        }
        return result;
    }

}
