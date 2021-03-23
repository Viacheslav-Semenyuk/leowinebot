package com.example.leowinebot.bot.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class HandlerConfig {

    @Bean
    public List<Handler> handlers() {
        List<Handler> handlers = new ArrayList<>();
        handlers.add(new ActiveHandler());
        handlers.add(new AfterMatchHandler());
        handlers.add(new MatchHandler());
        handlers.add(new MessageHandler());
        handlers.add(new ProfileAboutEditHandler());
        handlers.add(new ProfileEditHandler());
        handlers.add(new ProfileHandler());
        handlers.add(new ProfilePhotoEditHandler());
        handlers.add(new SearchHandler());
        handlers.add(new StartHandler());
        return handlers;
    }
}
