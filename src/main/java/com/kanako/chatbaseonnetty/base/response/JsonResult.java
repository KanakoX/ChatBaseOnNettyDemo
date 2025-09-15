package com.kanako.chatbaseonnetty.base.response;

public record JsonResult(Integer code, String message, Object data) {

    public static <T> JsonResult success(T data) {
        return new JsonResult(200, "success", data);
    }
}
