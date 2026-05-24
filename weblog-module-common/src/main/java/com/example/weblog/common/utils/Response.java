package com.example.weblog.common.utils;

import lombok.Data;

@Data
public class Response<T> {

    private boolean success = true;
    private String errorCode;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <T> Response<T> fail(String errorCode, String message) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
}
