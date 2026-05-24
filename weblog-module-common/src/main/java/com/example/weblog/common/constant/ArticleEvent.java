package com.example.weblog.common.constant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleEvent {

    private Long articleId;
    private String message;
}
