package com.example.meezy.bc.sharedkernel.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러 입니다."),

    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레쉬 토큰이 존재하지 않습니다."),

    PROFILE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 프로필 설정이 완료되었습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_ACCOUNT_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
    USER_PROFILE_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 가입 프로필을 완료한 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 다릅니다."),

    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    VERIFICATION_ATTEMPT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수를 초과했습니다. 24시간 후에 다시 시도해주세요."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다"),
    INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 확장자입니다."),
    FAILED_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FAILED_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "해당 팀의 리더가 아닙니다."),
    DUPLICATE_TEAM_MEMBER(HttpStatus.CONFLICT, "이미 팀에 참가한 유저입니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 멤버를 찾을 수 없습니다."),
    CANNOT_REMOVE_LEADER(HttpStatus.BAD_REQUEST, "리더는 팀에서 제거할 수 없습니다."),
    INVITE_CODE_NOT_GENERATED(HttpStatus.BAD_REQUEST, "초대 코드가 생성되지 않았습니다."),
    INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "초대 코드가 만료되었습니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "초대 코드가 일치하지 않습니다."),

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 팀이 존재하지 않습니다."),
    TEAM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "팀 생성에 실패했습니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채팅방이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
