package com.example.leowinebot.bot.handler;

import com.example.leowinebot.bot.Bot;
import com.example.leowinebot.entity.User;
import com.example.leowinebot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ActiveHandler implements Handler {

    @Autowired
    private Bot bot;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileHandler profileHandler;

    @Autowired
    private KeyboardHandler keyboardHandler;

    public void handle(Message message, User user, String chatId) {

        if (message.getText().equals("1") && user.getActive().equals(false)) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Мы тебя помним!"));
            user.setUserStates("profile");
            userService.save(user);
            profileHandler.handle(message, user, chatId);
        } else if (user.getActive().equals(false)) {
            bot.executeMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Нет такого варианта ответа"));
        }

        if (user.getActive().equals(true)) {
            if (message.getText().equals("1")) {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.oneKeyboard()))
                        .setText("Надеюсь ты нашел кого-то благодаря мне! Рад был с тобой пообщаться, " +
                                "будет скучно – пиши, обязательно найдем тебе кого-нибудь\n" +
                                "\n" +
                                "1. Смотреть анкеты"));
                user.setActive(false);
                userService.save(user);
            } else if (message.getText().equals("2")) {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setReplyMarkup(keyboardHandler.handle(keyboardHandler.threeKeyboard()))
                        .setText("1. Смотреть анкеты.\n" +
                                "2. Моя анкета.\n" +
                                "3. Я больше не хочу никого искать."));
                user.setUserStates("afterMatch");
                userService.save(user);
            } else {
                bot.executeMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText("Нет такого варианта ответа"));

            }
        }

    }

    @Override
    public boolean test(String o) {
        return "active".equals(o);
    }
}
