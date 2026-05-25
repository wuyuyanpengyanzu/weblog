package com.example.weblog.web.utils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Markdown → HTML 渲染工具，基于 flexmark。解析器静态初始化，线程安全。
 */
public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
                com.vladsch.flexmark.ext.tables.TablesExtension.create(),           // GFM 表格
                com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create() // 删除线
        ));
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /** 将 Markdown 文本渲染为 HTML */
    public static String parse2Html(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        return RENDERER.render(PARSER.parse(markdown));
    }
}
