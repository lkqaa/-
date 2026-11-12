package com.example.blog.common;

import java.io.Serializable;

public class ErrorCode implements Serializable {

    public static final ErrorCode SUCCESS = new ErrorCode(200, "success");
    public static final ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数错误");
    public static final ErrorCode UNAUTHORIZED = new ErrorCode(401, "未登录或Token已过期");
    public static final ErrorCode FORBIDDEN = new ErrorCode(403, "无权限访问");
    public static final ErrorCode NOT_FOUND = new ErrorCode(404, "资源不存在");
    public static final ErrorCode INTERNAL_ERROR = new ErrorCode(500, "服务器内部错误");
    public static final ErrorCode LOGIN_FAILED = new ErrorCode(1001, "用户名或密码错误");
    public static final ErrorCode ARTICLE_NOT_FOUND = new ErrorCode(1101, "文章不存在");
    public static final ErrorCode CATEGORY_NOT_FOUND = new ErrorCode(1102, "分类不存在");
    public static final ErrorCode TAG_NOT_FOUND = new ErrorCode(1103, "标签不存在");
    public static final ErrorCode RATE_LIMITED = new ErrorCode(429, "请求过于频繁，请稍后再试");

    private final Integer code;
    private final String message;

    public ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
