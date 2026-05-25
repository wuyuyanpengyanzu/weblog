package com.example.weblog.jwt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResultUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(data));
        writer.flush();
        writer.close();
    }
}
