package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Component
public class CommandHandler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private List<Handler> handlers;


    public void handle(Message message, User user, String chatId) {

        if (message.getText() != null && message.getText().equals("/start") && !message.hasPhoto()) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Мы тебя помним!"));

            user.setUserStates("profile");
            user.setSearchStates("0");
            user.setCountForCity(0);
            user.setMatchStates("0");
            user.setActive(true);
            user.setProfileEditStates("0");
            userService.save(user);
        }

        handlers.stream()
                .filter(handler -> handler.test(user.getUserStates()))
                .findFirst()
                .get().handle(message, user, chatId);

    }
}
