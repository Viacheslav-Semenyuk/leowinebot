package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AfterMatchHandler implements Handler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public void handle(Message message, User user, String chatId) {

        switch (message.getText()) {
            case ("1"):
                user.setUserStates("search");
                userService.save(user);
                searchHandler.handle(message, user, chatId);
                break;
            case ("2"):
                user.setProfileEditStates("0");
                user.setUserStates("profile");
                userService.save(user);
                profileHandler.handle(message, user, chatId);
                break;
            case ("3"):
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.twoKeyboard()))
                        .setText("Так ты не узнаешь, что кому-то нравишься... Точно хочешь отключить свою анкету?\n" +
                                "\n" +
                                "1. Да, отключить анкету.\n" +
                                "2. Нет, вернуться назад."));
                user.setUserStates("active");
                userService.save(user);
                break;
            default:
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Нет такого варианта ответа"));
                break;
        }

    }

    @Override
    public boolean test(String o) {
        return "afterMatch".equals(o);
    }
}
