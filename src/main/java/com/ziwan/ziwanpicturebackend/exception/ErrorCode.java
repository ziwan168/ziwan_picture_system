package com.ziwan.ziwanpicturebackend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "成功"),
    PARAM_ERROR(40000, "参数错误"),

    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTO_ERROR(40102, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    REQUEST_FORBIDDEN_ERROR(40300, "请求被禁止"),
    SYSTEM_ERROR(50000, "服务器错误"),
    OPERATION_ERROR(50001, "操作失败"),
    ;


    /**
     * 状态码
     */
    private final int code;


    /**
     * 信息
     */
    private final String message;



    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
