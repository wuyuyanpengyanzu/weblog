package com.example.weblog.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.example.weblog.*"})
@MapperScan({"com.example.weblog.common.domain.mapper"})
public class WeblogApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeblogApplication.class,args);
    }
}
