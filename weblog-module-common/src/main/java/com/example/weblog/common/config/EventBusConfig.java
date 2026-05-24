package com.example.weblog.common.config;

import com.example.weblog.common.constant.EventListener;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBus eventBus(List<EventListener> listeners) {
        EventBus eventBus = new EventBus("WeBlog-EventBus");
        for (EventListener listener : listeners) {
            eventBus.register(listener);
        }
        return eventBus;
    }
}