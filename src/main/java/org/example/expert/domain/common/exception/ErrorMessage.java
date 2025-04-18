package org.example.expert.domain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage {

    // Common
    BAD_REQUEST("잘못된 요청입니다."),
    NOT_FOUND_END_POINT("존재하지 않는 API입니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
    EXCEPTION_ERROR("서버 내부 오류가 발생했습니다."),

    // Weather
    WEATHER_FAIL_REQUEST("날씨 데이터를 가져오는데 실패했습니다."),
    WEATHER_NOT_FOUND("날씨 데이터가 없습니다."),
    WEATHER_TODAY_NOT_FOUND("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다."),

    // Auth & Jwt
    AUTH_TYPE_MISMATCH("@Auth와 AuthUser 타입은 함께 사용되어야 합니다."),
    AUTH_FORBIDDEN("관리자 권한이 없습니다."),

    JWT_REQUIRED("JWT 토큰이 필요합니다."),
    JWT_INVALID("유효하지 않는 JWT 토큰입니다."),
    JWT_INVALID_SIGNATURE("유효하지 않는 JWT 서명입니다."),
    JWT_BAD_TOKEN("잘못된 JWT 토큰입니다."),
    JWT_EXPIRED("만료된 JWT 토큰입니다."),
    JWT_UNSUPPORTED("지원되지 않는 JWT 토큰입니다."),
    JWT_NOT_FOUND_TOKEN("JWT 토큰이 없습니다"),

    // 기타
    INTERNAL_ERROR("서버 내부 오류가 발생했습니다."),

    // Todos
    TODO_NOT_FOUND("해당 Todo가 존재하지 않습니다."),
    TODO_CREATOR_MISMATCH("일정을 만든 유저가 유효하지 않습니다."),

    // Comment

    // User
    USER_EXIST_EMAIL("이미 존재하는 이메일입니다."),
    USER_NOT_FOUND("가입되지 않은 유저입니다."),
    USER_PASSWORD_INVALID("잘못된 비밀번호입니다."),
    INVALID_USER_ROLE("유효하지 않은 USERROLE입니다."),
    PASSWORD_DUPLICATE_NEW("새 비밀번호는 기존 비밀번호와 같을 수 없습니다."),

    // Manager
    MANAGER_NOT_FOUND("해당 담당자가 존재하지 않습니다."),
    MANAGER_USER_NOT_FOUND("등록하려고 하는 담당자 유저가 존재하지 않습니다."),
    MANAGER_CANNOT_ASSIGN_SELF("일정 작성자는 본인을 담당자로 등록할 수 없습니다."),
    MANAGER_NOT_REGISTERED_TODO("해당 일정에 등록된 담당자가 아닙니다."),
    MANAGER_ASSIGN_NOT_ALLOWED("담당자를 등록하려고 하는 유저와 일정을 만든 유저가 유효하지 않습니다."),
    ;

    private final String message;

}
